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
package org.linguafranca.pwdb.test;

import org.junit.jupiter.api.BeforeAll;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Supplier;
import org.linguafranca.util.TestUtil;

public class DatabaseTestBase {

    /**
     * Reads a database, e.g. {@code KdbxDatabase::read}, leaving the stream open
     */
    @FunctionalInterface
    public interface Reader {
        Database read(Credentials credentials, InputStream inputStream) throws IOException;
    }

    /**
     * Writes a database, e.g. {@code Database::write}, leaving the stream open
     */
    @FunctionalInterface
    public interface Writer {
        void write(Database database, Credentials credentials, OutputStream outputStream) throws IOException;
    }

    public static String OUTPUT_DIRECTORY_PATH = TestUtil.TEST_OUTPUT_DIR;

    @BeforeAll
    static void baseBeforeAll() throws Exception {
        Files.createDirectories(Paths.get(OUTPUT_DIRECTORY_PATH));
    }

    protected Database database;

    Supplier<Database> creator;
    Reader reader;
    Writer writer;
    Function<byte[], Credentials> credentials;

    public DatabaseTestBase(Supplier<Database> creator,
                            Reader reader,
                            Writer writer,
                            Function<byte[], Credentials> credentials) {
        this.creator = creator;
        this.reader = reader;
        this.writer = writer;
        this.credentials = credentials;
    }
    /**
     * Create a new database
     */
    public Database createDatabase() {
        return this.creator.get();
    }

    /**
     * Create a new database for default use in tests
     */
    public void newDatabase() {
        database = createDatabase();
    }

    /**
     * Get the current default database
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Read a database from a resource, closing the resource stream
     */
    public Database loadDatabase(byte[] credentials, String resourceName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            return loadDatabase(getCredentials(credentials), inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read a database, leaving the stream open
     */
    public Database loadDatabase(Credentials credentials, InputStream inputStream) {
        try {
            return reader.read(credentials, inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Write a database, leaving the stream open
     */
    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream){
        try {
            writer.write(database, credentials, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Credentials getCredentials(byte[] credentials){
        return this.credentials.apply(credentials);
    }
}
