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


import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.base.AbstractGroup;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbEntryBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbGroupBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link Group} for JAXB.
 *
 * <p>The class wraps an underlying JAXB generated delegate.
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class JaxbGroup extends AbstractGroup {

    protected JaxbDatabase database;
    protected JaxbGroupBinding delegate;

    /**
     * Wrap an existing {@link JaxbGroupBinding} in this database
     * @param database the database
     * @param group a JaxbGroupBinding
     */
    public JaxbGroup(JaxbDatabase database, JaxbGroupBinding group) {
        this.delegate = group;
        this.database = database;
    }

    public JaxbGroup(JaxbDatabase database) {
        this.database = database;
        this.delegate = database.getObjectFactory().createJaxbGroupBinding();
        delegate.setTimes(new ObjectFactory().createTimes());
        delegate.setIconID(0);
        delegate.setName("");
        delegate.setUUID(UUID.randomUUID());
    }

    @Override
    public boolean isRootGroup() {
        return database.getRootGroup().equals(this);
    }

    @Override
    public JaxbGroup getParent() {
        if (delegate.parent == null) {
            return null;
        }
        return new JaxbGroup(database, ((JaxbGroupBinding) delegate.parent));
    }

    @Override
    public void setParent(Group group) {
        if (isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }

        if (!isCompatibleGroup(database, group)) {
            throw new IllegalStateException("Parent is incompatible");
        }

        JaxbGroup JaxbGroupBinding = (JaxbGroup) group;
        JaxbGroupBinding parent = (JaxbGroupBinding) JaxbGroupBinding.delegate.parent;

        if (parent != null) {
            parent.getGroup().remove(JaxbGroupBinding.delegate);
            parent.getTimes().setLastModificationTime(new Date());
        }

        this.delegate.parent = JaxbGroupBinding.delegate;
        ((JaxbGroupBinding) this.delegate.parent).getTimes().setLastModificationTime(new Date());
        touch();
    }

    @Override
    public List<Group> getGroups() {
        List<Group> result = new ArrayList<>();
        for (JaxbGroupBinding child : delegate.getGroup()) {
            result.add(new JaxbGroup(database, child));
        }
        return result;
    }

    @Override
    public int getGroupsCount() {
        return delegate.getGroup().size();
    }

    @Override
    public Group addGroup(Group group) {
        if (group.isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        if (!isCompatibleGroup(this.database, group)) {
            throw new IllegalStateException("Group to add is incompatible");
        }
        JaxbGroup jaxbGroup = (JaxbGroup) group;
        if (group.getParent() != null) {
            ((JaxbGroup) group.getParent()).delegate.getGroup().remove(jaxbGroup.delegate);
        }
        jaxbGroup.delegate.parent = this.delegate;
        this.delegate.getGroup().add(jaxbGroup.delegate);
        touch();
        return jaxbGroup;
    }

    @Override
    public Group removeGroup(Group group) {
        if (!isCompatibleGroup(database, group)) {
            throw new IllegalStateException("group is not a compatible type");
        }
        delegate.getGroup().remove(((JaxbGroup) group).delegate);
        ((JaxbGroup) group).delegate.parent = null;
        touch();
        return group;
    }

    @Override
    public List<Entry> getEntries() {
        List<Entry> result = new ArrayList<>();
        for (JaxbEntryBinding entry : this.delegate.getEntry()) {
            result.add(new JaxbEntry(database, entry));
        }
        return result;
    }

    @Override
    public int getEntriesCount() {
        return this.delegate.getEntry().size();
    }

    @Override
    public Entry addEntry(Entry entry) {
        if (!isCompatibleEntry(database, entry)) {
            throw new IllegalStateException("Incompatible Entry");
        }
        if (entry.getParent() != null) {
            entry.getParent().removeEntry(entry);
        }
        JaxbEntry jaxbEntry = ((JaxbEntry) entry);
        delegate.getEntry().add(jaxbEntry.delegate);
        jaxbEntry.delegate.parent = this.delegate;
        touch();
        return jaxbEntry;
    }

    @Override
    public Entry removeEntry(Entry entry) {
        if (!(entry instanceof JaxbEntry)) {
           throw new IllegalStateException("Entry is not a consistent type for removal");
        }
        delegate.getEntry().remove(((JaxbEntry) entry).delegate);
        ((JaxbEntry) entry).delegate.parent = null;
        return entry;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String name) {
        this.delegate.setName(name);
    }

    @Override
    public UUID getUuid() {
        return this.delegate.getUUID();
    }

    @Override
    public Icon getIcon() {
        return new JaxbIconWrapper(this.delegate.getIconID());
    }

    @Override
    public void setIcon(Icon icon) {
        this.delegate.setIconID(icon.getIndex());
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JaxbGroup that = (JaxbGroup) o;

        return database.equals(that.database) && delegate.equals(that.delegate);
    }

    private void touch() {
        this.delegate.getTimes().setLastModificationTime(new Date());
        this.database.setDirty(true);
    }

    private static boolean isCompatibleGroup(JaxbDatabase database, Group group) {
        return (group != null && group instanceof JaxbGroup && ((JaxbGroup) group).database.equals(database));
    }
    private static boolean isCompatibleEntry(JaxbDatabase database, Entry entry) {
        return (entry != null && entry instanceof JaxbEntry && ((JaxbEntry) entry).database.equals(database));
    }

}
