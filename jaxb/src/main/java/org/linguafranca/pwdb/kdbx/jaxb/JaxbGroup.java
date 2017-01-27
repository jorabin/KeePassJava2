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

import org.linguafranca.pwdb.base.AbstractGroup;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbEntryBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbGroupBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link org.linguafranca.pwdb.Group} for JAXB.
 *
 * <p>The class wraps an underlying JAXB generated delegate.
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class JaxbGroup extends AbstractGroup<JaxbDatabase, JaxbGroup, JaxbEntry, JaxbIcon> {

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
    public boolean isRecycleBin() {
        return database.getKeePassFile().getMeta().getRecycleBinUUID().equals(this.getUuid());
    }

    @Override
    public JaxbGroup getParent() {
        if (delegate.parent == null) {
            return null;
        }
        return new JaxbGroup(database, ((JaxbGroupBinding) delegate.parent));
    }

    @Override
    public void setParent(JaxbGroup group) {
        if (isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }

        if (this.database != group.database) {
            throw new IllegalStateException("Must be from same database");
        }

        JaxbGroupBinding parent = (JaxbGroupBinding) group.delegate.parent;

        if (parent != null) {
            parent.getGroup().remove(group.delegate);
            parent.getTimes().setLastModificationTime(new Date());
        }

        this.delegate.parent = group.delegate;
        ((JaxbGroupBinding) this.delegate.parent).getTimes().setLastModificationTime(new Date());
        touch();
    }

    @Override
    public List<JaxbGroup> getGroups() {
        List<JaxbGroup> result = new ArrayList<>();
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
    public JaxbGroup addGroup(JaxbGroup group) {
        if (group.isRootGroup()) {
            throw new IllegalStateException("Cannot add root group to another group");
        }
        if (this.database != group.database) {
            throw new IllegalStateException("Must be from same database");
        }

        if (group.getParent() != null) {
            group.getParent().delegate.getGroup().remove(group.delegate);
        }
        group.delegate.parent = this.delegate;
        this.delegate.getGroup().add(group.delegate);
        touch();
        return group;
    }

    @Override
    public JaxbGroup removeGroup(JaxbGroup group) {
        if (this.database != group.database) {
            throw new IllegalStateException("Must be from same database");
        }
        delegate.getGroup().remove(group.delegate);
        group.delegate.parent = null;
        touch();
        return group;
    }

    @Override
    public List<JaxbEntry> getEntries() {
        List<JaxbEntry> result = new ArrayList<>();
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
    public JaxbEntry addEntry(JaxbEntry entry) {
        if (this.database != entry.database) {
            throw new IllegalStateException("Must be from same database");
        }
        if (entry.getParent() != null) {
            entry.getParent().removeEntry(entry);
        }
        delegate.getEntry().add(entry.delegate);
        entry.delegate.parent = this.delegate;
        touch();
        return entry;
    }

    @Override
    public JaxbEntry removeEntry(JaxbEntry entry) {
        delegate.getEntry().remove(entry.delegate);
        entry.delegate.parent = null;
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
    public JaxbIcon getIcon() {
        return new JaxbIcon(this.delegate.getIconID());
    }

    @Override
    public void setIcon(JaxbIcon icon) {
        this.delegate.setIconID(icon.getIndex());
    }

    @Override
    public JaxbDatabase getDatabase() {
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
}
