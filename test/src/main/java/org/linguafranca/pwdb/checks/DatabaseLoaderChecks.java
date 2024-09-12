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

package org.linguafranca.pwdb.checks;

import org.junit.Assert;
import org.junit.Test;
import org.linguafranca.pwdb.*;

import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * @author jo
 */
public abstract class DatabaseLoaderChecks <D extends Database<G, E>, G extends Group<G,E>, E extends Entry<G,E>> {

    static PrintStream printStream = getTestPrintStream();

    protected Database<G,E> database;
    protected boolean skipDateCheck = false;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX");
    /**
     * a test123 file for each format. Should contain the same thing. This is a basic sanity check.
     */
    @Test
    public void test123File() throws ParseException {

        // visit all groups and entries and list them to console
        database.visit(new Visitor.Print(printStream));

        // find all entries in the database
        // the kdb version has three additional system related entries
        List<? extends E> anything = database.findEntries("");
        Assert.assertTrue(10 <= anything.size());

        // find all entries in the database that have the string "test" in them
        List<? extends E> tests = database.findEntries("test");
        for (Entry tes: tests) {
            printStream.println(tes.getTitle());
        }
        Assert.assertEquals(4, tests.size());
        if (tests.size() > 0) {
            // copy the password of the first entry to the clipboard
            String pass = tests.get(0).getPassword();
/*
            StringSelection selection = new StringSelection(pass);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            printStream.println(pass + " copied to clip board");
*/
            // all the relevant entries should have the password 123
            String pass2 = tests.get(0).getPassword();
            Assert.assertEquals(pass, pass2);
            Assert.assertEquals("123", pass2);
        }

        List<? extends E> passwords = database.findEntries("password");
        Assert.assertEquals(4, passwords.size());
        for (Entry passwordEntry : passwords) {
            assertEquals(passwordEntry.getTitle(), passwordEntry.getPassword());
            printStream.println(passwordEntry.getTitle());
        }

        List<? extends E> entries = database.findEntries(new Entry.Matcher() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getTitle().equals("hello world");
            }});

        Assert.assertEquals(1, entries.size());
        assertEquals("pass", entries.get(0).getPassword());

        // kdb files don't have a time zone so can't make head or tail of the date - test file seems to have a local time in it
        if (skipDateCheck) {
            return;
        }

        Date c = entries.get(0).getCreationTime();
        Date expected = sdf.parse("2015-10-24T17:20:41Z");
        Assert.assertEquals(expected, c);
    }
}
