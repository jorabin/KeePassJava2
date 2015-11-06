package org.linguafranca.keepass.droid;

import org.junit.Test;
import org.linguafranca.keepass.db.Database;
import org.linguafranca.keepass.db.Entry;
import org.linguafranca.keepass.db.Group;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Jo
 */
public class VisitorTest {
    List<org.linguafranca.keepass.db.Entry> visitorList = new ArrayList<>();
    Database.Visitor visitor = new Database.Visitor() {
        StringBuffer indentation = new StringBuffer();
        @Override
        public void startVisit(Group group) {
            System.out.println(indentation.toString() + group.getName());
            indentation.append("   ");
        }

        @Override
        public void endVisit(Group group) {
            indentation.setLength(indentation.length()-3);
        }

        @Override
        public boolean isEntriesFirst() {
            return false;
        }

        @Override
        public void visit(org.linguafranca.keepass.db.Entry entry) {
            System.out.println(indentation.toString() + "= " + entry.getTitle());
            visitorList.add(entry);
        }
    };

    @Test
    public void testLoadDB() {
        try {
            org.linguafranca.keepass.db.Database db = DatabaseWrapper.load("loadsavetest.kdbx", "mypass");
            db.visit(visitor);

            List<org.linguafranca.keepass.db.Entry> matched = db.findEntries(new org.linguafranca.keepass.db.Entry.Matcher() {
                String matchee = "";
                @Override
                public boolean matches(org.linguafranca.keepass.db.Entry entry) {
                    return entry.getTitle().toLowerCase().contains(matchee) ||
                            entry.getNotes().toLowerCase().contains(matchee) ||
                            entry.getUsername().toLowerCase().contains(matchee);
                }
            });

            assertTrue(matched.size() == visitorList.size());

            for (Entry e: matched) {
                assertTrue(visitorList.contains(e));
            }
        } catch (Exception e) {
            assertTrue("Couldn\'t open test DB " + e.getMessage(), false);
        }
    }
}
