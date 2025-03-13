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

package org.linguafranca.pwdb.kdbx.database.validation;

import com.google.common.io.CharStreams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.format.Helpers;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class Issue27Test {
    static PrintStream printStream = getTestPrintStream();
    /**
     * Check load of problem file
     */
    @Test
    public void testIssue27() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("issue-27/bogus-timestamp2.kdbx");
        KdbxCredentials creds = new KdbxCredentials("passwordless".getBytes());
        KdbxDatabase db = KdbxDatabase.load(creds, is);
        List<? extends Entry> entries = db.findEntries("testtitle");

        for (Entry entry: entries) {
            printStream.println(Helpers.fromDateV3(entry.getCreationTime()));
            assertEquals("2021-01-11T09:18:56Z", Helpers.fromDateV3(entry.getCreationTime()));
        }
    }

    /**
     * Verify that V4 dates are still processed correctly
     */
    @Test
    public void testV4Date() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("V4-AES-AES.kdbx");
        KdbxCredentials creds = new KdbxCredentials("123".getBytes());
        KdbxDatabase db = KdbxDatabase.load(creds, is);
        List<? extends Entry> entries = db.findEntries("Sample Entry #2 - Copy");

        for (Entry entry: entries) {
            printStream.println(Helpers.fromDate(entry.getCreationTime()));
            assertEquals("2018-01-26T13:20:58Z", Helpers.fromDateV3(entry.getCreationTime()));
        }
    }

    @Test
    public void testIssue27XML() throws IOException {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("issue-27/bogus-timestamp2.kdbx");
        KdbxCredentials creds = new KdbxCredentials("passwordless".getBytes());
        InputStream plainText = KdbxSerializer.createUnencryptedInputStream(creds,new KdbxHeader(), is);
        printStream.println(CharStreams.toString(new InputStreamReader(plainText, StandardCharsets.UTF_8)));
    }
}
