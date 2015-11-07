package org.linguafranca.keepass;

import java.util.List;

/**
 * Interface for a password database
 */
public interface Database {
    /**
     * Interface for a Database visitor
     */
    interface Visitor {
        /**
         * Called on entry to a group visit
         * @param group the group being visited
         */
        void startVisit(Group group);

        /**
         * Called on exit from a group visit
         * @param group the group being exited
         */
        void endVisit(Group group);

        /**
         * Called on visit to an entry
          * @param entry the entry being visited
         */
        void visit(Entry entry);

        /**
         * called to determine whether to visit entries before subgroups, or not
         * @return true to visit
         */
        boolean isEntriesFirst();
    }

    abstract class DefaultVisitor implements Visitor {

        @Override
        public void startVisit(Group group) {}

        @Override
        public void endVisit(Group group) {}

        @Override
        public void visit(Entry entry) {}

        @Override
        public boolean isEntriesFirst() {
            return true;
        }
    }

    class PrintVisitor extends DefaultVisitor {
        @Override
        public void startVisit(Group group) {
            System.out.println(group.getPath() + ": " + group.toString());
        }

        @Override
        public void visit(Entry entry) {
            System.out.println(entry.getPath() + ": " + entry.toString());
        }
    }

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
