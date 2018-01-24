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
import org.linguafranca.pwdb.kdbx.Helpers;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//import javax.xml.bind.DatatypeConverter;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The class contains static helper methods for access to the underlying XML DOM
 *
 * @author jo
 */
class DomHelper {

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
    static final String BINARY_PROPERTY_ELEMENT_FORMAT = "Binary[Key/text()='%s']";
    static final String VALUE_ELEMENT_NAME = "Value";

    static final String RECYCLE_BIN_UUID_ELEMENT_NAME = "RecycleBinUuid";
    static final String RECYCLE_BIN_ENABLED_ELEMENT_NAME = "RecycleBinEnabled";
    static final String RECYCLE_BIN_CHANGED_ELEMENT_NAME = "RecycleBinChanged";

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

    static boolean removeElement(String elementPath, Element parentElement) {
        Element toRemove = getElement(elementPath, parentElement, false);
        if (toRemove == null) {
            return false;
        } else {
            toRemove.getParentNode().removeChild(toRemove);
            return true;
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

    @Nullable
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

    @Nullable
    static byte[] getBinaryElementContent(String elementPath, Element parentElement) {
        Element result = getElement(elementPath, parentElement, false);
        if (result == null) {
            return null;
        }
        String id = result.getAttribute("Ref");
        Element content = getElement("//Binaries/Binary[@ID=" + id + "]", parentElement.getOwnerDocument().getDocumentElement(),false);
        if (content == null) {
            throw new IllegalStateException("Could not find binary content with ID " + id);
        }
        return Helpers.decodeBase64Content(content.getTextContent().getBytes(), content.hasAttribute("Compressed"));
    }

    @NotNull
    static Element setBinaryElementContent(String elementPath, Element parentElement, byte[] value) {
        try {
            String b64 = Helpers.encodeBase64Content(value, true);

            //Find the highest numbered existing content
            String max = xpath.evaluate("//Binaries/Binary/@ID[not(. < ../../Binary/@ID)][1]", parentElement.getOwnerDocument().getDocumentElement());
            Integer newIndex = Integer.valueOf(max) + 1;

            Element binaries = getElement("//Binaries", parentElement.getOwnerDocument().getDocumentElement(),false);
            if (binaries == null) {
                throw new IllegalStateException("Binaries not found");
            }
            Element binary = (Element) binaries.appendChild(binaries.getOwnerDocument().createElement("Binary"));
            binary.setTextContent(b64);
            binary.setAttribute("Compressed", "True");
            binary.setAttribute("ID", newIndex.toString());

            Element result = getElement(elementPath, parentElement, true);
            result.setAttribute("Ref", newIndex.toString());


            return result;

        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
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
        return Helpers.base64FromUuid(UUID.randomUUID());
    }

}
