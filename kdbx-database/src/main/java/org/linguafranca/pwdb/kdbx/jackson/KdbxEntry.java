/*
 * Copyright (c) 2025. Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.linguafranca.pwdb.kdbx.jackson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.abstractdb.AbstractEntry;
import org.linguafranca.pwdb.format.Helpers;
import org.linguafranca.pwdb.kdbx.jackson.converter.Base64ToUUIDConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.BooleanToStringConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.StringToBooleanConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.UUIDToBase64Converter;

import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;
import org.linguafranca.pwdb.kdbx.jackson.model.Times;

import static org.linguafranca.pwdb.kdbx.jackson.model.EntryClasses.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;



@JsonPropertyOrder({
    "uuid",
    "iconID",
    "customIconUUID",
    "foregroundColor",
    "backgroundColor",
    "overrideURL",
    "previousParentGroup",
    "tags",
    "times",
    "string",
    "binary",
    "autoType",
    "customData",
    "history",
})


@JsonIgnoreProperties({"path", "username", "title", "notes", "url", "password"})
public class KdbxEntry extends AbstractEntry {

    @JsonIgnore
    KdbxDatabase database;

    @JsonIgnore
    KdbxGroup parent;

    protected KdbxEntry() {
        string = new ArrayList<>();
        binary = new ArrayList<>();
        times = new Times();
        uuid = UUID.randomUUID();
        iconID = 0;
    }

    public static KdbxEntry createEntry(KdbxDatabase database) {
        KdbxEntry result = new KdbxEntry();
        result.database = database;
        result.parent = null;
        // avoiding setProperty as it does a touch();
        for (String p : STANDARD_PROPERTY_NAMES) {
            result.string.add(new StringProperty(p, database.getPropertyValueStrategy().newUnprotected().of("")));
        }
        return result;
    }


    @JacksonXmlProperty(localName = "UUID")
    @JsonDeserialize(converter = Base64ToUUIDConverter.class)
    @JsonSerialize(converter = UUIDToBase64Converter.class)
    protected UUID uuid;

    @JacksonXmlProperty(localName = "IconID")
    protected int iconID;
    
    @JacksonXmlProperty(localName = "CustomIconUUID")
    @JsonDeserialize(converter = Base64ToUUIDConverter.class)
    @JsonSerialize(converter = UUIDToBase64Converter.class)
    protected UUID customIconUUID;
   
    @JacksonXmlProperty(localName = "ForegroundColor")
    protected String foregroundColor;
    
    @JacksonXmlProperty(localName = "BackgroundColor")
    protected String backgroundColor;
   
    @JacksonXmlProperty(localName = "OverrideURL")
    protected String overrideURL;

    @JacksonXmlProperty(localName = "PreviousParentGroup")
    @JsonDeserialize(converter = Base64ToUUIDConverter.class)
    @JsonSerialize(converter = UUIDToBase64Converter.class)
    protected UUID previousParentGroup;

    @JacksonXmlProperty(localName = "Tags")
    protected String tags;


    @JacksonXmlProperty(localName = "QualityCheck")
    @JsonDeserialize(converter = StringToBooleanConverter.class)
    @JsonSerialize(converter = BooleanToStringConverter.class)
    protected Boolean qualityCheck;

    @JacksonXmlProperty(localName = "Times")
    protected Times times;

    @JacksonXmlProperty(localName = "String") /* Workaround jackson */
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<StringProperty> string;

    @JacksonXmlProperty(localName = "Binary") /* Workaround jackson */
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<BinaryProperty> binary;

    @JacksonXmlProperty(localName = "AutoType")
    protected AutoType autoType;

    @JacksonXmlProperty(localName = "CustomData")
    protected KeePassFile.CustomData customData;

    @JacksonXmlProperty(localName = "History") /* Workaround jackson */
    protected KdbxHistory history;

    @Override
    @JsonIgnore
    public String getProperty(String s) {
        StringProperty sp = getStringProperty(s, string);
        if (sp == null) {
            return null;
        }
        return sp.getValue().getValueAsString();
    }

    @Override
    @JsonIgnore
    public KdbxEntry setProperty(String s, String s1) {
        StringProperty sp = getStringProperty(s, string);
        if (sp != null) {
            sp.setValue(database.getPropertyValueStrategy().newUnprotected().of(s1));
            return this;
        }
        string.add(new StringProperty(s, database.getPropertyValueStrategy().newUnprotected().of(s1)));
        touch();
        return this;
    }

    @Override
    @JsonIgnore
    public PropertyValue getPropertyValue(String name) {
        StringProperty sp = getStringProperty(name, string);
        return sp != null? sp.getValue() : null;
    }

    @Override
    @JsonIgnore
    public KdbxEntry setPropertyValue(String name, PropertyValue value) {
        StringProperty sp = getStringProperty(name, string);
        if (sp != null) {
            sp.setValue(value);
            return this;
        }
        string.add(new StringProperty(name, value));
        touch();
        return this;
    }

    @Override
    @JsonIgnore
    public Entry addProperty(String name, byte[] value){
        PropertyValue.Factory<? extends PropertyValue> f = database.getPropertyValueStrategy().getFactoryFor(name);
        this.setPropertyValue(name, f.of(value));
        return this;
    }

    @Override
    @JsonIgnore
    public Entry addProperty(String name, char[] value){
        PropertyValue.Factory<? extends PropertyValue> f = database.getPropertyValueStrategy().getFactoryFor(name);
        this.setPropertyValue(name, f.of(value));
        return this;
    }

    @Override
    @JsonIgnore
    public Entry addProperty(String name, CharSequence value){
        PropertyValue.Factory<? extends PropertyValue> f = database.getPropertyValueStrategy().getFactoryFor(name);
        this.setPropertyValue(name, f.of(value));
        return this;
    }

    @Override
    @JsonIgnore
    public boolean removeProperty(String name) throws IllegalArgumentException {
        if (STANDARD_PROPERTY_NAMES.contains(name))
            throw new IllegalArgumentException("may not remove property: " + name);

        StringProperty sp = getStringProperty(name, string);
        if (sp == null) {
            return false;
        } else {
            this.string.remove(sp);
            touch();
            return true;
        }
    }

    @Override
    @JsonIgnore
    public List<String> getPropertyNames() {
        List<String> result = new ArrayList<>();
        for (StringProperty property : this.string) {
            result.add(property.getKey());
        }
        return result;
    }

    @Override
    public byte[] getBinaryProperty(String s) {
        BinaryProperty bp = getBinaryProp(s, binary);
        if (bp == null) {
            return null;
        }

        KeePassFile.Binary binary = null;
        for (KeePassFile.Binary b : database.getBinaries()) {
            if (b.getId().equals(Integer.valueOf(getBinaryContent(bp)))) {
                binary = b;
            }
        }
        if (binary == null) {
            return null;
        }
        return Helpers.decodeBase64Content(binary.getValue().getBytes(), binary.getCompressed());
    }

    @Override
    public void setBinaryProperty(String s, byte[] bytes) {
        // remove old binary property with same name
        BinaryProperty bp = getBinaryProp(s, binary);
        if (bp != null) {
            binary.remove(bp);
        }

        // what is the next free index in the binary store?
        Integer max = -1;
        for (KeePassFile.Binary binary : database.getBinaries()) {
            if (binary.getId() > max) {
                max = binary.getId();
            }
        }
        max++;

        database.addBinary(bytes, max);

        // make a reference to it from the entry
        BinaryProperty binaryProperty = new BinaryProperty();
        binaryProperty.setKey(s);
        BinaryProperty.Value fieldValue = new BinaryProperty.Value();
        fieldValue.setRef(String.valueOf(max));
        binaryProperty.setValue(fieldValue);
        binary.add(binaryProperty);
        touch();
    }

    @Override
    @JsonIgnore
    public boolean removeBinaryProperty(String name) throws UnsupportedOperationException {
        BinaryProperty bp = getBinaryProp(name, binary);
        if (bp != null) {
            binary.remove(bp);
            touch();
            return true;
        }
        return false;
    }

    @Override
    @JsonIgnore
    public List<String> getBinaryPropertyNames() {
        List<String> result = new ArrayList<>();
        for (BinaryProperty property : this.binary) {
            result.add(property.getKey());
        }
        return result;
    }

    @Override
    public KdbxGroup getParent() {
        return parent;
    }

    @Override
    public @NotNull UUID getUuid() {
        return uuid;
    }

    @Override
    public KdbxIcon getIcon() {
        return new KdbxIcon(iconID);
    }

    @Override
    public void setIcon(Icon icon) {
        iconID = icon.getIndex();
    }

    @Override
    @JsonIgnore
    public Date getLastAccessTime() {
        return times.getLastAccessTime();
    }

    @Override
    @JsonIgnore
    public Date getCreationTime() {
        return times.getCreationTime();
    }

    @Override
    @JsonIgnore
    public boolean getExpires() {
        return times.getExpires();
    }

    @Override
    @JsonIgnore
    public void setExpires(boolean expires) {
        times.setExpires(expires);
    }

    @Override
    @JsonIgnore
    public Date getExpiryTime() {
        return times.getExpiryTime();
    }

    @Override
    @JsonIgnore
    public void setExpiryTime(Date expiryTime) throws IllegalArgumentException {
        if (expiryTime == null)
            throw new IllegalArgumentException("expiryTime may not be null");
        times.setExpiryTime(expiryTime);
    }

    @Override
    @JsonIgnore
    public Date getLastModificationTime() {
        return times.getLastModificationTime();
    }

    @Override
    @JsonIgnore
    protected void touch() {
        this.times.setLastModificationTime(new Date());
        this.database.setDirty(true);
    }
}
