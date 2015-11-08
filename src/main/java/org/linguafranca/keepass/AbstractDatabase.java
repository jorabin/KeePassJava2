package org.linguafranca.keepass;

import java.util.List;

/**
 * @author Jo
 */
public abstract class AbstractDatabase implements Database {

    @Override
    public void visit(Database.Visitor visitor) {
        visitor.startVisit(getRootGroup());
        visit(getRootGroup(), visitor);
        visitor.endVisit(getRootGroup());
    }

    @Override
    public void visit(Group group, Database.Visitor visitor) {

        if (visitor.isEntriesFirst()) {
            for (Entry entry : group.getEntries()) {
                visitor.visit(entry);
            }
        }

        for (Group g : group.getGroups()) {
            visitor.startVisit(g);
            visit(g, visitor);
            visitor.endVisit(g);
        }

        if (!visitor.isEntriesFirst()) {
            for (Entry entry : group.getEntries()) {
                visitor.visit(entry);
            }
        }
    }

    @Override
    public List<Entry> findEntries(Entry.Matcher matcher) {
        return getRootGroup().findEntries(matcher, true);
    }

    @Override
    public List<Entry> findEntries(String find) {
        return getRootGroup().findEntries(find, true);
    }

    @Override
    public Group newGroup(String name) {
        Group result = newGroup();
        result.setName(name);
        return result;
    }

    @Override
    public Group newGroup(Group group) {
        Group result = newGroup();
        result.setName(group.getName());
        result.setIcon(this.newIcon(group.getIcon().getIndex()));
        return result;
    }

    @Override
    public Entry newEntry(String title) {
        Entry result = newEntry();
        result.setTitle(title);
        return result;
    }

    @Override
    public Entry newEntry(Entry entry) {
        Entry result = newEntry();
        for (String propertyName: entry.getPropertyNames()) {
            try {
                // all implementations must support setting of STANDARD_PROPERTY_NAMES
                result.setProperty(propertyName, entry.getProperty(propertyName));
            } catch (UnsupportedOperationException e) {
                // oh well, we tried
            }
        }
        result.setIcon(this.newIcon(entry.getIcon().getIndex()));
        // everything else should have been copied via properties
        return result;
    }
}
