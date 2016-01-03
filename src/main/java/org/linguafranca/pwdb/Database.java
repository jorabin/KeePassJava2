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

package org.linguafranca.pwdb;

import java.util.List;

/**
 * Interface for a password database consisting of Groups, sub-Groups and Entries.
 * A database is arranged as a tree starting from the root group, with entries
 * allowed as children of any group.
 *
 * <p>A database is a factory for new Groups and Entries. Groups and entries belonging
 * to one database cannot in general be added to another database, they need to be
 * copied using {@link #newGroup(Group)} and {@link #newEntry(Entry)}.
 *
 * <p>Databases may be navigated directly from the root {@link #getRootGroup()},
 * or using a {@link Visitor} and {@link #visit(Visitor)} or {@link #visit(Group, Visitor)}
 * to start the visit at a particular group.
 *
 * <p>A list of entries that match a string or some custom criteria may be obtained using
 * the {@link #findEntries(Entry.Matcher)} and {@link #findEntries(String)} methods.
 *
 * <p>To match (optionally recursively) entries in a {@link Group} use
 * {@link Group#findEntries(String, boolean)} or {@link Group#findEntries(Entry.Matcher, boolean)}.
 *
 * <p>All Lists provided returned by methods of all interfaces may be modified by
 * the caller without affecting the underlying database structure, however changes
 * to the Groups and Entries contained in the lists do modify the database.
 */
public interface Database {

    /**
     * get the root group for the database
     * @return the root group
     */
    Group getRootGroup();

    /**
     * Create a new Group
     * @return the group created
     */
    Group newGroup();

    /**
     * Create a new named Group
     * @param name the name of the group
     * @return the group created
     */
    Group newGroup(String name);

    /**
     * Create a new Group copying the details of the supplied group, but not copying its children
     *
     * <p>Used for copying a group from one database to another
     * @param group the group to copy
     * @return the group created
     */
    Group newGroup(Group group);

    /**
     * Create a new Entry
     * @return the entry created
     */
    Entry newEntry();

    /**
     * Create a new Entry with a title
     * @return the entry created
     */
    Entry newEntry(String title);

    /**
     * Create a new Entry copying the details of the supplied entry
     *
     * <p>Used for copying an entry from one database to another
     * @param entry the entry to copy
     * @return the entry created
     */
    Entry newEntry(Entry entry);

    /**
     * Create a new default icon
     * @return the created icon
     */
    Icon newIcon();

    /**
     * Create a new icon with a specified index
     * @param i the index of the icon to create
     * @return the created icon
     */
    Icon newIcon(Integer i);

    /**
     * Visit all entries
     *
     * @param visitor the visitor to use
     */
    void visit(Visitor visitor);

    /**
     * Visit all entries starting from a group
     * @param group the group to start at
     * @param visitor the visitor to use
     */
    void visit(Group group, Visitor visitor);

    /**
     * Find all entries that match the criteria
     *
     * @param matcher the matcher to use
     * @return a list of entries
     */
    List<Entry> findEntries(Entry.Matcher matcher);

    /**
     * Find all entries that match {@link Entry#match(String)}
     *
     * @param find string to find
     * @return a list of entries
     */
    List<Entry> findEntries(String find);

    /**
     * Gets the database description, if there is one
     * @return the description or null if not supported
     */
    String getDescription();

    /**
     * Sets the database description if it is supported
     * @param description a description of the database
     */
    void setDescription(String description);

    /**
     * True if database been modified
     */
    boolean isDirty();
}
