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

import java.util.List;
import java.util.UUID;

/**
 * @author jo
 */
public class JaxbGroupWrapper extends AbstractGroup {
    private final JaxbDatabaseWrapper databaseWrapper;
    private Group parent;
    private final org.linguafranca.pwdb.kdbx.mem.Group group;
    private List<Group> children;
    private List<Entry> entries;

    public JaxbGroupWrapper(JaxbDatabaseWrapper databaseWrapper, Group parent, org.linguafranca.pwdb.kdbx.mem.Group group) {
        this.databaseWrapper = databaseWrapper;
        this.parent = parent;
        this.group = group;
        for (org.linguafranca.pwdb.kdbx.mem.Group childGroup : group.getGroup()) {
            children.add(new JaxbGroupWrapper(databaseWrapper, this, childGroup));
        }
        for (org.linguafranca.pwdb.kdbx.mem.Entry childEntry : group.getEntry()) {
            entries.add(new JaxbEntryWrapper(databaseWrapper, this, childEntry));
        }
    }

    @Override
    public boolean isRootGroup() {
        return parent == null;
    }

    @Override
    public Group getParent() {
        return parent;
    }

    @Override
    public void setParent(Group parent) {
        this.parent = parent;
    }

    @Override
    public List<Group> getGroups() {
        return children;
    }

    @Override
    public int getGroupsCount() {
        return children.size();
    }

    @Override
    public Group addGroup(Group group) {
        children.add(group);
        return group;
    }

    @Override
    public Group removeGroup(Group group) {
        children.remove(group);
        return group;
    }

    @Override
    public List<Entry> getEntries() {
        return entries;
    }

    @Override
    public int getEntriesCount() {
        return entries.size();
    }

    @Override
    public Entry addEntry(Entry entry) {
        entries.add(entry);
        return entry;
    }

    @Override
    public Entry removeEntry(Entry entry) {
        entries.remove(entry);
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

    public org.linguafranca.pwdb.kdbx.mem.Group getBackingGroup() {
        return group;
    }
}
