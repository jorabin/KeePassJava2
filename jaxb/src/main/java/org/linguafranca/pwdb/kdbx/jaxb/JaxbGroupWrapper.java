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

package org.linguafranca.pwdb.kdbx.jaxb;


import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.base.AbstractGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class JaxbGroupWrapper extends AbstractGroup {
    private final JaxbDatabaseWrapper databaseWrapper;
    private Group parent;
    private final org.linguafranca.pwdb.kdbx.jaxb.binding.Group group;
    boolean isRootGroup;

    public JaxbGroupWrapper(JaxbDatabaseWrapper databaseWrapper, Group parent, org.linguafranca.pwdb.kdbx.jaxb.binding.Group group) {
        this.databaseWrapper = databaseWrapper;
        this.parent = parent;
        this.group = group;

        if (group.getUUID() == null) {
            group.setUUID(UUID.randomUUID());
        }
        if (group.getTimes() == null) {
            group.setTimes(databaseWrapper.getObjectFactory().createTimes());
        }
        if (group.getName() == null) {
            group.setName("");
        }
    }

    @Override
    public boolean isRootGroup() {
        return isRootGroup;
    }

    @Override
    public Group getParent() {
        return parent;
    }

    @Override
    public void setParent(Group parent) {
        if (isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        this.parent = parent;
    }

    @Override
    public List<Group> getGroups() {
        List<Group> result = new ArrayList<>();
        for (org.linguafranca.pwdb.kdbx.jaxb.binding.Group child : group.getGroup()) {
            result.add(new JaxbGroupWrapper(databaseWrapper, this, child));
        }
        return result;
    }

    @Override
    public int getGroupsCount() {
        return group.getGroup().size();
    }

    @Override
    public Group addGroup(Group group) {
        if (group.isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        JaxbGroupWrapper g = JaxbGroupWrapper.create(databaseWrapper, this, group);
        this.group.getGroup().add(g.getBackingGroup());
        if (group.getParent() != null) {
            group.getParent().removeGroup(group);
        }
        group.setParent(this);
        return group;
    }

    @Override
    public Group removeGroup(Group group) {
        if (!(group instanceof JaxbGroupWrapper)){
            return null;
        }
        this.group.getGroup().remove(((JaxbGroupWrapper) group).getBackingGroup());
        group.setParent(null);
        return group;
    }

    @Override
    public List<Entry> getEntries() {
        List<Entry> result = new ArrayList<>();
        for (org.linguafranca.pwdb.kdbx.jaxb.binding.Entry entry : group.getEntry()) {
            result.add(new JaxbEntryWrapper(databaseWrapper, this, entry));
        }
        return result;
    }

    @Override
    public int getEntriesCount() {
        return group.getEntry().size();
    }

    @Override
    public Entry addEntry(Entry entry) {
        JaxbEntryWrapper e = JaxbEntryWrapper.create(databaseWrapper, this, entry);
        group.getEntry().add(e.getBackingEntry());
        return entry;
    }

    @Override
    public Entry removeEntry(Entry entry) {
        this.group.getEntry().remove(((JaxbEntryWrapper) entry).getBackingEntry());
        return entry;
    }

    @Override
    public String getName() {
        return group.getName();
    }

    @Override
    public void setName(String name) {
        group.setName(name);
    }

    @Override
    public UUID getUuid() {
        return group.getUUID();
    }

    @Override
    public Icon getIcon() {
        return new JaxbIconWrapper(group.getIconID());
    }

    @Override
    public void setIcon(Icon icon) {
        group.setIconID(icon.getIndex());
    }

    public org.linguafranca.pwdb.kdbx.jaxb.binding.Group getBackingGroup() {
        return group;
    }

    public static JaxbGroupWrapper create(JaxbDatabaseWrapper wrapper, JaxbGroupWrapper parent, Group group){
        if (group instanceof JaxbGroupWrapper && ((JaxbGroupWrapper) group).databaseWrapper.equals(parent.databaseWrapper)) {
            return (JaxbGroupWrapper) group;
        }
        org.linguafranca.pwdb.kdbx.jaxb.binding.Group backingGroup = wrapper.getObjectFactory().createGroup();
        backingGroup.setName(group.getName());
        backingGroup.setIconID(group.getIcon().getIndex());
        backingGroup.setUUID(group.getUuid());
        JaxbGroupWrapper result = new JaxbGroupWrapper(wrapper, parent, backingGroup);

        // copy entries
        for (Entry entry: group.getEntries()) {
            backingGroup.getEntry().add(JaxbEntryWrapper.create(wrapper, result, entry).getBackingEntry());
        }

        // copy sub groups
        for (Group child: group.getGroups()) {
            backingGroup.getGroup().add(JaxbGroupWrapper.create(wrapper, result, child).getBackingGroup());
        }
        return result;
     }

    @Override
    public int hashCode() {
        int result = databaseWrapper.hashCode();
        result = 31 * result + group.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JaxbGroupWrapper that = (JaxbGroupWrapper) o;

        return databaseWrapper.equals(that.databaseWrapper) && group.equals(that.group);

    }
}
