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

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

/**
 * Interface for a password database consisting of Groups, sub-Groups and Entries.
 * A database is arranged as a tree starting from the root group, with entries
 * allowed as children of any group.
 *
 * <p>A database is a factory for new Groups and Entries. Groups and entries belonging
 * to one database cannot in general be added to another database, they need to be
 * imported using {@link #newGroup(Group)} and {@link #newEntry(Entry)}, or implicitly
 * imported using {@link Group#addGroup(Group)}  which automatically create Groups and
 * Entries (as well as importing sub groups and their entries). {@link Group#addEntry(Entry)}
 * allows arbitrary importing from other databases.
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
public interface Database <D extends Database<D, G, E, I>, G extends Group<D, G, E, I>, E extends Entry<D,G,E,I>, I extends Icon> {

    /**
     * get the root group for the database
     * @return the root group
     */
    G getRootGroup();

    /**
     * Create a new Group
     * @return the group created
     */
    G newGroup();

    /**
     * Create a new named Group
     * @param name the name of the group
     * @return the group created
     */
    G newGroup(String name);

    /**
     * Create a new Group copying the details of the supplied group, but not copying its children
     *
     * <p>Used for copying a group from one database to another
     * @param group the group to copy
     * @return the group created
     */
    G newGroup(Group group);

    /**
     * Create a new Entry
     * @return the entry created
     */
    E newEntry();

    /**
     * Create a new Entry with a title
     * @return the entry created
     */
    E newEntry(String title);

    /**
     * Create a new Entry copying the details of the supplied entry
     *
     * <p>Used for copying an entry from one database to another
     * @param entry the entry to copy
     * @return the entry created
     */
    E newEntry(Entry<?,?,?,?> entry);

    /**
     * Create a new default icon
     * @return the created icon
     */
    I newIcon();

    /**
     * Create a new icon with a specified index
     * @param i the index of the icon to create
     * @return the created icon
     */
    I newIcon(Integer i);

    /**
     * Find an entry with this UUID anywhere in the database except the recycle bin
     * @param uuid the UUID
     * @return an entry or null if not found
     */
    @Nullable E findEntry(UUID uuid);

    /**
     * Delete an entry with this UUID from anywhere in the database except the recycle bin
     * if recycle is enabled then the entry is moved to the recycle bin
     * @param uuid the UUID
     * @return true if an entry was deleted
     */
    boolean deleteEntry(UUID uuid);

    /**
     * Find a group with this UUID anywhere in the database except the recycle bin
     * @param uuid the UUID
     * @return a group or null if not found
     */
    @Nullable G findGroup(UUID uuid);

    /**
     * Delete a group with this UUID from anywhere in the database except the recycle bin
     * if recycle is enabled then the group is moved to the recycle bin
     * @param uuid the UUID
     * @return true if a group was deleted
     */
    boolean deleteGroup(UUID uuid);

    /**
     * if a database has a recycle bin then it is enabled by default
     * @return true if the recycle bin is enabled - false if it is not or is not supported
     */
    boolean isRecycleBinEnabled();

    /**
     * change the recycle bin state
     * @throws UnsupportedOperationException if recycle bin functions are not supported
     * @see #supportsRecycleBin()
     */
    void enableRecycleBin(boolean enable);

    /**
     * If the recycle bin is enabled or it's disabled but there is a pre-existing
     * recycle bin, then return the recycle bin, creating one if necessary.
     * If the recycle bin is disabled and there is no pre-existing recycle bin
     * or if recycle bin is not supported then return null.
     * @see #supportsRecycleBin()
     */
    @Nullable G getRecycleBin();

    /**
     * empty the recycle bin whether it is enabled or disabled
     * @throws UnsupportedOperationException if recycle bin functions are not supported
     * @see #supportsRecycleBin()
     */
    void emptyRecycleBin();

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
    void visit(G group, Visitor visitor);

    /**
     * Find all entries that match the criteria
     *
     * @param matcher the matcher to use
     * @return a list of entries
     */
    List<? extends E> findEntries(Entry.Matcher matcher);

    /**
     * Find all entries that match {@link Entry#match(String)}
     *
     * @param find string to find
     * @return a list of entries
     */
    List<? extends E> findEntries(String find);

    /**
     * Gets the name of the database or null if not supported
     * @return a database name
     */
    String getName();

    /**
     * Set the name of the database if this is supported
     * @param name the name
     */
    void setName(String name);

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

    /**
     * Save the database to a stream
     */
    void save(Credentials credentials, OutputStream outputStream) throws IOException;

    /**
     * Properties to encrypt
     * @param propertyName the property of interest
     * @return true if it should be encrypted
     */
    boolean shouldProtect(String propertyName);

    /**
     * returns true if the database supports non-standard property names
     */
    boolean supportsNonStandardPropertyNames();

    /**
     * returns true if the database supports binary properties
     */
    boolean supportsBinaryProperties();

    /**
     * returns true if the database supports recycle bin
     */
    boolean supportsRecycleBin();

}
