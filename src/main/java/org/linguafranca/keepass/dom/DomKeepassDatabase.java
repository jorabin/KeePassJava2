package org.linguafranca.keepass.dom;

import org.linguafranca.keepass.db.DatabaseProvider;
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
            NodeList list = (NodeList) xpath.evaluate("//*[contains(text(),'${creationDate}')]",result.doc.getDocumentElement(), XPathConstants.NODESET);
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
            return this;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Instantiating Document Builder", e);
        } catch (SAXException e) {
            throw new IllegalStateException("Parsing exception", e);
        }
    }

    @Override
    public void save(OutputStream outputStream) {
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
