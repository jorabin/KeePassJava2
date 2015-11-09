package org.linguafranca.db;

import java.util.List;

/**
 * Interface for a password database consisting of Groups, sub-Groups and Entries.
 * A database is arranged as a tree starting from the root group, with entries
 * allowed as children of any group.
 *
 * <p>A database is a factory for new Groups and Entries. Groups and entries belonging
 * to one databsae cannot in general be added to another database, they need to be
 * copied using {@link #newGroup(Group)} and {@link #newEntry(Entry)}.
 *
 * <p>Databases may be navigated directly from the root, or using a {@link Visitor} and
 * {@link #visit(Visitor)}.
 *
 * <p>All Lists provided by all interfaces my be modified by the caller without
 * affecting the underlying database structure.
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
}
