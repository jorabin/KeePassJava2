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
import org.jetbrains.annotations.Nullable;
import org.linguafranca.pwdb.io.NonClosingOutputStream;

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
 * Entries (as well as importing subgroups and their entries). {@link Group#addEntry(Entry)}
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
public interface Database {

    /**
     * get the root group for the database
     * @return the root group
     */
    @NotNull Group getRootGroup();

    /**
     * Create a new Group
     * @return the group created
     */
    @NotNull Group newGroup();

    /**
     * Create a new named Group
     * @param name the name of the group
     * @return the group created
     */
    @NotNull Group newGroup(String name);

    /**
     * Create a new Group copying the details of the supplied group, but not copying its children
     *
     * <p>Used for copying a group from one database to another
     * @param group the group to copy
     * @return the group created
     */
    @NotNull Group newGroup(Group group);

    /**
     * Create a new Entry
     * @return the entry created
     */
    @NotNull Entry newEntry();

    /**
     * Create a new Entry with a title
     * @return the entry created
     */
    @NotNull Entry newEntry(String title);

    /**
     * Create a new Entry copying the details of the supplied entry
     *
     * <p>Used for copying an entry from one database to another
     * @param entry the entry to copy
     * @return the entry created
     */
    @NotNull Entry newEntry(Entry entry);

    /**
     * Create a new default icon
     * @return the created icon
     */
    @NotNull Icon newIcon();

    /**
     * Create a new icon with a specified index
     * @param i the index of the icon to create
     * @return the created icon
     */
    @NotNull Icon newIcon(Integer i);

    /**
     * Find an entry with this UUID anywhere in the database except the recycle bin
     * @param uuid the UUID
     * @return an entry or null if not found
     */
    @Nullable Entry findEntry(UUID uuid);

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
    @Nullable Group findGroup(UUID uuid);

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
     * If the recycle bin is enabled (or it's disabled but there is a pre-existing
     * recycle bin), then return the recycle bin, creating one if necessary.
     * If the recycle bin is disabled and there is no pre-existing recycle bin
     * or if recycle bin is not supported then return null.
     * @see Feature#RECYCLE_BIN
     */
    @Nullable Group getRecycleBin();

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
    void visit(Group group, Visitor visitor);

    /**
     * Find all entries that match the criteria
     *
     * @param matcher the matcher to use
     * @return a list of entries
     */
    @NotNull List<Entry> findEntries(Entry.Matcher matcher);

    /**
     * Find all entries that match {@link Entry#match(String)}
     *
     * @param find string to find
     * @return a list of entries
     */
    @NotNull List<Entry> findEntries(String find);

    /**
     * Gets the name of the database
     * @return a database name, or "" if there is none or names are not supported
     * @see Feature#DATABASE_NAME
     */
    @NotNull String getName();

    /**
     * Set the name of the database
     * @param name the name, null is treated as ""
     * @throws UnsupportedOperationException if names are not supported and the name is not empty
     * @see Feature#DATABASE_NAME
     */
    void setName(String name);

    /**
     * Gets the database description
     * @return the description, or "" if there is none
     */
    @NotNull String getDescription();

    /**
     * Sets the database description
     * @param description a description of the database, null is treated as ""
     */
    void setDescription(String description);

    /**
     * True if database been modified
     */
    boolean isDirty();

    /**
     * Write the database to a stream using the default format, leaving the stream open
     *
     * @param credentials credentials to use
     * @param outputStream where to write to - the caller closes it
     * @since 3.1.0
     */
    default void write(Credentials credentials, OutputStream outputStream) throws IOException {
        save(credentials, new NonClosingOutputStream(outputStream));
    }

    /**
     * Write the database to a stream using the format supplied, leaving the stream open
     *
     * @param streamFormat the format to use
     * @param credentials credentials to use
     * @param outputStream where to write to - the caller closes it
     * @since 3.1.0
     */
    default <C extends StreamConfiguration> void write(StreamFormat<C> streamFormat, Credentials credentials, OutputStream outputStream) throws IOException {
        save(streamFormat, credentials, new NonClosingOutputStream(outputStream));
    }

    /**
     * Save the database to a stream using default format and close the stream
     *
     * @deprecated closes a stream the caller opened; use {@link #write(Credentials, OutputStream)}
     * and close the stream yourself (issue #109)
     */
    @Deprecated
    void save(Credentials credentials, OutputStream outputStream) throws IOException;

    /**
     * Save the database to a stream using default format - avoiding checked exception
     *
     * @deprecated use {@link #write(Credentials, OutputStream)} (issue #109)
     */
    @Deprecated
    void saveNx(Credentials credentials, OutputStream outputStream);

    /**
     * Save the database to a stream and closes the stream
     *
     * @deprecated closes a stream the caller opened; use {@link #write(StreamFormat, Credentials, OutputStream)}
     * and close the stream yourself (issue #109)
     */
    @Deprecated
    <C extends StreamConfiguration> void save(StreamFormat<C> streamFormat, Credentials credentials, OutputStream outputStream) throws IOException;

    /**
     * Save the database to a stream and closes the stream - avoiding checked exception
     *
     * @deprecated use {@link #write(StreamFormat, Credentials, OutputStream)} (issue #109)
     */
    @Deprecated
    <C extends StreamConfiguration> void saveNx(StreamFormat<C> streamFormat, Credentials credentials, OutputStream outputStream);

    /**
     * Get the format the database was loaded from
     */
    @Nullable <C extends StreamConfiguration> StreamFormat<C> getStreamFormat();

    /**
     * Property to protect in memory
     * @param propertyName the property of interest
     * @return true if it should be protected by default
     */
    boolean shouldProtect(String propertyName);

    /**
     * Property to protect in memory
     * @param propertyName the property of interest
     * @param protect whether to protect by default
     */
    void setShouldProtect(String propertyName, boolean protect);

    /**
     * Obtain a list of those properties that should be protected by default
     * @return a list of property names
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull List<String> listShouldProtect();

    /**
     * Get the default means of storage of unprotected and protected property values
     */
    @NotNull PropertyValue.Strategy getPropertyValueStrategy();

    /**
     * Set the default means of storage of unprotected and protected property values
     * @param propertyValueStrategy a propertyValue strategy
     */
    void setPropertyValueStrategy(PropertyValue.Strategy propertyValueStrategy);

    /**
     * Whether the database supports a feature. When it doesn't, getters answer as if there were nothing
     * there, and setters throw {@link UnsupportedOperationException}, see {@link Feature}.
     * <p>
     * The default answers from the {@code supports…()} methods below; databases with features those
     * don't cover override it.
     *
     * @param feature the feature
     * @return true if the database supports it
     * @since 3.1.0
     */
    default boolean supports(Feature feature) {
        return switch (feature) {
            case DATABASE_NAME -> true;
            case AD_HOC_PROPERTIES -> supportsNonStandardPropertyNames();
            case BINARY_PROPERTIES, MULTIPLE_BINARY_PROPERTIES -> supportsBinaryProperties();
            case RECYCLE_BIN -> supportsRecycleBin();
            case PROPERTY_VALUE_STRATEGY -> supportsPropertyValueStrategy();
        };
    }

    /**
     * returns true if the database supports non-standard property names
     * @see Feature#AD_HOC_PROPERTIES
     */
    boolean supportsNonStandardPropertyNames();

    /**
     * returns true if the database supports binary properties
     * @see Feature#BINARY_PROPERTIES
     */
    boolean supportsBinaryProperties();

    /**
     * returns true if the database supports recycle bin
     * @see Feature#RECYCLE_BIN
     */
    boolean supportsRecycleBin();

    /**
     * returns true if the implementation supports a PropertyValueStrategy
     * @see Feature#PROPERTY_VALUE_STRATEGY
     */
    boolean supportsPropertyValueStrategy();

}
