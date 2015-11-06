package org.linguafranca.keepass.db.base;

import org.linguafranca.keepass.db.Database;
import org.linguafranca.keepass.db.Entry;
import org.linguafranca.keepass.db.Group;

import java.util.List;

/**
 * @author Jo
 */
public abstract class AbstractDatabase implements Database {

    public void visit(Database.Visitor visitor){
        visitor.startVisit(getRootGroup());
        visit(getRootGroup(), visitor);
        visitor.endVisit(getRootGroup());
    }

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

    public List<Entry> findEntries(Entry.Matcher matcher){
        return getRootGroup().findEntries(matcher, true);
    }
}
