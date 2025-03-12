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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.test.Test123Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jo
 */
public class Kdb123Test implements Test123Test {

    KdbDatabase kdbDatabase;

    Kdb123Test() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdb");
        // file has password credentials
        Credentials credentials = new KdbCredentials.Password("123".getBytes());
        // open database.
        kdbDatabase = KdbDatabase.load(credentials, inputStream);
    }

    @Override
    public Database getDatabase() {
        return kdbDatabase;
    }

    @Override
    public boolean getSkipDateCheck() {
        return true;
    }
}
