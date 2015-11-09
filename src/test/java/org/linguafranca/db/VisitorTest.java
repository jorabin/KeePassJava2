package org.linguafranca.db;

import org.junit.Test;
import org.linguafranca.db.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.db.kdbx.KdbxCredentials;
import org.linguafranca.db.kdbx.KdbxStreamFormat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Jo
 */
public class VisitorTest {
    List<Entry> visitorList = new ArrayList<>();
    Visitor visitor = new Visitor() {
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
        public void visit(Entry entry) {
            System.out.println(indentation.toString() + "= " + entry.getTitle());
            visitorList.add(entry);
        }
    };

    @Test
    public void testLoadDB() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
            DomDatabaseWrapper db = new DomDatabaseWrapper(new KdbxStreamFormat(), new KdbxCredentials.Password("123".getBytes()), inputStream);
            db.visit(visitor);

            List<Entry> matched = db.findEntries(new Entry.Matcher() {
                String matchee = "";
                @Override
                public boolean matches(Entry entry) {
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
