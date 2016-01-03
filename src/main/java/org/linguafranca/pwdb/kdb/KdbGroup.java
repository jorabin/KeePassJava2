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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.base.AbstractGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * The class holds a KDB Group
 * @author jo
 */
public class KdbGroup extends AbstractGroup {
    private boolean root;
    private KdbGroup parent;
    private UUID uuid = UUID.randomUUID();
    private String name = "";
    private Icon icon = new KdbIcon(0);
    private List<Group> groups = new ArrayList<>();
    private List<Entry> entries = new ArrayList<>();
    private Date creationTime;
    private Date lastModificationTime;
    private Date lastAccessTime;
    private Date expiryTime;
    private int flags;

    KdbGroup() {
        creationTime = new Date();
        lastModificationTime = creationTime;
        lastAccessTime = new Date(Long.MIN_VALUE);
        expiryTime = new Date(Long.MAX_VALUE);
    }

    @Override
    public Group addGroup(Group group) {
        groups.add(group);
        if (group.getParent() != null) {
            group.getParent().removeGroup(group);
        }
        ((KdbGroup) group).parent = this;
        return group;
    }

    @Override
    public Group removeGroup(Group group) {
        groups.remove(group);
        ((KdbGroup) group).parent = null;
        return group;
    }

    @Override
    public List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    @Override
    public int getEntriesCount() {
        return entries.size();
    }

    @Override
    public Entry addEntry(Entry entry) {
        KdbGroup entryParent = (((KdbEntry) entry).parent);
        if (entryParent != null) {
            entryParent.removeEntry(entry);
        }
        entries.add(entry);
        ((KdbEntry) entry).parent = this;
        return entry;
    }

    @Override
    public Entry removeEntry(Entry entry) {
        entries.remove(entry);
        ((KdbEntry) entry).parent = null;
        return entry;
    }

    /**
     * local helper to determine the level of a group while building group structure
     *
     * @return -3 if detached branch, -2 if no parent, -1 if root, 0 if top level etc.
     */
    int computedLevel() {
        if (isRootGroup()) {
            return -1;
        }
        int level = 0;
        Group currentParent = parent;
        if (currentParent == null) {
            return -2;
        }
        while (!currentParent.isRootGroup()) {
            currentParent = currentParent.getParent();
            if (currentParent == null) {
                return -3;
            }
            level++;
        }
        return level;
    }

    @Override
    public boolean isRootGroup() {
        return root;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Group getParent() {
        return parent;
    }

    @Override
    public void setParent(Group parent) {
        parent.addGroup(this);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    @Override
    public int getGroupsCount() {
        return groups.size();
    }

    int getFlags() {
        return flags;
    }

    void setFlags(int flags) {
        this.flags = flags;
    }

    void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    void setLastModificationTime(Date lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

    void setRoot(boolean root) {
        this.root = root;
    }

    public String toString() {
        String time = KdbDatabase.isoDateFormat.format(creationTime);
        return getPath() + String.format(" (%s) %s [%d]", uuid.toString(), time, flags);
    }
}
