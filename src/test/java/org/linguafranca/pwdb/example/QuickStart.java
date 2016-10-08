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

package org.linguafranca.pwdb.example;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdb.KdbCredentials;
import org.linguafranca.pwdb.kdb.KdbDatabase;
import org.linguafranca.pwdb.kdbx.*;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabaseWrapper;
import org.linguafranca.security.Credentials;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Examples for QuickStart
 *
 * @author jo
 */
public class QuickStart {

/*
### Load KDBX
*/
    public void loadKdbx() throws IOException {
        // get an input stream
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        // password credentials
        Credentials credentials = new KdbxCreds("123".getBytes());
        // open database using Dom implementation (slow)
        Database database = DomDatabaseWrapper.load(credentials, inputStream);
        // alternatively (faster)
        database = JaxbDatabaseWrapper.load(credentials, inputStream);

        // visit all groups and entries and list them to console
        database.visit(new Visitor.Print());
    }

/*
### Save KDBX
*/

    private static Entry entryFactory(Database database, String s, int e) {
        return database.newEntry(String.format("Group %s Entry %d", s, e));
    }

    public void saveKdbx() throws IOException {
        // create an empty database
        Database database = new JaxbDatabaseWrapper();

        // add some groups and entries
        for (Integer g = 0; g < 5; g++){
            Group group = database.getRootGroup().addGroup(database.newGroup(g.toString()));
            for (int e = 0; e <= g; e++) {
                // entry factory is a local helper to populate an entry
                group.addEntry(entryFactory(database, g.toString(), e));
            }
        }

        // save to a file with password "123"
        FileOutputStream outputStream = new FileOutputStream("test.kdbx");
        database.save(new KdbxCreds("123".getBytes()), outputStream);
    }

/*
### Load KDB
*/
    public void loadKdb() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test.kdb");
        // password credentials
        Credentials credentials = new KdbCredentials.Password("123".getBytes());
        // load KdbDatabase
        Database database = KdbDatabase.load(credentials, inputStream);
        // visit all groups and entries and list them to console
        database.visit(new Visitor.Print());
    }

/*
### SAX Parsing
 */
    public void exampleSaxparsing () throws IOException, SAXException, ParserConfigurationException {
        InputStream encryptedInputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        Credentials credentials = new KdbxCreds("123".getBytes());
        KdbxHeader kdbxHeader = new KdbxHeader();
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, kdbxHeader, encryptedInputStream);
        try {
            // use this to decrypt the encrypted fields
            final Salsa20Encryption memoryProtection = new Salsa20Encryption(kdbxHeader.getProtectedStreamKey());
            SAXParserFactory spfactory = SAXParserFactory.newInstance();
            SAXParser saxParser = spfactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();

            xmlReader.setContentHandler(new ContentHandler() {
                boolean protectedContent = false;
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
                    protectedContent = atts.getIndex("Protected")>=0;
                    System.out.print("<" + qName + ">");
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    System.out.print("</" + qName + ">");
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    String content =  new String(ch, start, length);
                    if (protectedContent) {
                        content  = new String(memoryProtection.decrypt(Helpers.getBinaryBase64Content(content.getBytes(),false)));
                    }
                    System.out.print(content);
                    protectedContent=false;
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
