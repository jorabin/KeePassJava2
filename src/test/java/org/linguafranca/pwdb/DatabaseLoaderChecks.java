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

package org.linguafranca.pwdb;

import org.junit.Test;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author jo
 */
public class DatabaseLoaderChecks {
    protected Database database;

    /**
     * a test123 file for each format. Should contain the same thing. This is a basic sanity check.
     */
    @Test
    public void test123File() {

        // visit all groups and entries and list them to console
        database.visit(new Visitor.Print());

        // find all entries in the database
        // the kdb version has three additional system related entries
        List<Entry> anything = database.findEntries("");
        assertTrue(10 <= anything.size());

        // find all entries in the database that have the string "test" in them
        List<Entry> tests = database.findEntries("test");
        for (Entry tes: tests) {
            System.out.println(tes.getTitle());
        }
        assertEquals(4, tests.size());
        if (tests.size() > 0) {
            // copy the password of the first entry to the clipboard
            String pass = tests.get(0).getPassword();
            StringSelection selection = new StringSelection(pass);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            System.out.println(pass + " copied to clip board");
            // all the relevant entries should have the password 123
            String pass2 = tests.get(0).getPassword();
            assertEquals(pass, pass2);
            assertEquals("123", pass2);
        }

        List<Entry> passwords = database.findEntries("password");
        assertEquals(4, passwords.size());
        for (Entry passwordEntry : passwords) {
            assertEquals(passwordEntry.getTitle(), passwordEntry.getPassword());
            System.out.println(passwordEntry.getTitle());
        }

        List<Entry> entries = database.findEntries(new Entry.Matcher() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getTitle().equals("hello world");
            }});

        assertEquals(1, entries.size());
        assertEquals("pass", entries.get(0).getPassword());
    }
}
