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

import org.linguafranca.pwdb.base.AbstractEntry;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.simple.converter.UuidConverter;
import org.linguafranca.pwdb.kdbx.simple.model.EntryClasses;
import org.linguafranca.pwdb.kdbx.simple.model.KeePassFile;
import org.linguafranca.pwdb.kdbx.simple.model.Times;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.convert.Convert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.linguafranca.pwdb.kdbx.simple.model.EntryClasses.*;

/**
 * Implementation of {@link org.linguafranca.pwdb.Entry} for Simple XML framework
 *
 * @author jo
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Root(name="Entry")
public class SimpleEntry extends AbstractEntry<SimpleDatabase, SimpleGroup, SimpleEntry, SimpleIcon> {
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

    protected SimpleEntry() {
        string = new ArrayList<>();
        binary = new ArrayList<>();
        times = new Times();
        uuid = UUID.randomUUID();
        iconID = 0;
    }

    /**
     * Factory to create a new {@link SimpleEntry} with no parent
     * @param database in which this entry
     * @return a detached Entry
     */
    public static SimpleEntry createEntry(SimpleDatabase database) {
        SimpleEntry result = new SimpleEntry();
        result.database = database;
        result.parent = null;
        // avoiding setProperty as it does a touch();
        for (String p: STANDARD_PROPERTY_NAMES) {
            result.string.add(new EntryClasses.StringProperty(p, new EntryClasses.StringProperty.Value("")));
        }
        return result;
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
    public boolean removeProperty(String name) throws IllegalArgumentException {
        if (STANDARD_PROPERTY_NAMES.contains(name)) throw new IllegalArgumentException("may not remove property: " + name);

        EntryClasses.StringProperty sp = getStringProperty(name, string);
        if (sp == null) {
            return false;
        } else {
            this.string.remove(sp);
            touch();
            return true;
        }
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
        newBin.setValue(Helpers.encodeBase64Content(bytes, true));
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
    public List<String> getBinaryPropertyNames() {
        List<String> result = new ArrayList<>();
        for (EntryClasses.BinaryProperty property: this.binary) {
            result.add(property.getKey());
        }
        return result;
    }

    @Override
    public SimpleGroup getParent() {
        return parent;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public SimpleIcon getIcon() {
        return new SimpleIcon(iconID);
    }

    @Override
    public void setIcon(SimpleIcon icon) {
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
    public void setExpires(boolean expires) {
        times.setExpires(expires);
    }

    @Override
    public Date getExpiryTime() {
        return times.getExpiryTime();
    }

    @Override
    public void setExpiryTime(Date expiryTime) throws IllegalArgumentException {
        if (expiryTime == null) throw new IllegalArgumentException("expiryTime may not be null");
        times.setExpiryTime(expiryTime);
    }

    @Override
    public Date getLastModificationTime() {
        return times.getLastModificationTime();
    }

    @Override
    protected void touch() {
        this.times.setLastModificationTime(new Date());
        this.database.setDirty(true);
    }
}