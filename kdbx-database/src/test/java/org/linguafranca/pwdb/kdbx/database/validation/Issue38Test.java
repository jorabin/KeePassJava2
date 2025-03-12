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

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * Bug report on GitHub, the Keyfile is Version 2 (Hex)
 */
public class Issue38Test {

    static PrintStream printStream = getTestPrintStream();
    @Test
    public void testV2Keyfile() throws IOException {
        InputStream databaseStream = Issue38Test.class.getClassLoader().getResourceAsStream("issue-38/Database.kdbx");
        InputStream keyStream = Issue38Test.class.getClassLoader().getResourceAsStream("issue-38/Database.keyx");
        assert keyStream != null;
        KdbxCredentials creds = new KdbxCredentials("MyPassword".getBytes(), keyStream);
        assert databaseStream != null;
        KdbxDatabase database = KdbxDatabase.load(creds, databaseStream);
        List<? extends Entry> entries = database.findEntries("Sample Entry");
        Entry entry = entries.get(0);
        printStream.println(entry.getTitle());
    }
}
