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

package org.linguafranca.pwdb.kdbx.dom;


import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.security.StreamEncryptor;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * This class is an XML DOM implementation of a KDBX database. The data is maintained as a DOM,
 * despite the obvious inefficiency of doing do, in order to maintain transparency on loading and
 * saving of elements and attributes this implementation knows nothing about.
 *
 * <p>Obviously, perhaps, if the database is added to, or under certain types of modification,
 * those elements will be missing from a re-serialization.
 *
 * @author jo
 */
public class DomSerializableDatabase implements SerializableDatabase {

    private Document doc;
    private StreamEncryptor encryption;

    private DomSerializableDatabase() {}

    public static DomSerializableDatabase createEmptyDatabase() {
        DomSerializableDatabase result = new DomSerializableDatabase();
        // read in the template KeePass XML database
        try {
            result.load(result.getClass().getClassLoader().getResourceAsStream("base.kdbx.xml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            // set the root group UUID
            Node uuid = (Node) DomHelper.xpath.evaluate("//"+ DomHelper.UUID_ELEMENT_NAME, result.doc.getDocumentElement(), XPathConstants.NODE);
            uuid.setTextContent(DomHelper.base64RandomUuid());
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    @Override
    public SerializableDatabase load(InputStream inputStream) throws IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputStream);

            // we need to decrypt all protected fields
            NodeList protectedContent = (NodeList) DomHelper.xpath.evaluate("//*[@Protected='True']", doc, XPathConstants.NODESET);
            for (int i = 0; i < protectedContent.getLength(); i++){
                Element element = ((Element) protectedContent.item(i));
                String base64 = DomHelper.getElementContent(".", element);
                // Android compatibility
                byte[] encrypted = Base64.decodeBase64(base64.getBytes());
                String decrypted = new String(encryption.decrypt(encrypted), StandardCharsets.UTF_8);
                DomHelper.setElementContent(".", element, decrypted);
                element.removeAttribute("Protected");
                element.setAttribute("kpj2-ProtectOnOutput", "True");
            }
            return this;

        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Instantiating Document Builder", e);
        } catch (SAXException e) {
            throw new IllegalStateException("Parsing exception", e);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("XPath Exception", e);
        }
    }

    @Override
    public void addBinary(int index, byte[] payload) {
        DomHelper.addBinary(doc.getDocumentElement(), Helpers.encodeBase64Content(payload, true),index);
    }

    @Override
    public byte[] getBinary(int index) {
        return Helpers.decodeBase64Content(DomHelper.getBinary(doc.getDocumentElement(), index).getBytes(StandardCharsets.UTF_8), true);
    }

    @Override
    public int getBinaryCount() {
        return DomHelper.getBinaryCount(doc.getDocumentElement());
    }

    @Override
    public void save(OutputStream outputStream) {
        // make a copy so we can mess with content
        Document copyDoc = (Document) doc.cloneNode(true);
        try {
            // check whether protection is required by default and if so mark the element with @@kpj2-ProtectOnOutput='True'
            prepareProtection(copyDoc, "Title");
            prepareProtection(copyDoc, "UserName");
            prepareProtection(copyDoc, "Password");
            prepareProtection(copyDoc, "Notes");
            prepareProtection(copyDoc, "URL");

            // encrypt and base64 every element marked as protected
            NodeList protectedContent = (NodeList) DomHelper.xpath.evaluate("//*[@kpj2-ProtectOnOutput='True']", copyDoc, XPathConstants.NODESET);
            for (int i = 0; i < protectedContent.getLength(); i++){
                Element element = ((Element) protectedContent.item(i));

                element.removeAttribute("kpj2-ProtectOnOutput");
                element.setAttribute("Protected", "True");

                String decrypted = DomHelper.getElementContent(".", element);
                if (decrypted == null) {
                    decrypted = "";
                }
                byte[] encrypted = encryption.encrypt(decrypted.getBytes());
                // Android compatibility
                String base64 = new String(Base64.encodeBase64(encrypted));
                DomHelper.setElementContent(".", element, base64);
            }

            // we need to serialise dates according to the format, and
            // we are going to say that anything that is a date is called *Changed or *Time
            // because that does work in the schema, but won't work for custom values
            NodeList timeBasedContent = (NodeList) DomHelper.xpath.evaluate(
                    //"//*[ends-with(name(), 'Changed')] | //*[ends-with(name(), 'Time')]",
                    // dealing with XPath1
                    "//*[substring(name(),string-length(name())-6) = 'Changed'] | //*[substring(name(),string-length(name())-3) = 'Time']",
                    copyDoc,
                    XPathConstants.NODESET);
            for (int i = 0; i < timeBasedContent.getLength(); i++){
                Element element = ((Element) timeBasedContent.item(i));
                String time = DomHelper.getElementContent(".", element);
                Date date;
                if (time.equals("${creationDate}")) {
                    date = Date.from(Instant.now());
                } else {
                    date = Helpers.toDate(time);
                }
                String encoded = Helpers.fromDate(date);
                DomHelper.setElementContent(".", element, encoded);
            }

        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }

        Source xmlSource = new DOMSource(copyDoc);
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            //factory.setAttribute("indent-number", "4");
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //noinspection HttpUrlsUsage
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlSource, new StreamResult(outputStream));
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String protectQuery = "//Meta/MemoryProtection/Protect%s";
    private static final String pattern = "//String/Key[text()='%s']/following-sibling::Value";
    private void prepareProtection(Document doc, String protect) throws XPathExpressionException {
        // does this require encryption
        String query = String.format(protectQuery, protect);
        if (!((String) DomHelper.xpath.evaluate(query, doc, XPathConstants.STRING)).equalsIgnoreCase("true")) {
            return;
        }
        // mark the field as Protected but don't actually encrypt yet, that comes later
        String path = String.format(pattern, protect);
        NodeList nodelist = (NodeList) DomHelper.xpath.evaluate(path, doc, XPathConstants.NODESET);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Element element = (Element) nodelist.item(i);
            element.setAttribute("kpj2-ProtectOnOutput", "True");
        }
    }

    @Override
    public byte[] getHeaderHash() {
        try {
            String base64 = (String) DomHelper.xpath.evaluate("//HeaderHash", doc, XPathConstants.STRING);
            // Android compatibility
            return Base64.decodeBase64(base64.getBytes());
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Can't get header hash", e);
        }
    }

    @Override
    public void setHeaderHash(byte[] hash) {
        // Android compatibility
        String base64String = new String(Base64.encodeBase64(hash));
        DomHelper.ensureElementContent("Meta/HeaderHash", doc.getDocumentElement(), base64String);
        //((Element) DomHelper.xpath.evaluate("//HeaderHash", doc, XPathConstants.NODE)).setTextContent(base64String);
    }


    @Override
    public StreamEncryptor getEncryption() {
        return encryption;
    }

    @Override
    public void setEncryption(StreamEncryptor encryption) {
        this.encryption = encryption;
    }

    public Document getDoc() {
        return doc;
    }
}
