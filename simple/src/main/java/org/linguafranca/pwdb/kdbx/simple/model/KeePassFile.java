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

package org.linguafranca.pwdb.kdbx.simple.model;

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.simple.SimpleGroup;
import org.linguafranca.pwdb.kdbx.simple.converter.Base64ByteArrayConverter;
import org.linguafranca.pwdb.kdbx.simple.converter.KeePassBooleanConverter;
import org.linguafranca.pwdb.kdbx.simple.converter.TimeConverter;
import org.linguafranca.pwdb.kdbx.simple.converter.UuidConverter;
import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Convert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author jo
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Root(name = "KeePassFile")
public class KeePassFile {

    @Element(name = "Meta")
    public Meta meta;
    @Element(name = "Root")
    public Root root;

    public List<Binaries.Binary> getBinaries() {
        return meta.binaries;
    }

    public static class Root {
        @Element(name = "Group")
        public SimpleGroup group;
        @ElementList(name = "DeletedObjects", required = false)
        protected ArrayList<DeletedObject> deletedObjects;

        public SimpleGroup getGroup() {
            return group;
        }
    }

    @SuppressWarnings("unused")
    public static class Meta {
        @Element(name = "Generator")
        protected String generator;
        @Element(name = "HeaderHash", required = false)
        @Convert(Base64ByteArrayConverter.class)
        public KeePassFile.ByteArray headerHash;
        @Element(name = "DatabaseName")
        public String databaseName;
        @Element(name = "DatabaseNameChanged", type = Date.class)
        @Convert(TimeConverter.class)
        public Date databaseNameChanged;
        @Element(name = "DatabaseDescription")
        public String databaseDescription;
        @Element(name = "DatabaseDescriptionChanged", type = Date.class)
        @Convert(TimeConverter.class)
        public Date databaseDescriptionChanged;
        @Element(name = "DefaultUserName")
        protected String defaultUserName;
        @Element(name = "DefaultUserNameChanged", type = Date.class)
        @Convert(TimeConverter.class)
        protected Date defaultUserNameChanged;
        @Element(name = "MaintenanceHistoryDays")
        protected int maintenanceHistoryDays;
        @Element(name = "Color")
        protected String color;
        @Element(name = "MasterKeyChanged", type = Date.class)
        @Convert(TimeConverter.class)
        protected Date masterKeyChanged;
        @Element(name = "MasterKeyChangeRec")
        protected int masterKeyChangeRec;
        @Element(name = "MasterKeyChangeForce")
        protected int masterKeyChangeForce;
        @Element(name = "MemoryProtection")
        public KeePassFile.MemoryProtection memoryProtection;
        @ElementList(name = "CustomIcons", required = false)
        protected ArrayList<Icon> customIcons;
        @Element(name = "RecycleBinEnabled", type = Boolean.class)
        @Convert(KeePassBooleanConverter.class)
        public Boolean recycleBinEnabled;
        @Element(name = "RecycleBinUUID", type = UUID.class)
        @Convert(UuidConverter.class)
        public UUID recycleBinUUID;
        @Element(name = "RecycleBinChanged", type = Date.class)
        @Convert(TimeConverter.class)
        public Date recycleBinChanged;
        @Element(name = "EntryTemplatesGroup", type = UUID.class)
        @Convert(UuidConverter.class)
        protected UUID entryTemplatesGroup;
        @Element(name = "EntryTemplatesGroupChanged", type = Date.class)
        @Convert(TimeConverter.class)
        protected Date entryTemplatesGroupChanged;
        @Element(name = "LastSelectedGroup", type = UUID.class)
        @Convert(UuidConverter.class)
        protected UUID lastSelectedGroup;
        @Element(name = "LastTopVisibleGroup", type = UUID.class)
        @Convert(UuidConverter.class)
        protected UUID lastTopVisibleGroup;
        @Element(name = "HistoryMaxItems")
        protected int historyMaxItems;
        @Element(name = "HistoryMaxSize")
        protected int historyMaxSize;
        @ElementList(name = "Binaries", required = false)
        protected List<Binaries.Binary> binaries;
        @Element(name = "CustomData", required = false)
        protected KeePassFile.CustomData customData;
    }


    public static class MemoryProtection {
        @Element(name = "ProtectTitle", type = Boolean.class)
        @Convert(KeePassBooleanConverter.class)
        protected Boolean protectTitle;
        @Element(name = "ProtectUserName", type = Boolean.class)
        @Convert(KeePassBooleanConverter.class)
        protected Boolean protectUserName;
        @Element(name = "ProtectPassword", type = Boolean.class)
        @Convert(KeePassBooleanConverter.class)
        protected Boolean protectPassword;
        @Element(name = "ProtectURL", type = Boolean.class)
        @Convert(KeePassBooleanConverter.class)
        protected Boolean protectURL;
        @Element(name = "ProtectNotes", type = Boolean.class)
        @Convert(KeePassBooleanConverter.class)
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
        @ElementList(name = "Binary")
        protected List<Binary> binary;

        @org.simpleframework.xml.Root(name = "Binary")
        public static class Binary implements org.simpleframework.xml.util.Entry {
            @Text
            protected String value;

            @Attribute(name = "ID")
            protected Integer id;
            @Attribute(name = "Compressed")
            @Convert(KeePassBooleanConverter.class)
            protected Boolean compressed;

            @Override
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
    }

    @org.simpleframework.xml.Root(name = "Icon")
    public static class Icon implements org.simpleframework.xml.util.Entry {
        @Element(name = "UUID", type=UUID.class)
        @Convert(UuidConverter.class)
        protected UUID uuid;
        @Element(name = "Data")
        @Convert(Base64ByteArrayConverter.class)
        protected ByteArray data;

        @Override
        public String getName() {
            return uuid.toString();
        }
    }

    /**
     * Work around problems deserializing byte[]
     */
    public static class ByteArray {
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
    }

    public static class CustomData {
        protected List<Object> any;
    }

    @org.simpleframework.xml.Root(name = "DeletedObject")
    public static class DeletedObject {
        @Element(name = "UUID", type = UUID.class)
        @Convert(UuidConverter.class)
        protected UUID uuid;
        @Element(name = "DeletionTime", type = Date.class)
        @Convert(TimeConverter.class)
        protected Date deletionTime;
    }
}
