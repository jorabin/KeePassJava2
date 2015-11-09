package org.linguafranca.db.kdbx;

import org.w3c.dom.Document;

import javax.xml.bind.DatatypeConverter;
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
public class KdbxKeyFile {

    static XPath xpath = XPathFactory.newInstance().newXPath();

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
        return DatatypeConverter.parseBase64Binary(base64);
    }
}
