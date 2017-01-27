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

import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.simple.converter.KeePassBooleanConverter;
import org.linguafranca.pwdb.kdbx.simple.converter.UuidConverter;
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

/**
 * Implementation of {@link Group} using the Simple XML framework.
 * @author jo
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Root(name = "Group")
public class SimpleGroup extends org.linguafranca.pwdb.base.AbstractGroup<SimpleDatabase, SimpleGroup, SimpleEntry, SimpleIcon> {
    @Element(name = "UUID", type = UUID.class)
    @Convert(UuidConverter.class)
    protected UUID uuid;
    @Element(name = "Name")
    protected String name;
    @Element(name = "Notes", required = false)
    protected String notes;
    @Element(name = "IconID")
    protected int iconID;
    @Element(name = "Times")
    protected Times times;
    @Element(name = "IsExpanded", required = false, type = Boolean.class)
    @Convert(KeePassBooleanConverter.class)
    protected Boolean isExpanded;
    @Element(name = "DefaultAutoTypeSequence", required = false)
    protected String defaultAutoTypeSequence;
    @Element(name = "EnableAutoType", required = false, type = Boolean.class)
    @Convert(KeePassBooleanConverter.class)
    protected Boolean enableAutoType;
    @Element(name = "EnableSearching", required = false, type = Boolean.class)
    @Convert(KeePassBooleanConverter.class)
    protected Boolean enableSearching;
    @Element(name = "LastTopVisibleEntry", required = false, type = UUID.class)
    @Convert(UuidConverter.class)
    protected UUID lastTopVisibleEntry;
    @ElementList(inline = true, required = false)
    protected List<SimpleEntry> entry;
    @ElementList(inline = true, required = false)
    protected List<SimpleGroup> group;

    @Transient
    protected SimpleDatabase database;
    @Transient
    protected SimpleGroup parent;

    protected SimpleGroup() {
        entry = new ArrayList<>();
        group = new ArrayList<>();
        times = new Times();
    }

    public static SimpleGroup createGroup(SimpleDatabase database) {
        SimpleGroup group = new SimpleGroup();
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
    public SimpleGroup getParent() {
        return parent;
    }

    @Override
    public void setParent(SimpleGroup group) {
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
    public List<SimpleGroup> getGroups() {
        List<SimpleGroup> result = new ArrayList<>();
        for (SimpleGroup aGroup : group) {
            result.add(aGroup);
        }
        return result;
    }

    @Override
    public int getGroupsCount() {
        return group.size();
    }

    @Override
    public SimpleGroup addGroup(SimpleGroup group) {
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
        this.group.add(group);
        touch();
        return group;
    }

    @Override
    public SimpleGroup removeGroup(SimpleGroup group) {
        if (this.database != group.database) {
            throw new IllegalStateException("Must be from same database");
        }
        this.group.remove(group);
        group.parent = null;
        touch();
        return group;
    }

    @Override
    public List<SimpleEntry> getEntries() {
        List<SimpleEntry> result = new ArrayList<>();
        for (SimpleEntry entry: this.entry){
            result.add(entry);
        }
        return result;
    }

    @Override
    public int getEntriesCount() {
        return this.entry.size();
    }

    @Override
    public SimpleEntry addEntry(SimpleEntry entry) {
        if (this.database != entry.database) {
            throw new IllegalStateException("Must be from same database");
        }
        if (entry.getParent() != null) {
            entry.getParent().removeEntry(entry);
        }
        this.entry.add(entry);
        entry.parent=this;
        touch();
        return entry;
    }

    @Override
    public SimpleEntry removeEntry(SimpleEntry entry) {
        if (this.database != entry.database) {
            throw new IllegalStateException("Must be from same database");
        }
        this.entry.remove(entry);
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
    public SimpleIcon getIcon() {
        return new SimpleIcon(iconID);
    }

    @Override
    public void setIcon(SimpleIcon icon) {
        this.iconID = icon.getIndex();
        touch();
    }

    @Override
    public SimpleDatabase getDatabase() {
        return database;
    }

    private void touch() {
        this.times.setLastModificationTime(new Date());
        this.database.setDirty(true);
    }
}
