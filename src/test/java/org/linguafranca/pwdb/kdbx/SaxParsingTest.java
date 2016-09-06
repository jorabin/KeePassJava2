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

import org.junit.Test;
import org.linguafranca.security.Credentials;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple illustration of hooking a SAX parser up to process a KDBX file
 * 
 * @author jo
 */
public class SaxParsingTest {
    @Test
    public void exampleSaxparsing () throws IOException, SAXException, ParserConfigurationException {
        InputStream encryptedInputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        Credentials credentials = new KdbxCredentials.Password("123".getBytes());
        KdbxHeader kdbxHeader = new KdbxHeader();
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, kdbxHeader, encryptedInputStream);
        try {
            // use this to decrypt the encrypted fields
            Salsa20Encryption memoryProtection = new Salsa20Encryption(kdbxHeader.getProtectedStreamKey());
            SAXParserFactory spfactory = SAXParserFactory.newInstance();
            SAXParser saxParser = spfactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();

            xmlReader.setContentHandler(new ContentHandler() {
                @Override
                public void setDocumentLocator(Locator locator) {

                }

                @Override
                public void startDocument() throws SAXException {
                    System.out.println("Starting document");
                }

                @Override
                public void endDocument() throws SAXException {
                    System.out.println("Ending document");
                }

                @Override
                public void startPrefixMapping(String prefix, String uri) throws SAXException {

                }

                @Override
                public void endPrefixMapping(String prefix) throws SAXException {

                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                    System.out.println("<" + qName + ">");
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    System.out.println("</" + qName + ">");
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    System.out.println(new String(ch, start, length));
                }

                @Override
                public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

                }

                @Override
                public void processingInstruction(String target, String data) throws SAXException {

                }

                @Override
                public void skippedEntity(String name) throws SAXException {

                }
            });

            InputSource xmlInputSource  =   new InputSource(decryptedInputStream);
            xmlReader.parse(xmlInputSource);
        } finally {
            decryptedInputStream.close();
        }
    }
}
