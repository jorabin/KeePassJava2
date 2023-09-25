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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class has a static method to load a key from an {@link InputStream}
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class KdbxKeyFile {

    private static final XPath xpath = XPathFactory.newInstance().newXPath();
    private static final int BUFFER_SIZE = 65;
    private static final int KEY_LEN_32 = 32;
    private static final int KEY_LEN_64 = 64;

    private static class HashMismatchException extends Exception {}

    /**
     * Load a key from an InputStream
     * <p>
     *     The InputStream can represent ... TODO write about the formats
     *
     * @param inputStream the input stream holding the key, caller should close
     * @return the key
     */
    public static byte[] load(InputStream inputStream) {
        DigestInputStream digestInputStream = new DigestInputStream(inputStream, Encryption.getSha256MessageDigestInstance());
        PushbackInputStream pis = new PushbackInputStream(digestInputStream, BUFFER_SIZE);
        try {
            byte[] buffer = new byte[65];
            int bytesRead = pis.read(buffer);
            if (bytesRead == KEY_LEN_32) {
                // if length 32 assume binary key file
                return buffer;
            } else if (bytesRead == KEY_LEN_64) {
                // if length 64 assume hex encoded key file (avoid creating a String)
                return Hex.decodeHex(ByteBuffer.wrap(buffer).asCharBuffer().array());
            } else {
                // if length not 32 or 64 either an XML key file or just a file to get digest
                try {
                    // see if it's an XML key file
                    pis.unread(buffer);
                    return tryComputeXmlKeyFile(new FilterInputStream(pis){
                        @Override
                        public void close() {
                            // suppress ability to close, so we can carry on reading on exception
                        }
                    });
                } catch (HashMismatchException e) {
                    throw new IllegalArgumentException("Invalid key in signature file");
                } catch (Exception e) {
                    byte [] sink = new byte[1024];
                    // xml file was invalid so read the remainder of file
                    //noinspection StatementWithEmptyBody
                    while (digestInputStream.read(sink) > 0) {
                        // just reading file to get its digest
                    }
                    return digestInputStream.getMessageDigest().digest();
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Read the InputStream (kdbx xml keyfile) and compute the hash (SHA-256) to build a key
     *
     * @param is The KeyFile as an InputStream
     * @return the computed byte array (keyFile) to compute the MasterKey
     */
    private static byte[] tryComputeXmlKeyFile(InputStream is) throws HashMismatchException {

        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = documentBuilder.parse(is);
            String version = (String) xpath.evaluate("//KeyFile/Meta/Version/text()", doc, XPathConstants.STRING);
            String data = (String) xpath.evaluate("//KeyFile/Key/Data/text()", doc, XPathConstants.STRING);
            if (data == null) {
                return null;
            }
            if (Objects.isNull(version) || !version.equals("2.0")){
                return Base64.decodeBase64(data);
            }

            byte[] hexData = Hex.decodeHex(data.replaceAll("\\s", ""));
            MessageDigest md = Encryption.getSha256MessageDigestInstance();
            byte[] computedHash = md.digest(hexData);

            String hashToCheck = (String) xpath.evaluate("//KeyFile/Key/Data/@Hash", doc, XPathConstants.STRING);
            byte[] verifiedHash = Hex.decodeHex(hashToCheck);

            if (!Arrays.equals(Arrays.copyOf(computedHash, verifiedHash.length), verifiedHash)) {
                throw new HashMismatchException();
            }
            return hexData;
        } catch(IOException | SAXException | ParserConfigurationException | XPathExpressionException | DecoderException e) {
            throw new IllegalArgumentException("An error occurred during XML parsing: " + e.getMessage());
        }
    }
}
