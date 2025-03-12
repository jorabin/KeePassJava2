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
package org.linguafranca.pwdb.kdbx.database;

import org.junit.jupiter.api.BeforeAll;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;
import org.linguafranca.pwdb.test.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KdbxBinariesV3Test implements
        BinaryPropertiesTest {

    static String OUTPUT_DIRECTORY_PATH = "testOutput";
    public String databaseName = "Attachment.kdbx";

    @BeforeAll
    static void ppt2BeforeAll() throws Exception {
        Files.createDirectories(Paths.get(OUTPUT_DIRECTORY_PATH));
    }

    Database database;
    {
        newDatabase();
    }
    /**
     * Create a new database
     */
    @Override
    public Database createDatabase() {
        return new KdbxDatabase();
    }

    /**
     * Create a new database for default use in tests
     */
    @Override
    public void newDatabase() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(databaseName);
        try {
            database = KdbxDatabase.load(new KdbxCredentials("123".getBytes()),inputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get the current default database
     */
    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException{
        database.save(credentials, outputStream);
    }

    @Override
    public Database loadDatabase(Credentials credentials, InputStream inputStream) throws IOException{
        return KdbxDatabase.load(credentials, inputStream);
    }

    @Override
    public Credentials getCredentials(byte[] credentials){
        return new KdbxCredentials(credentials);
    }
}
