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
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
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
@SuppressWarnings("WeakerAccess")
@Root(name = "Group")
public class SimpleGroup extends org.linguafranca.pwdb.base.AbstractGroup {
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

    public static SimpleGroup importGroup(SimpleGroup parent, Group group){
        if (group instanceof SimpleGroup && parent.database == ((SimpleGroup) group).database) {
            ((SimpleGroup) group).parent = parent;
            return (SimpleGroup) group;
        }

        SimpleGroup result = createGroup(parent.database);
        result.parent = parent;
        result.uuid = group.getUuid();
        result.name = group.getName();
        result.iconID = group.getIcon().getIndex();

        // copy entries
        for (Entry entry: group.getEntries()) {
            result.addEntry(SimpleEntry.importEntry(result, entry));
        }

        // copy sub groups
        for (Group child: group.getGroups()) {
            result.addGroup(importGroup(result, child));
        }
        return result;
    }

    @Override
    public boolean isRootGroup() {
        return database.getRootGroup().equals(this);
    }

    @Override
    public Group getParent() {
        return parent;
    }

    @Override
    public void setParent(Group group) {
        if (isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        if (!(group instanceof SimpleGroup)) {
            throw new IllegalStateException("Parent is not a compatible SimpleGroup type");
        }
        if (parent != null) {
            parent.removeGroup(group);
            parent.touch();
        }
        parent = (SimpleGroup) group;
        parent.touch();
        touch();
    }

    @Override
    public List<Group> getGroups() {
        List<Group> result = new ArrayList<>();
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
    public Group addGroup(Group group) {
        if (group.isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        if (group.getParent() != null) {
            group.getParent().removeGroup(group);
        }
        SimpleGroup g = importGroup(this, group);
        this.group.add(g);
        touch();
        return g;
    }

    @Override
    public Group removeGroup(Group group) {
        if (!(group instanceof SimpleGroup)) {
            throw new IllegalStateException("group is not a compatible type");
        }
        this.group.remove(group);
        ((SimpleGroup) group).parent = null;
        touch();
        return group;
    }

    @Override
    public List<Entry> getEntries() {
        List<Entry> result = new ArrayList<>();
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
    public Entry addEntry(Entry entry) {
        if (entry.getParent() != null) {
            entry.getParent().removeEntry(entry);
        }
        this.entry.add(SimpleEntry.importEntry(this, entry));
        touch();
        return entry;
    }

    @Override
    public Entry removeEntry(Entry entry) {
        if (!(entry instanceof SimpleEntry)) {
            throw new IllegalStateException("Entry is not a compatible type for removal");
        }
        this.entry.remove(entry);
        ((SimpleEntry) entry).parent = null;
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
    public Icon getIcon() {
        return new SimpleIcon(iconID);
    }

    @Override
    public void setIcon(Icon icon) {
        this.iconID = icon.getIndex();
        touch();
    }

    private void touch() {
        this.times.setLastModificationTime(new Date());
        this.database.setDirty(true);
    }
}
