/*
 * Copyright (c) 2025. Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.linguafranca.pwdb.basic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.abstractdb.AbstractGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BasicGroup extends AbstractGroup {
    private BasicGroup parent;
    private final List<BasicGroup> groups = new ArrayList<>();
    private final List<BasicEntry> entries = new ArrayList<>();
    private String name;
    private Icon icon;
    private final UUID uuid;
    private final @NotNull BasicDatabase database;

    BasicGroup(@NotNull BasicDatabase database) {
        this.database = database;
        this.uuid = UUID.randomUUID();
        this.icon = new BasicIcon();
        this.name = "";
    }

    BasicGroup(@NotNull BasicDatabase database, String name) {
        this(database);
        this.name = name;
    }

    @Override
    public boolean isRootGroup() {
        return false;
    }

    @Override
    public boolean isRecycleBin() {
        return false;
    }

    @Override
    public @Nullable Group getParent() {
        return parent;
    }

    @Override
    public void setParent(Group parent) {
        this.parent = (BasicGroup) parent;
    }

    @Override
    public List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    @Override
    public int getGroupsCount() {
        return groups.size();
    }

    @Override
    public Group addGroup(Group group) {
        BasicGroup basicGroup = (BasicGroup) group;
        groups.add(basicGroup);
        basicGroup.parent = this;
        return group;
    }

    @Override
    public Group removeGroup(Group group) {
        BasicGroup basicGroup = (BasicGroup) group;
        groups.remove(basicGroup);
        basicGroup.parent = null;
        return group;
    }

    @Override
    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public int getEntriesCount() {
        return entries.size();
    }

    @Override
    public Entry addEntry(Entry entry) {
        BasicEntry basicEntry = (BasicEntry) entry;
        entries.add(basicEntry);
        basicEntry.parent = this;
        return entry;
    }

    @Override
    public Entry removeEntry(Entry entry) {
        BasicEntry basicEntry = (BasicEntry) entry;
        entries.remove(basicEntry);
        (basicEntry).parent = null;
        return entry;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name=name;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }

    @Override
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public Database getDatabase() {
        return this.database;
    }
}
