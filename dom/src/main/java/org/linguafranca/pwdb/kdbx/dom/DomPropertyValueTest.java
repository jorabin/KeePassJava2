/*
 * Copyright 2024 Jo Rabin
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

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.checks.PropertyValueChecks;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DomPropertyValueTest extends PropertyValueChecks {

    public DomPropertyValueTest() throws IOException {
        super(false);
    }


    @Override
    public Database createDatabase() throws IOException {
        return new DomDatabaseWrapper();
    }

    @Override
    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException {
        database.save(credentials, outputStream);
    }

    @Override
    public Database loadDatabase(Credentials credentials, InputStream inputStream) throws IOException {
        try {
            return DomDatabaseWrapper.load(credentials, inputStream);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Credentials getCreds(byte[] creds) {
        return new KdbxCreds(creds);
    }
}
