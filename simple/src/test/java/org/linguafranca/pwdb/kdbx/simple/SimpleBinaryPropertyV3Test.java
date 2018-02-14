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

package org.linguafranca.pwdb.kdbx.simple;

import org.linguafranca.pwdb.checks.BinaryPropertyChecks;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jo
 */
public class SimpleBinaryPropertyV3Test extends BinaryPropertyChecks {

    public SimpleBinaryPropertyV3Test() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Attachment.kdbx");
        try {
            database = SimpleDatabase.load(new KdbxCreds("123".getBytes()),inputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException {
        database.save(credentials, outputStream);
    }
}
