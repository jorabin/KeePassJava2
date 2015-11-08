package org.linguafranca.keepass.dom;

import org.linguafranca.keepass.DatabaseProvider;
import org.linguafranca.keepass.encryption.Salsa20Encryption;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;

import static org.linguafranca.keepass.dom.DomHelper.*;

/**
 * @author jo
 */
public class DomKeepassDatabase implements DatabaseProvider {

    private Document doc;
    private Encryption encryption;

    private DomKeepassDatabase() {}

    public static DomKeepassDatabase createEmptyDatabase() throws IOException {
        DomKeepassDatabase result = new DomKeepassDatabase();
        // read in the template keepass xml database
        result.load(result.getClass().getClassLoader().getResourceAsStream("base.kdbx.xml"));
        try {
            // replace all placeholder dates with now
            String now = dateFormatter.format(new Date());
            NodeList list = (NodeList) xpath.evaluate("//*[contains(text(),'${creationDate}')]", result.doc.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < list.getLength(); i++) {
                list.item(i).setTextContent(now);
            }
            // set the root group UUID
            Node uuid = (Node) xpath.evaluate("//"+ UUID_ELEMENT_NAME, result.doc.getDocumentElement(), XPathConstants.NODE);
            uuid.setTextContent(base64RandomUuid());
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
        result.setEncryption(new Salsa20Encryption(SecureRandom.getSeed(32)));
        return result;
    }

    @Override
    public DatabaseProvider load(InputStream inputStream) throws IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputStream);

            // we need to decrypt all protected fields
            // TODO we assume they are all strings, which is wrong
            NodeList protectedContent = (NodeList) xpath.evaluate("//*[@Protected='True']", doc, XPathConstants.NODESET);
            for (int i = 0; i < protectedContent.getLength(); i++){
                Element element = ((Element) protectedContent.item(i));
                String base64 = getElementContent(".", element);
                byte[] encrypted = DatatypeConverter.parseBase64Binary(base64);
                String decrypted = new String(encryption.decrypt(encrypted), "UTF-8");
                setElementContent(".", element, decrypted);
                element.removeAttribute("Protected");
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
    public void save(OutputStream outputStream) {

        try {
            // check whether protection is required and if so mark the element with @Protected='True'
            prepareProtection("Title");
            prepareProtection("UserName");
            prepareProtection("Password");
            prepareProtection("Notes");
            prepareProtection("URL");

            // encrypt and base64 every element marked as protected
            NodeList protectedContent = (NodeList) xpath.evaluate("//*[@Protected='True']", doc, XPathConstants.NODESET);
            for (int i = 0; i < protectedContent.getLength(); i++){
                Element element = ((Element) protectedContent.item(i));
                String decrypted = getElementContent(".", element);
                if (decrypted == null) {
                    decrypted = "";
                }
                byte[] encrypted = encryption.encrypt(decrypted.getBytes());
                String base64 = DatatypeConverter.printBase64Binary(encrypted);
                setElementContent(".", element, base64);
            }

        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }

        Source xmlSource = new DOMSource(doc);
        Result outputTarget = new StreamResult(outputStream);
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            //factory.setAttribute("indent-number", "4");
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlSource, outputTarget);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String protectQuery = "//Meta/MemoryProtection/Protect%s";
    private static final String pattern = "//String/Key[text()='%s']/following-sibling::Value";
    private void prepareProtection(String protect) throws XPathExpressionException {
        // does this require encryption
        String query = String.format(protectQuery, protect);
        if (!((String) xpath.evaluate(query, doc, XPathConstants.STRING)).toLowerCase().equals("true")) {
            return;
        }
        // mark the field as Protected but don't actually encrypt yet, that comes later
        String path = String.format(pattern, protect);
        NodeList nodelist = (NodeList) xpath.evaluate(path, doc, XPathConstants.NODESET);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Element element = (Element) nodelist.item(i);
            element.setAttribute("Protected", "True");
        }
    }

    @Override
    public void setHeaderHash(MessageDigest messageDigest) {
        String base64String = DatatypeConverter.printBase64Binary(messageDigest.digest());
        try {
            ((Element) xpath.evaluate("//HeaderHash", doc, XPathConstants.NODE)).setTextContent(base64String);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Can't set header hash", e);
        }
    }

    @Override
    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    public Document getDoc() {
        return doc;
    }

    @Override
    public Encryption getEncryption() {
        return encryption;
    }
}
