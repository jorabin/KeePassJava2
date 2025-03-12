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

package org.linguafranca.pwdb.test;

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Visitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * a test123 file for each format. Should contain the same thing. This is a basic sanity check.
 * <p>
 * (i.e. there exist files called test123 for some formats and this test tests their contents. presumably
 * back in the day, a database was created then saved in kdbx and kdb formats and when there was more than
 * one KDBX implementation the test was created to test them all as well as the KDB implementation)
 */
public interface Test123Test {

    boolean getSkipDateCheck();
    Database getDatabase();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX");

    @Test
    default void test123File() throws ParseException {

        // visit all groups and entries and list them to console
        getDatabase().visit(new Visitor.Print(getTestPrintStream()));

        // find all entries in the database
        // the kdb version has three additional system related entries
        List<? extends Entry> anything = getDatabase().findEntries("");
        assertTrue(10 <= anything.size());

        // find all entries in the database that have the string "test" in them
        List<? extends Entry> tests = getDatabase().findEntries("test");
        for (Entry tes: tests) {
            getTestPrintStream().println(tes.getTitle());
        }
        assertEquals(4, tests.size());
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
            assertEquals(pass, pass2);
            assertEquals("123", pass2);
        }

        List<? extends Entry> passwords = getDatabase().findEntries("password");
        assertEquals(4, passwords.size());
        for (Entry passwordEntry : passwords) {
            assertEquals(passwordEntry.getTitle(), passwordEntry.getPassword());
            getTestPrintStream().println(passwordEntry.getTitle());
        }

        List<? extends Entry> entries = getDatabase().findEntries(new Entry.Matcher() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getTitle().equals("hello world");
            }});

        assertEquals(1, entries.size());
        assertEquals("pass", entries.get(0).getPassword());

        // kdb files don't have a time zone so can't make head or tail of the date - test file seems to have a local time in it
        if (getSkipDateCheck()) {
            return;
        }

        Date c = entries.get(0).getCreationTime();
        Date expected = sdf.parse("2015-10-24T17:20:41Z");
        assertEquals(expected, c);
    }
}
