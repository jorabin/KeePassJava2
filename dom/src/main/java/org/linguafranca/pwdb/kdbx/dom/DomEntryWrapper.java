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

import org.linguafranca.pwdb.base.AbstractEntry;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.w3c.dom.Element;

import java.text.ParseException;
import java.util.*;

/**
 * Class wraps Entries from a {@link DomSerializableDatabase} as {@link org.linguafranca.pwdb.Entry}
 *
 * @author jo
 */
public class DomEntryWrapper extends AbstractEntry <DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper>{

    private static Map<String, DomHelper.ValueCreator> mandatoryEntryElements = new HashMap<String, DomHelper.ValueCreator>() {{
        put(DomHelper.UUID_ELEMENT_NAME, new DomHelper.UuidValueCreator());
        put(DomHelper.ICON_ELEMENT_NAME, new DomHelper.ConstantValueCreator("2"));
        put(DomHelper.TIMES_ELEMENT_NAME, new DomHelper.ConstantValueCreator(""));
        put(DomHelper.LAST_MODIFICATION_TIME_ELEMENT_NAME, new DomHelper.DateValueCreator());
        put(DomHelper.CREATION_TIME_ELEMENT_NAME, new DomHelper.DateValueCreator());
        put(DomHelper.LAST_ACCESS_TIME_ELEMENT_NAME, new DomHelper.DateValueCreator());
        put(DomHelper.EXPIRY_TIME_ELEMENT_NAME, new DomHelper.DateValueCreator());
        put(DomHelper.EXPIRES_ELEMENT_NAME, new DomHelper.ConstantValueCreator("False"));
        put(DomHelper.USAGE_COUNT_ELEMENT_NAME, new DomHelper.ConstantValueCreator("0"));
        put(DomHelper.LOCATION_CHANGED, new DomHelper.DateValueCreator());
    }};

    final Element element;
    private final DomDatabaseWrapper database;

    public DomEntryWrapper(Element element, DomDatabaseWrapper database, boolean newElement) {
        this.element = element;
        this.database = database;
        if (newElement) {
            DomHelper.ensureElements(element, mandatoryEntryElements);
            ensureProperty("Notes");
            ensureProperty("Title");
            ensureProperty("URL");
            ensureProperty("UserName");
            ensureProperty("Password");
        }
    }

    @Override
    public String getProperty(String name) {
        Element property = DomHelper.getElement(String.format(DomHelper.PROPERTY_ELEMENT_FORMAT, name), element, false);
        if (property == null) {
            return null;
        }
        return DomHelper.getElementContent(DomHelper.VALUE_ELEMENT_NAME, property);
    }

    @Override
    public void setProperty(String name, String value) {
        Element property = DomHelper.getElement(String.format(DomHelper.PROPERTY_ELEMENT_FORMAT, name), element, false);
        if (property == null) {
            property = DomHelper.newElement("String", element);
            DomHelper.setElementContent("Key", property, name);
        }
        DomHelper.setElementContent(DomHelper.VALUE_ELEMENT_NAME, property, value);
        DomHelper.touchElement(DomHelper.LAST_MODIFICATION_TIME_ELEMENT_NAME, element);
        database.setDirty(true);
    }

    @Override
    public List<String> getPropertyNames() {
        ArrayList<String> result = new ArrayList<>();
        List<Element> list = DomHelper.getElements("String", element);
        for (Element listElement: list) {
            result.add(DomHelper.getElementContent("Key", listElement));
        }
        return result;
    }

    @Override
    public byte[] getBinaryProperty(String name) {
        Element property = DomHelper.getElement(String.format(DomHelper.BINARY_PROPERTY_ELEMENT_FORMAT, name), element, false);
        if (property == null) {
            return null;
        }
        return DomHelper.getBinaryElementContent(DomHelper.VALUE_ELEMENT_NAME, property);
    }

    @Override
    public void setBinaryProperty(String name, byte[] value) {
        Element property = DomHelper.getElement(String.format(DomHelper.BINARY_PROPERTY_ELEMENT_FORMAT, name), element, false);
        if (property == null) {
            property = DomHelper.newElement("Binary", element);
            DomHelper.setElementContent("Key", property, name);
        }
        DomHelper.setBinaryElementContent(DomHelper.VALUE_ELEMENT_NAME, property, value);
        DomHelper.touchElement(DomHelper.LAST_MODIFICATION_TIME_ELEMENT_NAME, element);
        database.setDirty(true);

    }

    @Override
    public List<String> getBinaryPropertyNames() {
        ArrayList<String> result = new ArrayList<>();
        List<Element> list = DomHelper.getElements("Binary", element);
        for (Element listElement: list) {
            result.add(DomHelper.getElementContent("Key", listElement));
        }
        return result;
    }

    private void ensureProperty(String name){
        Element property = DomHelper.getElement(String.format(DomHelper.PROPERTY_ELEMENT_FORMAT, name), element, false);
        if (property == null) {
            Element container = DomHelper.newElement("String", element);
            DomHelper.setElementContent("Key", container, name);
            DomHelper.getElement("Value", container, true);
        }
    }

    @Override
    public DomGroupWrapper getParent() {
        if (element.getParentNode() == null) {
            return null;
        }
        return new DomGroupWrapper((Element) element.getParentNode(), database, false);
    }

    @Override
    public UUID getUuid() {
        return Helpers.uuidFromBase64(DomHelper.getElementContent(DomHelper.UUID_ELEMENT_NAME, element));
    }

    @Override
    public DomIconWrapper getIcon() {
        return new DomIconWrapper(DomHelper.getElement(DomHelper.ICON_ELEMENT_NAME, element, false));
    }

    @Override
    public void setIcon(DomIconWrapper icon) {
        DomHelper.getElement(DomHelper.ICON_ELEMENT_NAME, element, true).setTextContent(String.valueOf(icon.getIndex()));
        DomHelper.touchElement(DomHelper.LAST_MODIFICATION_TIME_ELEMENT_NAME, element);
        database.setDirty(true);
    }

    @Override
    public Date getLastAccessTime() {
        try {
            return DomHelper.dateFormatter.parse(DomHelper.getElementContent(DomHelper.LAST_ACCESS_TIME_ELEMENT_NAME, element));
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    public Date getCreationTime() {
        try {
            return DomHelper.dateFormatter.parse(DomHelper.getElementContent(DomHelper.CREATION_TIME_ELEMENT_NAME, element));
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    public boolean getExpires() {
        String content = DomHelper.getElementContent(DomHelper.EXPIRES_ELEMENT_NAME, element);
        return content != null && content.equalsIgnoreCase("true");
    }

    @Override
    public Date getExpiryTime() {
        try {
            return DomHelper.dateFormatter.parse(DomHelper.getElementContent(DomHelper.EXPIRY_TIME_ELEMENT_NAME, element));
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    /**
     * Sets the expiration time for this Entry.
     * If <i>expiryTime</i> is null, this Entry is set to not expire.
     */
    @Override
    public void setExpires(Date expiryTime) {
        if (expiryTime == null) {
            DomHelper.getElement(DomHelper.EXPIRES_ELEMENT_NAME, element, false).setTextContent("False");
        } else {
            DomHelper.getElement(DomHelper.EXPIRES_ELEMENT_NAME, element, false).setTextContent("True");
            String formattedDate = DomHelper.dateFormatter.format(expiryTime);
            DomHelper.getElement(DomHelper.EXPIRY_TIME_ELEMENT_NAME, element, false).setTextContent(formattedDate);
        }

        DomHelper.touchElement(DomHelper.LAST_MODIFICATION_TIME_ELEMENT_NAME, element);
        database.setDirty(true);
    }

    @Override
    public Date getLastModificationTime() {
        try {
            return DomHelper.dateFormatter.parse(DomHelper.getElementContent(DomHelper.LAST_MODIFICATION_TIME_ELEMENT_NAME, element));
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    protected void touch() {
        DomHelper.setElementContent(DomHelper.LAST_MODIFICATION_TIME_ELEMENT_NAME, element, DomHelper.dateFormatter.format(new Date()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomEntryWrapper that = (DomEntryWrapper) o;

        return element.equals(that.element) && database.equals(that.database);

    }
}
