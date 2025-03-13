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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class DatabaseTestBase {

    @FunctionalInterface
    public interface TriConsumer<A,B,C> {

        void accept(A a, B b, C c);

        default TriConsumer<A, B, C> andThen(TriConsumer<? super A, ? super B, ? super C> after) {
            Objects.requireNonNull(after);

            return (l, r,c) -> {
                accept(l, r, c);
                after.accept(l, r, c);
            };
        }
    }

    static String OUTPUT_DIRECTORY_PATH = "testOutput";

    @BeforeAll
    static void baseBeforeAll() throws Exception {
        Files.createDirectories(Paths.get(OUTPUT_DIRECTORY_PATH));
    }

    protected Database database;

    Supplier<Database> creator;
    BiFunction<Credentials, InputStream, Database> loader;
    TriConsumer<Database, Credentials, OutputStream> saver;
    Function<byte[], Credentials> credentials;

    public DatabaseTestBase(Supplier<Database> creator,
                            BiFunction<Credentials, InputStream, Database> loader,
                            TriConsumer<Database, Credentials, OutputStream> saver,
                            Function<byte[], Credentials> credentials) {
        this.creator = creator;
        this.loader = loader;
        this.saver = saver;
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

    public Database loadDatabase(byte[] credentials, String resourceName) {
        return loader.apply(getCredentials(credentials), getClass().getClassLoader().getResourceAsStream(resourceName));
    }

    public Database loadDatabase(Credentials credentials, InputStream inputStream) {
        return loader.apply(credentials, inputStream);
    }

    public void saveDatabase(Database database, Credentials credentials, OutputStream outputStream){
        saver.accept(database, credentials, outputStream);
    }

    public Credentials getCredentials(byte[] credentials){
        return this.credentials.apply(credentials);
    }
}
