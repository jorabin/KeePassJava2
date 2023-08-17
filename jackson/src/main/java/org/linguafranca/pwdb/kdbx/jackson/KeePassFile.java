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

package org.linguafranca.pwdb.kdbx.jackson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.jackson.converter.Base64ToByteConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.ByteToBase64Converter;
import org.linguafranca.pwdb.kdbx.jackson.converter.TimeConverter;

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

    public List<Binary> getBinaries() {
        return meta.binaries;
    }

    public void createBinaries() {
        meta.binaries = new ArrayList<>();
    }

    public static class Root {

        public Root() {}

       @JacksonXmlProperty(localName = "Group")
        public JacksonGroup group;

       @JacksonXmlProperty(localName = "DeletedObjects") /** Jackson workaround see https://github.com/FasterXML/jackson-dataformat-xml/issues/181#issuecomment-349411792  **/
       @JacksonXmlElementWrapper(useWrapping = false)
        protected ArrayList<DeletedObject> deletedObjects;

    }

    public static class Meta {

        public Meta() {}
       @JacksonXmlProperty(localName = "Generator")
        public String generator;

       @JacksonXmlProperty(localName = "HeaderHash")
        //public KeePassFile.ByteArray headerHash;
        //public String headerHash;
       @JsonDeserialize(converter = Base64ToByteConverter.class)
       @JsonSerialize(converter = ByteToBase64Converter.class)
       public byte[] headerHash;

       @JacksonXmlProperty(localName = "DatabaseName")
        public String databaseName;
       
        @JacksonXmlProperty(localName = "DatabaseNameChanged")
        @JsonDeserialize(converter = TimeConverter.class)
        public Date databaseNameChanged;

       @JacksonXmlProperty(localName = "DatabaseDescription")
        public String databaseDescription;

       @JacksonXmlProperty(localName = "DatabaseDescriptionChanged")
       @JsonDeserialize(converter = TimeConverter.class)
        public Date databaseDescriptionChanged;

       @JacksonXmlProperty(localName = "DefaultUserName")
        protected String defaultUserName;

       @JacksonXmlProperty(localName = "DefaultUserNameChanged")
       @JsonDeserialize(converter = TimeConverter.class)
        protected Date defaultUserNameChanged;

       @JacksonXmlProperty(localName = "MaintenanceHistoryDays")
        protected int maintenanceHistoryDays;

       @JacksonXmlProperty(localName = "Color")
        protected String color;

       @JacksonXmlProperty(localName = "MasterKeyChanged")
       @JsonDeserialize(converter = TimeConverter.class)
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
       public Boolean recycleBinEnabled;
       
       @JacksonXmlProperty(localName = "RecycleBinUUID")
       public UUID recycleBinUUID;

       @JacksonXmlProperty(localName = "RecycleBinChanged")
       @JsonDeserialize(converter = TimeConverter.class)
        public Date recycleBinChanged;
        
       @JacksonXmlProperty(localName = "EntryTemplatesGroup")
        
        protected UUID entryTemplatesGroup;
       
        @JacksonXmlProperty(localName = "EntryTemplatesGroupChanged")
        @JsonDeserialize(converter = TimeConverter.class) 
        protected Date entryTemplatesGroupChanged;

       @JacksonXmlProperty(localName = "LastSelectedGroup")
        
        protected UUID lastSelectedGroup;
       @JacksonXmlProperty(localName = "LastTopVisibleGroup")
        
        protected UUID lastTopVisibleGroup;
        @JacksonXmlProperty(localName = "HistoryMaxItems")
        protected int historyMaxItems;
        @JacksonXmlProperty(localName = "HistoryMaxSize")
        protected int historyMaxSize;
        
        @JacksonXmlElementWrapper(localName = "Binaries")
        protected List<Binary> binaries;

       @JacksonXmlProperty(localName = "CustomData")
        protected KeePassFile.CustomData customData;

        /* version 4 */

       @JacksonXmlProperty(localName = "SettingsChanged")
       @JsonDeserialize(converter = TimeConverter.class)
       protected Date settingsChanged;
    }


    public static class MemoryProtection {
        public MemoryProtection() {}

       @JacksonXmlProperty(localName = "ProtectTitle")
        
        protected Boolean protectTitle;
       @JacksonXmlProperty(localName = "ProtectUserName")
        
        protected Boolean protectUserName;
       @JacksonXmlProperty(localName = "ProtectPassword")
        
        protected Boolean protectPassword;
       @JacksonXmlProperty(localName = "ProtectURL")
        
        protected Boolean protectURL;
       @JacksonXmlProperty(localName = "ProtectNotes")
        
        protected Boolean protectNotes;

        public Boolean shouldProtect(String name) {
            switch(name) {
                case Entry.STANDARD_PROPERTY_NAME_TITLE: return protectTitle;
                case Entry.STANDARD_PROPERTY_NAME_USER_NAME: return protectUserName;
                case Entry.STANDARD_PROPERTY_NAME_PASSWORD: return protectPassword;
                case Entry.STANDARD_PROPERTY_NAME_URL: return protectURL;
                case Entry.STANDARD_PROPERTY_NAME_NOTES: return protectNotes;
            }
            return false;
        }
    }

    public static class Binaries {
        public Binaries() {}
    }

   @JacksonXmlRootElement(localName = "Binary")
    public static class Binary  {

        public Binary() {}

        @JacksonXmlText
        protected String value;

        @JacksonXmlProperty(localName = "ID", isAttribute = true)
        protected Integer id;

        @JacksonXmlProperty(localName = "Compressed", isAttribute = true)
        protected Boolean compressed;

        public String getName() {
            return String.valueOf(id);
        }

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

    @JacksonXmlRootElement(localName = "Icon")
    public static class Icon  {
        public Icon() {}
       @JacksonXmlProperty(localName = "UUID")
        protected UUID uuid;
       @JacksonXmlProperty(localName = "Name")
        protected String name;
       @JacksonXmlProperty(localName = "LastModificationTime")
       @JsonDeserialize(converter = TimeConverter.class)
        protected Date lastModificationTime;
       @JacksonXmlProperty(localName = "Data")
       @JsonDeserialize(converter = Base64ToByteConverter.class)
       @JsonSerialize(converter = ByteToBase64Converter.class)
        protected byte[] data;

        public String getName() {
            return uuid.toString();
        }
    }

   /*  public static class ByteArray {

        public ByteArray() {

        }

        private byte[] content;

        public ByteArray(byte[] content) {
            this.content = content;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    } */

    public static class CustomData {

        public CustomData() {}

        protected List<Object> any;
    }

    @JacksonXmlRootElement(localName = "DeletedObjects")
    public static class DeletedObject {
        public DeletedObject() {}

       @JacksonXmlProperty(localName = "UUID")
        protected UUID uuid;
       @JacksonXmlProperty(localName = "DeletionTime")
       @JsonDeserialize(converter = TimeConverter.class)
        protected Date deletionTime;
    }
}
