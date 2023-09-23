/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.pwdb.kdbx;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.linguafranca.pwdb.security.Encryption;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Class has a static method to load a key from a KDBX XML Key File
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class KdbxKeyFile {

    private static XPath xpath = XPathFactory.newInstance().newXPath();
    private static int BUFFER_SIZE = 65;
    private static int KEY_LEN_32 = 32;
    private static int KEY_LEN_64 = 64;

    /**
     * Load a key from an InputStream with a KDBX XML key file.
     * 
     * @param inputStream the input stream holding the key
     * @return the key
     */
    public static byte[] load(InputStream inputStream) {
        try {

            PushbackInputStream pis = new PushbackInputStream(inputStream, BUFFER_SIZE);
            byte[] buffer = new byte[65];
            int bytesRead = pis.read(buffer);

            if (bytesRead == KEY_LEN_32) {
                return Arrays.copyOf(buffer, bytesRead);
            } else if (bytesRead == KEY_LEN_64) {
                byte[] keyFile = Hex.decodeHex(new String(Arrays.copyOf(buffer, bytesRead)));
                return keyFile;
            } else {
                if (isXML(buffer)) {
                    pis.unread(buffer); // Push back the buffer
                    return computeXmlKeyFile(pis);
                } else {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(buffer, 0, bytesRead); // Insert the first 65 bytes in the OutputStream
                    buffer = new byte[1024]; // Increase the buffer
                    while ((bytesRead = pis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    // Compute the SHA256 of the InputStream
                    MessageDigest md = Encryption.getSha256MessageDigestInstance();
                    byte[] keyFile = md.digest(outputStream.toByteArray());
                    return keyFile;
                }
            }
        } catch (IOException | DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Check if the data is an XML
     * @param data the data to ckeck
     * @return true if the data is an XML
     */
    private static boolean isXML(byte[] data) {

        try {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            XMLStreamReader reader = factory.createXMLStreamReader(bais);
            // Attempt to read the start element of the XML document.
            if (reader.hasNext()) {
                int eventType = reader.next();
                if (eventType == XMLStreamReader.START_ELEMENT) {
                    return true; // The InputStream contains valid XML
                }
            }
            // If we reach this point, it's not valid XML.
            return false;
        } catch (Exception e) {
            // An exception occurred, so it's not valid XML.
            return false;
        }
    }

    /**
     * Read the InputStream (keyx file) and compute the hash (SHA2-256) to build a key
     * 
     * @param is The KeyFile as an InputStream
     * @return the computed byte array (keyFile) to compute the MasterKey
     */
    private static byte[] computeXmlKeyFile(InputStream is) {

        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = documentBuilder.parse(new PushbackInputStream(is)); // This function close the input stream, we
                                                                                // need to create another one
            String version = (String) xpath.evaluate("//KeyFile/Meta/Version/text()", doc, XPathConstants.STRING);
            String data = (String) xpath.evaluate("//KeyFile/Key/Data/text()", doc, XPathConstants.STRING);
            if (data == null) {
                return null;
            }
            if (version.equals("2.0")) {
                byte[] hexData = Hex.decodeHex(data.replaceAll("\\s", ""));
                MessageDigest md = Encryption.getSha256MessageDigestInstance();
                byte[] computedHash = md.digest(hexData);
                String hashToCheck = (String) xpath.evaluate("//KeyFile/Key/Data/@Hash", doc, XPathConstants.STRING);
                byte[] verifiedHash = Hex.decodeHex(hashToCheck);
    
                boolean isHashVerified = Arrays.equals(Arrays.copyOf(computedHash, verifiedHash.length),
                        verifiedHash);
                if (!isHashVerified) {
                    throw new IllegalStateException("Hash mismatch error");
                }
                return hexData;
            }
            return Base64.decodeBase64(data.getBytes());
            
        } catch(IOException | SAXException | ParserConfigurationException | XPathExpressionException | DecoderException e) {
            throw new IllegalArgumentException("An error occours during XML parsing: " + e.getMessage());
        }
    }
}
