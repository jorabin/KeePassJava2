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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linguafranca.pwdb.base.AbstractEntry;
import org.linguafranca.pwdb.kdbx.Helpers;


import static org.linguafranca.pwdb.kdbx.jackson.model.EntryClasses.*;
import org.linguafranca.pwdb.kdbx.jackson.model.Times;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Entry")
public class JacksonEntry extends AbstractEntry<JacksonDatabase, JacksonGroup, JacksonEntry, JacksonIcon> {

    @JacksonXmlProperty(localName = "UUID")
    protected UUID uuid;
    @JacksonXmlProperty(localName = "IconID")
    protected int iconID;
    @JacksonXmlProperty(localName = "CustomIconUUID")
    protected UUID customIconUUID;
    @JacksonXmlProperty(localName = "ForegroundColor")
    protected String foregroundColor;
    @JacksonXmlProperty(localName = "BackgroundColor")
    protected String backgroundColor;
    @JacksonXmlProperty(localName = "OverrideURL")
    protected String overrideURL;
    @JacksonXmlProperty(localName = "Tags")
    protected String tags;
    @JacksonXmlProperty(localName = "Times")
    protected Times times;

    @JacksonXmlProperty(localName = "String") /** Workaround jackson  **/
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<StringProperty> string;

    @JacksonXmlProperty(localName = "Binary") /** Workaround jackson  **/
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<BinaryProperty> binary;

    @JacksonXmlProperty(localName = "AutoType")
    protected AutoType autoType;

    @JacksonXmlProperty(localName = "History") /** Workaround jackson  **/
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<JacksonHistory> history;

    @JsonIgnore
    JacksonDatabase database;

    @JsonIgnore
    JacksonGroup parent;

    protected JacksonEntry() {
        string = new ArrayList<>();
        binary = new ArrayList<>();
        times = new Times();
        uuid = UUID.randomUUID();
        iconID = 0;
    }


    public static JacksonEntry createEntry(JacksonDatabase database) {
        JacksonEntry result = new JacksonEntry();
        result.database = database;
        result.parent = null;
        // avoiding setProperty as it does a touch();
        for (String p: STANDARD_PROPERTY_NAMES) {
            result.string.add(new StringProperty(p, new StringProperty.Value("")));
        }
        return result;
    }

    @Override
    public String getProperty(String s) {
        return getStringContent(getStringProperty(s, string));
    }

    @Override
    public void setProperty(String s, String s1) {
        StringProperty sp;
        if ((sp = getStringProperty(s, string)) != null) {
            this.string.remove(sp);
        }
        this.string.add(new StringProperty(s, new StringProperty.Value(s1)));
        touch();
    }

    @Override
    public boolean removeProperty(String name) throws IllegalArgumentException {
        if (STANDARD_PROPERTY_NAMES.contains(name)) throw new IllegalArgumentException("may not remove property: " + name);

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
    public List<String> getPropertyNames() {
        List<String> result = new ArrayList<>();
        for (StringProperty property: this.string) {
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
        if (bp != null){
            binary.remove(bp);
        }

        // what is the next free index in the binary store?
        Integer max = -1;
        for (KeePassFile.Binary binary: database.getBinaries()){
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
        for (BinaryProperty property: this.binary) {
            result.add(property.getKey());
        }
        return result;
    }

    @Override
    public JacksonGroup getParent() {
        return parent;
    }

    @Override
    public @NotNull UUID getUuid() {
        return uuid;
    }

    @Override
    public JacksonIcon getIcon() {
        return new JacksonIcon(iconID);
    }

    @Override
    public void setIcon(JacksonIcon icon) {
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
