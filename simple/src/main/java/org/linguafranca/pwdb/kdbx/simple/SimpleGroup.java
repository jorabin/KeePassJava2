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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRootGroup() {
        return database.getRootGroup().equals(this);
    }

    @Override
    public org.linguafranca.pwdb.Group getParent() {
        return parent;
    }

    @Override
    public void setParent(org.linguafranca.pwdb.Group group) {
        if (isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        if (!(group instanceof SimpleGroup)) {
            throw new IllegalStateException("Parent is not a compatible SimpleGroup type");
        }
        if (parent != null) {
            parent.removeGroup(group);
        }
        parent = (SimpleGroup) group;
    }

    @Override
    public List<org.linguafranca.pwdb.Group> getGroups() {
        List<org.linguafranca.pwdb.Group> result = new ArrayList<>();
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
    public org.linguafranca.pwdb.Group addGroup(org.linguafranca.pwdb.Group group) {
        if (group.isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        if (group.getParent() != null) {
            group.getParent().removeGroup(group);
        }
        SimpleGroup g = createGroup(this, group);
        g.parent = this;
        this.group.add(g);
        return g;
    }

    @Override
    public org.linguafranca.pwdb.Group removeGroup(org.linguafranca.pwdb.Group group) {
        if (!(group instanceof SimpleGroup)) {
            throw new IllegalStateException("SimpleGroup is not a compatible SimpleGroup type");
        }
        this.group.remove(group);
        ((SimpleGroup) group).parent = null;
        return group;
    }

    @Override
    public List<org.linguafranca.pwdb.Entry> getEntries() {
        List<org.linguafranca.pwdb.Entry> result = new ArrayList<>();
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
    public org.linguafranca.pwdb.Entry addEntry(org.linguafranca.pwdb.Entry entry) {
        this.entry.add(SimpleEntry.createEntry(this, entry));
        return entry;
    }

    @Override
    public org.linguafranca.pwdb.Entry removeEntry(org.linguafranca.pwdb.Entry entry) {
        if (!(entry instanceof SimpleEntry)) {
            throw new IllegalStateException("SimpleEntry is not a compatible SimpleEntry type");
        }
        this.entry.remove(entry);
        ((SimpleEntry) entry).parent = null;
        return entry;

    }

    @Override
    public void setName(String s) {
        this.name = s;
        this.times.setLastModificationTime(new Date());
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

    }

    public static SimpleGroup createGroup(SimpleDatabase database, SimpleGroup parent) {
        SimpleGroup group = new SimpleGroup();
        group.parent = parent;
        group.database = database;
        group.iconID = 0;
        group.name = "";
        group.uuid = UUID.randomUUID();
        return group;
    }

    public static SimpleGroup createGroup(SimpleGroup parent, Group group){
        if (group instanceof SimpleGroup && parent.database == ((SimpleGroup) group).database) {
            return (SimpleGroup) group;
        }

        SimpleGroup result = createGroup(parent.database, parent);
        result.uuid = group.getUuid();
        result.name = group.getName();
        result.iconID = group.getIcon().getIndex();
        return result;
    }
}
