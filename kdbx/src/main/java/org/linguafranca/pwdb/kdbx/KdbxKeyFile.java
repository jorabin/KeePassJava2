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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.linguafranca.pwdb.security.Encryption;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
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

    /**
     * Load a key from an InputStream with a KDBX XML key file.
     * @param inputStream the input stream holding the key
     * @return the key
     */
    public static byte[] load(InputStream inputStream) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = documentBuilder.parse(inputStream);
            String version = (String) xpath.evaluate("//KeyFile/Meta/Version/text()", doc, XPathConstants.STRING);
            String data = (String) xpath.evaluate("//KeyFile/Key/Data/text()", doc, XPathConstants.STRING);
            if (data == null) {
                return null;
            }
            if (version.equals("2.0")) {
               
                byte[] hexData = Hex.decodeHex(data.replaceAll("\\s",""));
               
                MessageDigest md = Encryption.getSha256MessageDigestInstance();
                byte[] computedHash = md.digest(hexData);
               
                String hashToCheck = (String) xpath.evaluate("//KeyFile/Key/Data/@Hash", doc, XPathConstants.STRING);
                byte[] verifiedHash = Hex.decodeHex(hashToCheck);
                
                boolean isHashVerified = Arrays.equals(Arrays.copyOf(computedHash, verifiedHash.length), verifiedHash);
                if(!isHashVerified) {
                    throw new IllegalStateException("Hash mismatch error");        
                }
                return hexData;
                
            }
            return Base64.decodeBase64(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Key File input stream cannot be null");
        }
    }
}
