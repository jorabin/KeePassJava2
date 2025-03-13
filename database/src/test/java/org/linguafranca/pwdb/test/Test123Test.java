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
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Visitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME.PASSWORD;
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
    Database loadDatabase(Credentials credentials, java.io.InputStream inputStream);
    Credentials getCredentials(byte[] credentials);
    String getFileName();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX");

    @Test
    default void test123File() throws ParseException {
        Database database = loadDatabase(getCredentials("123".getBytes()),
                getClass().getClassLoader().getResourceAsStream(getFileName()));
        // visit all groups and entries and list them to console
        database.visit(new Visitor.Print(getTestPrintStream()));

        // find all entries in the database
        // the kdb version has three additional system related entries
        List<? extends Entry> anything = database.findEntries("");
        assertTrue(10 <= anything.size());

        // find all entries in the database that have the string "test" in them
        List<? extends Entry> tests = database.findEntries("test");
        for (Entry tes: tests) {
            getTestPrintStream().println(tes.getTitle());
        }
        assertEquals(4, tests.size());
        if (!tests.isEmpty()) {
            // copy the password of the first entry
            String pass = tests.get(0).getProperty(PASSWORD);
            // all the relevant entries should have the password 123
            String pass2 = tests.get(0).getProperty(PASSWORD);
            assertEquals(pass, pass2);
            assertEquals("123", pass2);
        }

        List<? extends Entry> passwords = database.findEntries("password");
        assertEquals(4, passwords.size());
        for (Entry passwordEntry : passwords) {
            assertEquals(passwordEntry.getTitle(), passwordEntry.getProperty(PASSWORD));
            getTestPrintStream().println(passwordEntry.getTitle());
        }

        List<? extends Entry> entries = database.findEntries(entry -> entry.getTitle().equals("hello world"));

        assertEquals(1, entries.size());
        assertEquals("pass", entries.get(0).getProperty(PASSWORD));

        // kdb files don't have a time zone so can't make head or tail of the date - test file seems to have a local time in it
        if (getSkipDateCheck()) {
            return;
        }

        Date c = entries.get(0).getCreationTime();
        Date expected = sdf.parse("2015-10-24T17:20:41Z");
        assertEquals(expected, c);
    }
}
