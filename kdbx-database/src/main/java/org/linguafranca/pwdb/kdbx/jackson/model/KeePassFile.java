/*
 * Copyright 2023 Giuseppe Valente
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

package org.linguafranca.pwdb.kdbx.jackson.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.jackson.KdbxGroup;
import org.linguafranca.pwdb.kdbx.jackson.converter.Base64ToByteConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.Base64ToUUIDConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.BooleanToStringConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.ByteToBase64Converter;
import org.linguafranca.pwdb.kdbx.jackson.converter.DateToStringConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.StringToBooleanConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.StringToDateConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.UUIDToBase64Converter;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "KeePassFile")
public class KeePassFile {

    @JacksonXmlProperty(localName = "Meta")
    public Meta meta;
    @JacksonXmlProperty(localName = "Root")
    public Root root;

    public void createBinaries() {
        meta.binaries = new ArrayList<>();
    }

    @JsonPropertyOrder({
        "group",
        "deletedObject"
    })
    public static class Root {

        public Root() {
        }

        @JacksonXmlProperty(localName = "Group")
        public KdbxGroup group;

        @JacksonXmlProperty(localName = "DeletedObjects")
        protected Object deletedObject;

    }

    @JsonPropertyOrder({
        "generator",
        "headerHash",
        "databaseName",
        "databaseNameChanged",
        "databaseDescription",
        "databaseDescriptionChanged",
        "defaultUserName",
        "defaultUserNameChanged",
        "maintenanceHistoryDays",
        "color",
        "masterKeyChanged",
        "masterKeyChangeRec",
        "masterKeyChangeForce",
        "memoryProtection",
        "customIcons",
        "recycleBinEnabled",
        "recycleBinUUID",
        "recycleBinChanged",
        "entryTemplatesGroup",
        "entryTemplatesGroupChanged",
        "lastSelectedGroup",
        "lastTopVisibleGroup",
        "historyMaxItems",
        "historyMaxSize",
        "binaries",
    })
    public static class Meta {

        public Meta() {
        }

        @JacksonXmlProperty(localName = "Generator")
        public String generator;

        @JacksonXmlProperty(localName = "HeaderHash")
        @JsonDeserialize(converter = Base64ToByteConverter.class)
        @JsonSerialize(converter = ByteToBase64Converter.class)
        public byte[] headerHash;

        @JacksonXmlProperty(localName = "DatabaseName")
        public String databaseName;

        @JacksonXmlProperty(localName = "DatabaseNameChanged")
        @JsonDeserialize(converter = StringToDateConverter.class)
        @JsonSerialize(converter = DateToStringConverter.class)
        public Date databaseNameChanged;

        @JacksonXmlProperty(localName = "DatabaseDescription")
        public String databaseDescription;

        @JacksonXmlProperty(localName = "DatabaseDescriptionChanged")
        @JsonDeserialize(converter = StringToDateConverter.class)
        @JsonSerialize(converter = DateToStringConverter.class)
        public Date databaseDescriptionChanged;

        @JacksonXmlProperty(localName = "DefaultUserName")
        protected String defaultUserName;

        @JacksonXmlProperty(localName = "DefaultUserNameChanged")
        @JsonDeserialize(converter = StringToDateConverter.class)
        @JsonSerialize(converter = DateToStringConverter.class)
        protected Date defaultUserNameChanged;

        @JacksonXmlProperty(localName = "MaintenanceHistoryDays")
        protected int maintenanceHistoryDays;

        @JacksonXmlProperty(localName = "Color")
        protected String color;

        @JacksonXmlProperty(localName = "MasterKeyChanged")
        @JsonDeserialize(converter = StringToDateConverter.class)
        @JsonSerialize(converter = DateToStringConverter.class)
        protected Date masterKeyChanged;

        @JacksonXmlProperty(localName = "MasterKeyChangeRec")
        protected int masterKeyChangeRec;

        @JacksonXmlProperty(localName = "MasterKeyChangeForce")
        protected int masterKeyChangeForce;

        @JacksonXmlProperty(localName = "MemoryProtection")
        public KeePassFile.MemoryProtection memoryProtection;

        @JacksonXmlElementWrapper(localName = "CustomIcons")
        protected ArrayList<Icon> customIcons;

        @JacksonXmlProperty(localName = "RecycleBinEnabled")
        @JsonDeserialize(converter = StringToBooleanConverter.class)
        @JsonSerialize(converter = BooleanToStringConverter.class)
        public Boolean recycleBinEnabled;

        @JacksonXmlProperty(localName = "RecycleBinUUID")
        @JsonDeserialize(converter = Base64ToUUIDConverter.class)
        @JsonSerialize(converter = UUIDToBase64Converter.class)
        public UUID recycleBinUUID;

        @JacksonXmlProperty(localName = "RecycleBinChanged")
        @JsonDeserialize(converter = StringToDateConverter.class)
        @JsonSerialize(converter = DateToStringConverter.class)
        public Date recycleBinChanged;

        @JacksonXmlProperty(localName = "EntryTemplatesGroup")
        @JsonDeserialize(converter = Base64ToUUIDConverter.class)
        @JsonSerialize(converter = UUIDToBase64Converter.class)
        protected UUID entryTemplatesGroup;

        @JacksonXmlProperty(localName = "EntryTemplatesGroupChanged")
        @JsonDeserialize(converter = StringToDateConverter.class)
        @JsonSerialize(converter = DateToStringConverter.class)
        protected Date entryTemplatesGroupChanged;

        @JacksonXmlProperty(localName = "LastSelectedGroup")
        @JsonDeserialize(converter = Base64ToUUIDConverter.class)
        @JsonSerialize(converter = UUIDToBase64Converter.class)
        protected UUID lastSelectedGroup;

        @JacksonXmlProperty(localName = "LastTopVisibleGroup")
        @JsonDeserialize(converter = Base64ToUUIDConverter.class)
        @JsonSerialize(converter = UUIDToBase64Converter.class)
        protected UUID lastTopVisibleGroup;

        @JacksonXmlProperty(localName = "HistoryMaxItems")
        protected int historyMaxItems;

        @JacksonXmlProperty(localName = "HistoryMaxSize")
        protected int historyMaxSize;

        @JacksonXmlProperty(localName = "Binaries")
        public List<Binary> binaries;
        
        @JacksonXmlProperty(localName = "CustomData")
        protected KeePassFile.CustomData customData;

        /* version 4  */

        @JacksonXmlProperty(localName = "SettingsChanged")
        @JsonDeserialize(converter = StringToDateConverter.class)
        @JsonSerialize(converter = DateToStringConverter.class)
        protected Date settingsChanged;
    }

    @JsonPropertyOrder({
        "protectTitle",
        "protectUserName",
        "protectPassword",
        "protectURL",
        "protectNotes"
    })
    public static class MemoryProtection {
        public MemoryProtection() {
        }

        @JacksonXmlProperty(localName = "ProtectTitle")
        @JsonDeserialize(converter = StringToBooleanConverter.class)
        @JsonSerialize(converter = BooleanToStringConverter.class)
        protected Boolean protectTitle;

        @JacksonXmlProperty(localName = "ProtectUserName")
        @JsonDeserialize(converter = StringToBooleanConverter.class)
        @JsonSerialize(converter = BooleanToStringConverter.class)
        protected Boolean protectUserName;

        @JacksonXmlProperty(localName = "ProtectPassword")
        @JsonDeserialize(converter = StringToBooleanConverter.class)
        @JsonSerialize(converter = BooleanToStringConverter.class)
        protected Boolean protectPassword;

        @JacksonXmlProperty(localName = "ProtectURL")
        @JsonDeserialize(converter = StringToBooleanConverter.class)
        @JsonSerialize(converter = BooleanToStringConverter.class)
        protected Boolean protectURL;

        @JacksonXmlProperty(localName = "ProtectNotes")
        @JsonDeserialize(converter = StringToBooleanConverter.class)
        @JsonSerialize(converter = BooleanToStringConverter.class)
        protected Boolean protectNotes;

        public Boolean shouldProtect(String name) {
            switch (name) {
                case Entry.STANDARD_PROPERTY_NAME_TITLE:
                    return protectTitle;
                case Entry.STANDARD_PROPERTY_NAME_USER_NAME:
                    return protectUserName;
                case Entry.STANDARD_PROPERTY_NAME_PASSWORD:
                    return protectPassword;
                case Entry.STANDARD_PROPERTY_NAME_URL:
                    return protectURL;
                case Entry.STANDARD_PROPERTY_NAME_NOTES:
                    return protectNotes;
            }
            return false;
        }
    }

    public static class Binaries {
        public Binaries() {
        }
    }

    public static class Binary {

        public Binary() {
        }

        @JacksonXmlText
        protected String value;

        @JacksonXmlProperty(localName = "ID", isAttribute = true)
        protected Integer id;

        @JacksonXmlProperty(localName = "Compressed", isAttribute = true)
        @JsonDeserialize(converter = StringToBooleanConverter.class)
        @JsonSerialize(converter = BooleanToStringConverter.class)
        protected Boolean compressed;

        public String getValue() {
            return value;
        }

        public Boolean getCompressed() {
            return compressed;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer Id) {
            this.id = Id;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setCompressed(boolean compressed) {
            this.compressed = compressed;
        }
    }

    public static class Icon {
        public Icon() {
        }

        @JacksonXmlProperty(localName = "UUID")
        @JsonDeserialize(converter = Base64ToUUIDConverter.class)
        @JsonSerialize(converter = UUIDToBase64Converter.class)
        protected UUID uuid;

        @JacksonXmlProperty(localName = "Name")
        protected String name;
        
        @JacksonXmlProperty(localName = "LastModificationTime")
        @JsonDeserialize(converter = StringToDateConverter.class)
        @JsonSerialize(converter = DateToStringConverter.class)
        protected Date lastModificationTime;
        
        @JacksonXmlProperty(localName = "Data")
        @JsonDeserialize(converter = Base64ToByteConverter.class)
        @JsonSerialize(converter = ByteToBase64Converter.class)
        protected byte[] data;

        public String getName() {
            return uuid.toString();
        }
    }

    public static class CustomData {

        public CustomData() {
        }

        public List<Object> getAny() {
            return any;
        }

        public void setAny(List<Object> any) {
            this.any = any;
        }

        protected List<Object> any;
    }
}
