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

package org.linguafranca.pwdb.kdbx.jaxb;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.checks.SaveAndReloadChecks;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jo
 */
public class JaxbSaveAndReloadTest extends SaveAndReloadChecks {
    @Override
    public Database getDatabase() {
        return new JaxbDatabase();
    }
    @Override
    public Database getDatabase(String name, Credentials credentials) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        return JaxbDatabase.load(credentials, inputStream);
    }

    @Override
    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException {
        database.save(credentials, outputStream);
    }

    @Override
    public Database loadDatabase(Credentials credentials, InputStream inputStream) throws IOException {
        return JaxbDatabase.load(credentials, inputStream);
    }

    @Override
    public Credentials getCreds(byte[] creds) {
        return new KdbxCreds(creds);
    }
}
