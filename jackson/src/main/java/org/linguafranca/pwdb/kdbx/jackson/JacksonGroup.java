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
import org.linguafranca.pwdb.kdbx.jackson.converter.StringToBooleanConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.UUIDToBase64Converter;
import org.linguafranca.pwdb.kdbx.jackson.converter.Base64ToUUIDConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.BooleanToStringConverter;
import org.linguafranca.pwdb.kdbx.jackson.model.Times;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

// @JacksonXmlRootElement(localName = "Group")
@JsonPropertyOrder({
"uuid",
"name",
"notes",
"iconID",
"customIconUUID",
"times",
"isExpanded",
"defaultAutoTypeSequence",
"enableAutoType",
"enableSearching",
"lastTopVisibleEntry",
"entry",
"group",
//"customData"
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class JacksonGroup
        extends org.linguafranca.pwdb.base.AbstractGroup<JacksonDatabase, JacksonGroup, JacksonEntry, JacksonIcon> {

    @JacksonXmlProperty(localName = "UUID")
    @JsonDeserialize(converter = Base64ToUUIDConverter.class)
    @JsonSerialize(converter = UUIDToBase64Converter.class)
    protected UUID uuid;

    @JacksonXmlProperty(localName = "Name")
    protected String name;
    
    @JacksonXmlProperty(localName = "Notes")
    protected String notes;
    
    @JacksonXmlProperty(localName = "IconID")
    protected int iconID;
    
    @JacksonXmlProperty(localName = "CustomIconUUID")
    @JsonDeserialize(converter = Base64ToUUIDConverter.class)
    @JsonSerialize(converter = UUIDToBase64Converter.class)
    protected UUID customIconUuid;
    
    @JacksonXmlProperty(localName = "Times")
    protected Times times;
    
    @JacksonXmlProperty(localName = "IsExpanded")
    @JsonDeserialize(converter = StringToBooleanConverter.class)
    @JsonSerialize(converter = BooleanToStringConverter.class)
    protected Boolean isExpanded;
    
    @JacksonXmlProperty(localName = "DefaultAutoTypeSequence")
    protected String defaultAutoTypeSequence;
    
    @JacksonXmlProperty(localName = "EnableAutoType")
    @JsonDeserialize(converter = StringToBooleanConverter.class)
    @JsonSerialize(converter = BooleanToStringConverter.class)
    protected Boolean enableAutoType;
    
    @JacksonXmlProperty(localName = "EnableSearching")
    @JsonDeserialize(converter = StringToBooleanConverter.class)
    @JsonSerialize(converter = BooleanToStringConverter.class)
    protected Boolean enableSearching;

    @JacksonXmlProperty(localName = "LastTopVisibleEntry")
    @JsonDeserialize(converter = Base64ToUUIDConverter.class)
    @JsonSerialize(converter = UUIDToBase64Converter.class)
    protected UUID lastTopVisibleEntry;

    @JacksonXmlProperty(localName = "Entry") /** Workaround jackson **/
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<JacksonEntry> entries;


    @JacksonXmlProperty(localName = "Group") /** Workaround jackson **/
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<JacksonGroup> groups;

    @JsonIgnore
    protected JacksonDatabase database;
    
    @JsonIgnore
    protected JacksonGroup parent;

    protected JacksonGroup() {
        entries = new ArrayList<>();
        groups = new ArrayList<>();
        times = new Times();
    }

    public static JacksonGroup createGroup(JacksonDatabase database) {
        JacksonGroup group = new JacksonGroup();
        group.database = database;
        group.iconID = 0;
        group.name = "";
        group.uuid = UUID.randomUUID();
        return group;
    }

    @Override
    public boolean isRootGroup() {
        return database.getRootGroup().equals(this);
    }

    @Override
    public boolean isRecycleBin() {
        return database.keePassFile.meta.recycleBinUUID.equals(this.uuid);
    }

    @Override
    public JacksonGroup getParent() {
        return parent;
    }

    @Override
    public void setParent(JacksonGroup group) {
        if (isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        if (this.database != group.database) {
            throw new IllegalStateException("Must be from same database");
        }
        if (parent != null) {
            parent.removeGroup(group);
            parent.touch();
        }
        parent = group;
        parent.touch();
        touch();
    }

    @Override
    public List<JacksonGroup> getGroups() {
        List<JacksonGroup> result = new ArrayList<>();
        for (JacksonGroup aGroup : groups) {
            result.add(aGroup);
        }
        return result;
    }

    @Override
    @JsonIgnore
    public int getGroupsCount() {
        return groups.size();
    }

    @Override
    public JacksonGroup addGroup(JacksonGroup group) {
        if (group.isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        if (this.database != group.database) {
            throw new IllegalStateException("Must be from same database");
        }
        if (group.getParent() != null) {
            group.getParent().removeGroup(group);
        }
        group.parent = this;
        this.groups.add(group);
        touch();
        return group;
    }

    @Override
    public JacksonGroup removeGroup(JacksonGroup group) {
        if (this.database != group.database) {
            throw new IllegalStateException("Must be from same database");
        }
        this.groups.remove(group);
        group.parent = null;
        touch();
        return group;
    }

    @Override
    public List<JacksonEntry> getEntries() {
        List<JacksonEntry> result = new ArrayList<>();
        for (JacksonEntry entry : this.entries) {
            result.add(entry);
        }
        return result;
    }

    @Override
    @JsonIgnore
    public int getEntriesCount() {
        return this.entries.size();
    }

    @Override
    public JacksonEntry addEntry(JacksonEntry entry) {
        if (this.database != entry.database) {
            throw new IllegalStateException("Must be from same database");
        }
        if (entry.getParent() != null) {
            entry.getParent().removeEntry(entry);
        }
        this.entries.add(entry);
        entry.parent = this;
        touch();
        return entry;
    }

    @Override
    public JacksonEntry removeEntry(JacksonEntry entry) {
        if (this.database != entry.database) {
            throw new IllegalStateException("Must be from same database");
        }
        this.entries.remove(entry);
        entry.parent = null;
        return entry;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String s) {
        this.name = s;
        touch();
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public JacksonIcon getIcon() {
        return new JacksonIcon(iconID);
    }

    @Override
    public void setIcon(JacksonIcon icon) {
        this.iconID = icon.getIndex();
        touch();
    }

    @NotNull
    @Override
    public JacksonDatabase getDatabase() {
        return database;
    }

    private void touch() {
        if (this.times != null) {
            this.times.setLastModificationTime(new Date());
        }

        if (this.database != null) {
            this.database.setDirty(true);
        }

    }
}
