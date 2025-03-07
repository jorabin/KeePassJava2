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

package org.linguafranca.pwdb;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Interface for a Database Group.
 *
 * <p>Databases have exactly one Root Group.
 *
 * <p>Other Groups of the database are a child of another Group.
 *
 * <p>A Database may contain Groups to an indefinite level.
 *
 * <p>Any Group may contain {@link Entry} items.
 *
 * <p>Groups have a name, a UUID and an Icon.
 *
 * @author Jo
 */
public interface Group {
    /**
     * Returns true if this is the root group of a database
     */
    boolean isRootGroup();

    /**
     * Returns true if this is the recycle bin of a database
     */
    boolean isRecycleBin();

    /**
     * Returns the parent of this group, or null if either this
     * group is the root group, or if the group does not have
     * a parent - e.g. if it is newly created or if it has
     * been removed from a previous parent.
      */
    @Nullable Group getParent();

    /**
     * Add this group to a parent. The group must be of a type compatible with the database
     * and if it already belongs to another group it is removed from that group.
     *
     * @param parent a prospective parent
     */
     void setParent(Group parent);

    /**
     * Returns a list of groups that are the children of this group.
     *
     * <p>The list returned is modifiable by the caller without affecting the status of the database.
     *
     * @return a modifiable list of groups
     */
    List<? extends Group> getGroups();

    /**
     * Returns the number of groups that are direct children of this group
     *
     * <p>It's possible that returning a list as in {@link #getGroups()} may incur significantly
     * more overhead so use this method if only the count is required
     */
    int getGroupsCount();

    /**
     * If the group belongs to this database then move it from its present parent, if any, to
     * the group on which this method is called.
     *
     * <p>The root group cannot be added to another group.
     *
     * @param group the group to add
     * @return the group added
     */
    Group addGroup(Group group);

    /**
     * Returns a list of child Groups whose name exactly matches that supplied.
     *
     * <p>The returned list may be modified without affecting the underlying database.
     *
     * @param groupName the name of the groups sought
     * @return a modifiable list
     */
    List<? extends Group> findGroups(String groupName);

    /**
     * Removes the group supplied from this group. The group removed
     * no longer has a parent and so in no longer part of the database unless
     * it is re-added to another group.
     * <p>
     * If the group is incompatible with the database an exception is thrown.
     * <p>
     * If the group is not present no error is thrown.
     * @param group the group to remove
     * @return the group passed for removal
     * @throws IllegalArgumentException if the group is not a child of this group
     */
    Group removeGroup(Group group);

    /**
     * Returns a modifiable by the caller list of entries contained in this group.
     */
    List<? extends Entry> getEntries();

    /**
     * Returns the number of entries in this group
     *
     * <p>It's possible that returning a list as in {@link #getEntries()} may incur significantly
     * more overhead so use this method if only the count is required
     */
    int getEntriesCount();

    /**
     * Finds all entries in this group that match the string supplied.
     * Optionally recurse over subgroups, excluding recycle bin.
     *
     * <p>Entry match is described under {@link Entry#match(String)}
     *
     * <p><em>Note:</em> finding within recycle bin is supported, recursion into recycle bin is inhibited
     *
     * @param match the text to match
     * @param recursive whether to include subgroups in the process
     * @return a modifiable-by-caller list
     * @see Entry#match(String)
     */
    List<? extends Entry> findEntries(String match, boolean recursive);

    /**
     * Finds all entries in this group that match using the matcher supplied.
     * Optionally recurse over subgroups, excluding recycle bin.
     *
     * <p>Entry match is described under {@link Entry#match(Entry.Matcher)}
     *
     * <p><em>Note:</em> finding within recycle bin is supported, recursion into recycle bin is inhibited
     *
     * @param matcher the matcher to use
     * @param recursive whether to include subgroups in the process
     * @return a modifiable-by-caller list
     * @see Entry#match(Entry.Matcher)
     */
    List<? extends Entry> findEntries(Entry.Matcher matcher, boolean recursive);

    /**
     * Adds an entry to this group removing it from another group
     * if it was part of one.
     * @param entry the entry to add
     * @return the entry added
     */
    Entry addEntry(Entry entry);

    /**
     * Adds a new entry to this group
     * @return the entry added
     */
    Entry addEntry();

    /**
     * Adds a new entry to this group
     * @param title the title of the entry to add
     * @return the entry added
     */
    Entry addEntry(String title);

    /**
     * Remove an entry from this group and hence from the database.
     * @param entry the entry to remove
     * @return the entry removed
     * @throws IllegalArgumentException if the Entry is not in the group
     */
    Entry removeEntry(Entry entry);

    /**
     * Make a deep copy of the children a group and add to this group. Does not copy the parent group.
     * @param parent the group to deep copy
     */
    void copy(Group parent);

    /**
     * Returns an XPath-like string of the names of Groups from the Root
     * to this Group.
     */
    String getPath();

    /**
     * Get the name of this group
     */
    String getName();

    /**
     * Set the name of this group
     */
    void setName(String name);

    /**
     * Get the UUID of this group
     */
    UUID getUuid();

    /**
     * Get the Icon of this group
     */
    Icon getIcon();

    /**
     * Set the Icon of this group
     */
    void setIcon(Icon icon);

    /**
     * Get the database this group was created from
     */
    @NotNull Database getDatabase();
}
