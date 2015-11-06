package org.linguafranca.keepass.db;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for Database facade through which password databases may be accessed
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
     * Create  a new Group
     * @return the group created
     */
    Group newGroup();

    /**
     * Create a new Entry
     * @return the entry created
     */
    Entry newEntry();

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

    String getFilename();

    String getDescription();

    void setDescription(String description);

    void setPassword(String password);

    void save(String filename) throws IOException;

    void save(Path pathname) throws IOException;

    void save() throws IOException;
}
