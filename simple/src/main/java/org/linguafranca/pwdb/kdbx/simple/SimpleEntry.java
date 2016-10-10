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

package org.linguafranca.pwdb.kdbx.simple;

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.base.AbstractEntry;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.simple.converter.UuidConverter;
import org.linguafranca.pwdb.kdbx.simple.model.EntryClasses;
import org.linguafranca.pwdb.kdbx.simple.model.KeePassFile;
import org.linguafranca.pwdb.kdbx.simple.model.Times;
import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Convert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.linguafranca.pwdb.kdbx.simple.model.EntryClasses.*;

/**
 * @author jo
 */
@SuppressWarnings({"WeakerAccess"})
@Root(name="Entry")
public class SimpleEntry extends AbstractEntry {
    @Element(name = "UUID", type=UUID.class)
    @Convert(UuidConverter.class)
    protected UUID uuid;
    @Element(name = "IconID")
    protected int iconID;
    @Element(name = "CustomIconUUID", type = UUID.class, required=false)
    @Convert(UuidConverter.class)
    protected UUID customIconUUID;
    @Element(name = "ForegroundColor", required=false)
    protected String foregroundColor;
    @Element(name = "BackgroundColor", required = false)
    protected String backgroundColor;
    @Element(name = "OverrideURL", required = false)
    protected String overrideURL;
    @Element(name = "Tags", required = false)
    protected String tags;
    @Element(name = "Times")
    protected Times times;
    @ElementList(inline=true)
    protected List<EntryClasses.StringProperty> string;
    @ElementList(inline=true, required = false)
    protected List<EntryClasses.BinaryProperty> binary;
    @Element(name = "AutoType", required = false)
    protected EntryClasses.AutoType autoType;
    @ElementList(name = "History", required = false)
    protected List<SimpleEntry> history;

    @Transient
    SimpleDatabase database;
    @Transient
    SimpleGroup parent;

    public SimpleEntry() {
        string = new ArrayList<>();
        binary = new ArrayList<>();
        times = new Times(new Date());
        uuid = UUID.randomUUID();
        iconID = 0;
    }

    @Override
    protected void touch() {
        this.times.setLastModificationTime(new Date());
        this.database.setDirty(true);
    }

    @Override
    public String getProperty(String s) {
        return getStringContent(getStringProperty(s, string));
    }

    @Override
    public void setProperty(String s, String s1) {
        EntryClasses.StringProperty sp;
        if ((sp = getStringProperty(s, string)) != null) {
            this.string.remove(sp);
        }
        this.string.add(new EntryClasses.StringProperty(s, new EntryClasses.StringProperty.Value(s1)));
        touch();
    }

    @Override
    public List<String> getPropertyNames() {
        List<String> result = new ArrayList<>();
        for (EntryClasses.StringProperty property: this.string) {
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

        KeePassFile.Binaries.Binary binary = null;
        for (KeePassFile.Binaries.Binary b : database.getBinaries()) {
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
        if (bp != null){
            binary.remove(bp);
        }

        // what is the next free index in the binary store?
        Integer max = -1;
        for (KeePassFile.Binaries.Binary binary: database.getBinaries()){
            if (binary.getId() > max) {
                max = binary.getId();
            }
        }
        max++;

        // create a new binary to put in the store
        KeePassFile.Binaries.Binary newBin = new KeePassFile.Binaries.Binary();
        newBin.setId(max);
        newBin.setValue(new String(Helpers.encodeBase64Content(bytes, true)));
        newBin.setCompressed(true);
        database.getBinaries().add(newBin);

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
    public List<String> getBinaryPropertyNames() {
        List<String> result = new ArrayList<>();
        for (EntryClasses.BinaryProperty property: this.binary) {
            result.add(property.getKey());
        }
        return result;
    }

    @Override
    public org.linguafranca.pwdb.Group getParent() {
        return parent;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public Icon getIcon() {
        return new SimpleIcon(iconID);
    }

    @Override
    public void setIcon(Icon icon) {
        iconID = icon.getIndex();
    }

    @Override
    public Date getLastAccessTime() {
        return times.getLastAccessTime();
    }

    @Override
    public Date getCreationTime() {
        return times.getCreationTime();
    }

    @Override
    public boolean getExpires() {
        return times.getExpires();
    }

    @Override
    public Date getExpiryTime() {
        return times.getExpiryTime();
    }

    @Override
    public Date getLastModificationTime() {
        return times.getLastModificationTime();
    }

    public static SimpleEntry createEntry(SimpleDatabase database, SimpleGroup parent) {
        SimpleEntry result = new SimpleEntry();
        result.database = database;
        result.parent = parent;
        for (String p: Entry.STANDARD_PROPERTY_NAMES) {
            result.setProperty(p, "");
        }
        return result;
    }

    public static SimpleEntry createEntry(SimpleGroup parent, org.linguafranca.pwdb.Entry entry) {
        if (entry instanceof SimpleEntry && ((SimpleEntry) entry).database == parent.database) {
            return ((SimpleEntry) entry);
        }
        SimpleEntry result = createEntry(parent.database, parent);
        result.iconID = entry.getIcon().getIndex();
        result.uuid = entry.getUuid();
        for (String propertyName: entry.getPropertyNames()) {
            result.setProperty(propertyName, entry.getProperty(propertyName));
        }
        for (String propertyName: entry.getBinaryPropertyNames()) {
            result.setBinaryProperty(propertyName, entry.getBinaryProperty(propertyName));
        }
        return result;
    }
}