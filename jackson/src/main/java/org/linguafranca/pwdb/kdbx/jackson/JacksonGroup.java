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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.jetbrains.annotations.NotNull;
import org.linguafranca.pwdb.base.AbstractGroup;
import org.linguafranca.pwdb.kdbx.jackson.converter.Base64ToUUIDConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.BooleanToStringConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.StringToBooleanConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.UUIDToBase64Converter;
import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;
import org.linguafranca.pwdb.kdbx.jackson.model.Times;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
"previousParentGroup",
"tags",
"customData",
"entry",
"group",
})
@JsonIgnoreProperties(ignoreUnknown=true)
public class JacksonGroup extends AbstractGroup <JacksonDatabase, JacksonGroup, JacksonEntry, JacksonIcon>{

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

    @JacksonXmlProperty(localName = "PreviousParentGroup")
    @JsonDeserialize(converter = Base64ToUUIDConverter.class)
    @JsonSerialize(converter = UUIDToBase64Converter.class)
    protected UUID previousParentGroup;

    @JacksonXmlProperty(localName = "Tags")
    protected String tags;

    @JacksonXmlProperty(localName = "CustomData")
    protected KeePassFile.CustomData customData;

    @JacksonXmlProperty(localName = "Entry") /* Workaround jackson */
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<JacksonEntry> entries;


    @JacksonXmlProperty(localName = "Group") /* Workaround jackson */
    @JacksonXmlElementWrapper(useWrapping = false)
    protected List<JacksonGroup> groups;

    @JsonIgnore
    protected JacksonDatabase database;
    
    @JsonIgnore
    protected JacksonGroup parent;

    public JacksonGroup() {
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
    public void setParent(JacksonGroup g) {
        JacksonGroup group = (JacksonGroup) g;
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
        return new ArrayList<>(groups);
    }

    @Override
    @JsonIgnore
    public int getGroupsCount() {
        return groups.size();
    }

    @Override
    public JacksonGroup addGroup(JacksonGroup g) {
        JacksonGroup group = (JacksonGroup) g;
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
    public JacksonGroup removeGroup(JacksonGroup g) {
        JacksonGroup group = (JacksonGroup) g;
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
        return new ArrayList<>(this.entries);
    }

    @Override
    @JsonIgnore
    public int getEntriesCount() {
        return this.entries.size();
    }

    @Override
    public JacksonEntry addEntry(JacksonEntry e) {
        JacksonEntry entry = (JacksonEntry) e;
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
    public JacksonEntry removeEntry(JacksonEntry e) {
        JacksonEntry entry = (JacksonEntry) e;
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
