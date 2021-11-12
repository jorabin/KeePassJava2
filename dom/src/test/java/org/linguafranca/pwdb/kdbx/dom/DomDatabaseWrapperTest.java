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

package org.linguafranca.pwdb.kdbx.dom;

import org.linguafranca.pwdb.checks.BasicDatabaseChecks;
import org.junit.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.pwdb.Credentials;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jo
 */
public class DomDatabaseWrapperTest extends BasicDatabaseChecks {

    public DomDatabaseWrapperTest() throws IOException {
    }

    @Test
    public void inspectPasswordDatabase() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxStreamFormat(), new KdbxCreds("123".getBytes()), inputStream);

        database.save(new StreamFormat.None(), new Credentials.None(), System.out);
    }

    @Test
    public void inspectKeyfileDatabase() throws IOException {
        InputStream keyFileInputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.key");
        Credentials credentials = new KdbxCreds("123".getBytes(), keyFileInputStream);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxStreamFormat(), credentials, inputStream);

        database.save(new StreamFormat.None(), new Credentials.None(), System.out);
    }

    @Override
    public Database createDatabase() throws IOException {
        return new DomDatabaseWrapper();
    }
}