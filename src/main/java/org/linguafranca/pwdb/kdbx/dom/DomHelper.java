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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.bind.DatatypeConverter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The class contains static helper methods for access to the underlying XML DOM
 *
 * @author jo
 */
public class DomHelper {

    static XPath xpath = XPathFactory.newInstance().newXPath();

    static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static final String GROUP_ELEMENT_NAME = "Group";
    static final String ENTRY_ELEMENT_NAME = "Entry";
    static final String ICON_ELEMENT_NAME = "IconID";
    static final String UUID_ELEMENT_NAME = "UUID";
    static final String NAME_ELEMENT_NAME = "Name";
    static final String NOTES_ELEMENT_NAME = "Notes";
    static final String TIMES_ELEMENT_NAME = "Times";
    static final String IS_EXPANDED = "IsExpanded";

    static final String HISTORY_ELEMENT_NAME = "History";

    static final String LAST_MODIFICATION_TIME_ELEMENT_NAME = "Times/LastModificationTime";
    static final String CREATION_TIME_ELEMENT_NAME = "Times/CreationTime";
    static final String LAST_ACCESS_TIME_ELEMENT_NAME = "Times/LastAccessTime";
    static final String EXPIRY_TIME_ELEMENT_NAME = "Times/ExpiryTime";
    static final String EXPIRES_ELEMENT_NAME = "Times/Expires";
    static final String USAGE_COUNT_ELEMENT_NAME = "Times/UsageCount";
    static final String LOCATION_CHANGED = "Times/LocationChanged";

    static final String PROPERTY_ELEMENT_FORMAT = "String[Key/text()='%s']";
    static final String VALUE_ELEMENT_NAME = "Value";

    interface ValueCreator {
        String getValue();
    }

    static class ConstantValueCreator implements ValueCreator {
        String value;
        ConstantValueCreator(String value) {
            this.value = value;
        }
        @Override
        public String getValue() {
            return value;
        }
    }

    static class DateValueCreator implements ValueCreator {
        @Override
        public String getValue() {
            return dateFormatter.format(new Date());
        }
    }

    static class UuidValueCreator implements ValueCreator {
        @Override
        public String getValue() {
            return base64RandomUuid();
        }

    }

    static void ensureElements (Element element, Map<String, ValueCreator> childElements) {
        for (Map.Entry<String, ValueCreator> entry: childElements.entrySet()) {
            ensureElementContent(entry.getKey(), element, entry.getValue().getValue());
        }
    }


    @Nullable @Contract("_,_,true -> !null")
    static  Element getElement(String elementPath, Element parentElement, boolean create) {
        try {
            Element result = (Element) xpath.evaluate(elementPath, parentElement, XPathConstants.NODE);
            if (result == null && create) {
                result = createHierarchically(elementPath, parentElement);
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    static List<Element> getElements (String elementPath, Element parentElement) {
        try {
            NodeList nodes = (NodeList) xpath.evaluate(elementPath, parentElement, XPathConstants.NODESET);
            ArrayList<Element> result = new ArrayList<>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(((Element) nodes.item(i)));
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    static int getElementsCount (String elementPath, Element parentElement) {
        try {
            NodeList nodes = (NodeList) xpath.evaluate(elementPath, parentElement, XPathConstants.NODESET);
            return nodes.getLength();
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    static Element newElement(String elementName, Element parentElement) {
        Element newElement = parentElement.getOwnerDocument().createElement(elementName);
        parentElement.appendChild(newElement);
        return newElement;
    }

    @Nullable
    static String getElementContent(String elementPath, Element parentElement) {
        Element result = getElement(elementPath, parentElement, false);
        return (result == null) ? null : result.getTextContent();
    }

    @NotNull
    static String ensureElementContent(String elementPath, Element parentElement, @NotNull String value) {
        Element result = getElement(elementPath, parentElement, false);
        if (result == null) {
            result = createHierarchically(elementPath, parentElement);
            result.setTextContent(value);
        }
        return result.getTextContent();
    }

    @NotNull
    static Element setElementContent(String elementPath, Element parentElement, String value) {
        Element result = getElement(elementPath, parentElement, true);
        result.setTextContent(value);
        return result;
    }

    @NotNull
    static Element touchElement(String elementPath, Element parentElement) {
        return setElementContent(elementPath, parentElement, dateFormatter.format(new Date()));
    }

    private static Element createHierarchically(String elementPath, Element startElement) {
        Element currentElement = startElement;
        for (String elementName : elementPath.split("/")) {
            try {
                Element nextElement = (Element) xpath.evaluate(elementName, currentElement, XPathConstants.NODE);
                if (nextElement == null) {
                    nextElement = (Element) currentElement.appendChild(currentElement.getOwnerDocument().createElement(elementName));
                }
                currentElement = nextElement;
            } catch (XPathExpressionException e) {
                throw new IllegalStateException(e);
            }
        }
        return currentElement;
    }

    static String base64RandomUuid () {
        return base64FromUuid(UUID.randomUUID());
    }

    static String base64FromUuid(UUID uuid) {
        byte[] buffer = new byte[16];
        ByteBuffer b = ByteBuffer.wrap(buffer);
        b.putLong(uuid.getMostSignificantBits());
        b.putLong(8, uuid.getLeastSignificantBits());
        return DatatypeConverter.printBase64Binary(buffer);
    }

    static String hexStringFromUuid(UUID uuid) {
        byte[] buffer = new byte[16];
        ByteBuffer b = ByteBuffer.wrap(buffer);
        b.putLong(uuid.getMostSignificantBits());
        b.putLong(8, uuid.getLeastSignificantBits());
        return DatatypeConverter.printHexBinary(buffer);
    }

    static String hexStringFromBase64(String base64) {
        byte[] buffer = DatatypeConverter.parseBase64Binary(base64);
        return DatatypeConverter.printHexBinary(buffer);
    }

    static UUID uuidFromBase64(String base64) {
        byte[] buffer = DatatypeConverter.parseBase64Binary(base64);
        ByteBuffer b = ByteBuffer.wrap(buffer);
        return new UUID(b.getLong(), b.getLong(8));
    }
}
