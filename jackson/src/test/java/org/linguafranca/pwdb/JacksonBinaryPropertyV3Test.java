/*
 * Copyright 2023 Giuseppe Valente
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.linguafranca.pwdb.checks.BinaryPropertyChecks;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;

public class JacksonBinaryPropertyV3Test extends BinaryPropertyChecks {


    public JacksonBinaryPropertyV3Test() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Attachment.kdbx");
        try {
            database = JacksonDatabase.load(new KdbxCreds("123".getBytes()),inputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException {
        database.save(credentials, outputStream);
    }

    @Override
    public Database loadDatabase(Credentials credentials, InputStream inputStream) throws IOException {
        return JacksonDatabase.load(credentials, inputStream);
    }

    @Override
    public Database newDatabase() {
        Database result = null;
        try {
            result = new JacksonDatabase();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return result;
        
    }

    @Override
    public Credentials getCreds(byte[] creds) {
        return new KdbxCreds(creds);
    }
    
}
