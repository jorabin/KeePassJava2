package org.linguafranca.db;

/**
 * Interface for implementing a visitor for Groups, their sub-Groups and their Entries.
 *
 * @author jo
 */
public interface Visitor {
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

    /**
     * Empty implementation of Visitor
     */
    abstract class Default implements Visitor {

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

    /**
     * Visitor prints the Groups and Entries it visits
     */
    class Print extends Default {
        @Override
        public void startVisit(Group group) {
            System.out.println(group.toString());
        }

        @Override
        public void visit(Entry entry) {
            System.out.println(entry.toString());
        }
    }
}
