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
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class has a static method to load a key from an {@link InputStream}
 */
@SuppressWarnings("WeakerAccess")
public class KdbxKeyFile {

    private static final XPath xpath = XPathFactory.newInstance().newXPath();
    private static final int BUFFER_SIZE = 65;
    private static final int KEY_LEN_32 = 32;
    private static final int KEY_LEN_64 = 64;

    /**
     * Load a key from an InputStream, in this method the inputStrem represent the KeyFile
     * <p>
     * A key file is a file that contains a key (and possibly additional data, e.g. a hash that allows to verify the integrity of the key). The file extension typically is 'keyx' or 'key'.
     * </p>
     *   Formats. KeePass supports the following key file formats:
     *   <ul>
     *       <li>XML (recommended, default). There is an XML format for key files. KeePass 2.x uses this format by default, i.e. when creating a key file in the master key dialog, an XML key file is created. The syntax and the semantics of the XML format allow to detect certain corruptions (especially such caused by faulty hardware or transfer problems), and a hash (in XML key files version 2.0 or higher) allows to verify the integrity of the key. This format is resistant to most encoding and new-line character changes (which is useful for instance when the user is opening and saving the key file or when transferring it from/to a server). Such a key file can be printed (as a backup on paper), and comments can be added in the file (with the usual XML syntax: <!-- ... -->). It is the most flexible format; new features can be added easily in the future.</li>
     *       <li>32 bytes. If the key file contains exactly 32 bytes, these are used as a 256-bit cryptographic key. This format requires the least disk space.</li>
     *       <li>Hexadecimal. If the key file contains exactly 64 hexadecimal characters (0-9 and A-F, in UTF-8/ASCII encoding, one line, no spaces), these are decoded to a 256-bit cryptographic key.</li>
     *       <li>Hashed. If a key file does not match any of the formats above, its content is hashed using a cryptographic hash function in order to build a key (typically a 256-bit key with SHA-256). This allows to use arbitrary files as key files.</li>
     *   </ul>
     *
     * @param inputStream the input stream holding the key, caller should close
     * @return the key
     */
    public static byte[] load(InputStream inputStream) {
        // wrap the stream to get its digest (in case we need it)
        DigestInputStream digestInputStream = new DigestInputStream(inputStream,
                Encryption.getSha256MessageDigestInstance());
        // wrap the stream, so we can test reading from it but then push back to get original stream
        PushbackInputStream pis = new PushbackInputStream(digestInputStream, BUFFER_SIZE);
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = pis.read(buffer);

            // if length 32 assume binary key file
            if (bytesRead == KEY_LEN_32) {
                return Arrays.copyOf(buffer, bytesRead);
            }

            // if length 64 may be hex encoded key file
            if (bytesRead == KEY_LEN_64) {
                try {               
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                    Charset charSet = StandardCharsets.UTF_8;
                    CharBuffer charBuffer = charSet.decode(byteBuffer);
                    charBuffer.limit(KEY_LEN_64);
                    char[] hexValue = new char[charBuffer.remaining()];
                    charBuffer.get(hexValue);
                    return Hex.decodeHex(hexValue); // (avoid creating a String)
                } catch (DecoderException ignored) {
                    // fall through it may be an XML file or just a file whose digest we want
                }
            }
            // restore stream
            pis.unread(buffer);

            // if length not 32 or 64 either an XML key file or just a file to get digest
            try {
                // see if it's an XML key file
                return tryComputeXmlKeyFile(pis);
            } catch (HashMismatchException e) {
                throw new IllegalArgumentException("Invalid key in signature file");
            } catch (Exception ignored) {
                // fall through to get file digest
            }

            // is not a valid xml file, so read the remainder of file
            byte[] sink = new byte[1024];
            // read file to get its digest
            //noinspection StatementWithEmptyBody
            while (digestInputStream.read(sink) > 0) { /* nothing */ }
            return digestInputStream.getMessageDigest().digest();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Read the InputStream (kdbx xml keyfile) and compute the hash (SHA-256) to build a key
     *
     * @param is The KeyFile as an InputStream, must return with stream open on error
     * @return the computed byte array (keyFile) to compute the MasterKey
     */
    private static byte[] tryComputeXmlKeyFile(InputStream is) throws HashMismatchException {
        // DocumentBuilder closes input stream so wrap inputStream to inhibit this in case of failure
        InputStream unCloseable = new FilterInputStream(is) {
            @Override
            public void close() { /* nothing */ }
        };
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = documentBuilder.parse(unCloseable);
            // get the key
            String data = (String) xpath.evaluate("//KeyFile/Key/Data/text()", doc, XPathConstants.STRING);
            if (data == null) {
                throw new IllegalArgumentException("Key file does not contain a key");
            }
            // get the file version
            String version = (String) xpath.evaluate("//KeyFile/Meta/Version/text()", doc, XPathConstants.STRING);
            // if not 2.0 then key is base64 encoded
            if (Objects.isNull(version) || !version.equals("2.0")) {
                return Base64.decodeBase64(data);
            }

            // key data may contain white space
            byte[] decodedData = Hex.decodeHex(data.replaceAll("\\s", ""));
            byte[] decodedDataHash = Encryption.getSha256MessageDigestInstance().digest(decodedData);

            // hash used to verify the data
            String hashToCheck = (String) xpath.evaluate("//KeyFile/Key/Data/@Hash", doc, XPathConstants.STRING);
            byte[] decodedHashToCheck = Hex.decodeHex(hashToCheck);

            // hashToCheck is a truncated version of the actual hash
            if (!Arrays.equals(Arrays.copyOf(decodedDataHash, decodedHashToCheck.length), decodedHashToCheck)) {
                throw new HashMismatchException();
            }
            return decodedData;
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException |
                 DecoderException e) {
            throw new IllegalArgumentException("An error occurred during XML parsing: " + e.getMessage());
        }
    }
    private static class HashMismatchException extends Exception {
    }
}