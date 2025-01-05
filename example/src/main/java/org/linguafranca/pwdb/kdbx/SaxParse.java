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

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.security.StreamEncryptor;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * @author jo
 */
public class SaxParse {
    /**
     * SAX Parsing - shows also how to decrypt values that are stored encrypted in XML (Inner Stream Encryption)
     * though it doesn't decode other values, like dates etc.
     */
    public void exampleSaxParsing(String resourceName, Credentials credentials, PrintWriter writer) throws IOException, SAXException, ParserConfigurationException {
        InputStream encryptedInputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        KdbxHeader kdbxHeader = new KdbxHeader();

        try (InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, kdbxHeader, encryptedInputStream)) {
            // use this to decrypt the encrypted fields
            final StreamEncryptor valueEncryptor = kdbxHeader.getInnerStreamEncryptor();
            SAXParserFactory spfactory = SAXParserFactory.newInstance();
            SAXParser saxParser = spfactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();

            xmlReader.setContentHandler(new DefaultHandler() {
                boolean protectedContent = false;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) {
                    protectedContent = atts.getIndex("Protected") >= 0;
                    writer.append("<")
                                    .append(qName);
                    for (int i = 0; i < atts.getLength(); i++){
                        if (!atts.getLocalName(i).equals("Protected")) {
                            // we wil decrypt protected values
                            writer.append(" ")
                                    .append(atts.getLocalName(i))
                                    .append(" = \"")
                                    .append(atts.getValue(i))
                                    .append("\"");
                        }
                    }
                    writer.append(">");
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    writer.append("</")
                            .append(qName)
                            .append(">");
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    String content = new String(ch, start, length);
                    if (protectedContent) {
                        content = new String(valueEncryptor.decrypt(Helpers.decodeBase64Content(content.getBytes(), false)));
                    }
                    writer.append(content);
                    protectedContent = false;
                }
            });

            InputSource xmlInputSource = new InputSource(decryptedInputStream);
            xmlReader.parse(xmlInputSource);
        }
        writer.flush();
    }
}
