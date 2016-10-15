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
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;

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
     * @return they key or null if there was a problem
     */
    public static byte[] load(InputStream inputStream) {
        String base64;
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = documentBuilder.parse(inputStream);
            base64 = (String) xpath.evaluate("//KeyFile/Key/Data/text()", doc, XPathConstants.STRING);
            if (base64 == null) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        // android compatibility
        return Base64.decodeBase64(base64.getBytes());
    }
}
