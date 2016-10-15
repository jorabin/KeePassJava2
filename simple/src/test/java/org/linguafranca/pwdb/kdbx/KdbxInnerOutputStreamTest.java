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

package org.linguafranca.pwdb.kdbx;

import org.junit.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleEntry;
import org.linguafranca.pwdb.security.Credentials;
import org.linguafranca.xml.XmlOutputStreamFilter;

import java.security.SecureRandom;

/**
 * @author jo
 */
public class KdbxInnerOutputStreamTest {

    @Test
    public void test() throws Exception {
        final SimpleDatabase database = new SimpleDatabase();
        final SimpleEntry entry = database.newEntry();
        entry.setTitle("Password Encyption Test");
        entry.setPassword("password");
        database.getRootGroup().addEntry(entry);
        database.save(new KdbxCreds.None(), System.out);
   }

}