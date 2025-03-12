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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.PropertyValue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public interface ProtectedPropertyTest2 {
    
        void newDatabase();
        Database getDatabase();
        void saveDatabase(Database database, Credentials credentials, OutputStream outputStream) throws IOException;
        Database loadDatabase(Credentials credentials, InputStream inputStream) throws IOException;
        Credentials getCredentials(byte[] credentials);

        @BeforeEach
        default void ppt2SetUp() throws Exception {
            newDatabase();
        }
        
        /**
         * Catches expected thrown exception
         *
         * @param f a Consumer i.e. a function that does have output
         * @param c the exception that is expected to be thrown when the function is executed
         */
        private void catchException(Consumer<Boolean> f, @SuppressWarnings("SameParameterValue") Class<? extends Exception> c) {
            try {
                f.accept(null);
            } catch (Exception e) {
                if (c.equals(e.getClass())) {
                    return;
                }
            }
            fail(c.getName() + " expected");
        }

        /**
         * Check that unsupported operations throw exceptions
         */
        @Test
        default void noPropertyValueSupport() {
            if (getDatabase().supportsPropertyValueStrategy()) return;

            assertFalse(getDatabase().supportsPropertyValueStrategy());
            // test a sample of the methods that are supposed to thrown if used here
            catchException((z) -> getDatabase().setPropertyValueStrategy(null), UnsupportedOperationException.class);
            catchException((z) -> getDatabase().getPropertyValueStrategy(), UnsupportedOperationException.class);
            catchException((z) -> getDatabase().setShouldProtect(null, true), UnsupportedOperationException.class);
            catchException((z) -> getDatabase().listShouldProtect(), UnsupportedOperationException.class);

            Entry entry = getDatabase().newEntry();
            catchException((z) -> entry.getPropertyValue(null), UnsupportedOperationException.class);
            catchException((z) -> entry.setPropertyValue(null, (PropertyValue) null), UnsupportedOperationException.class);

        }

        @Test
        default void checkDefaults(){
            // these are the defaults established by createDatabase()
            assertTrue(getDatabase().shouldProtect(Entry.STANDARD_PROPERTY_NAME_PASSWORD));
            assertFalse(getDatabase().shouldProtect(Entry.STANDARD_PROPERTY_NAME_USER_NAME));
            assertFalse(getDatabase().shouldProtect(Entry.STANDARD_PROPERTY_NAME_NOTES));
            assertFalse(getDatabase().shouldProtect(Entry.STANDARD_PROPERTY_NAME_URL));
            assertFalse(getDatabase().shouldProtect(Entry.STANDARD_PROPERTY_NAME_TITLE));
        }

        @Test
        default void expectedStorageType() throws IOException {
            if (!getDatabase().supportsPropertyValueStrategy()) {
                return;
            }

            // Password is protected
            PropertyValue.Strategy pvs = getDatabase().getPropertyValueStrategy();
            PropertyValue pv = pvs.getFactoryFor("Password").of("123");
            assertEquals(pv.getClass(), pvs.newProtected().of("a").getClass());
            assertTrue(pv.isProtected());

            // Notes isn't protected
            pv = pvs.getFactoryFor("Notes").of("123");
            assertEquals(pv.getClass(), pvs.newUnprotected().of("b").getClass());
            assertFalse(pv.isProtected());

            // "random" is not protected
            pv = pvs.getFactoryFor("random").of("123");
            assertEquals(pv.getClass(), pvs.newUnprotected().of("b").getClass());
            assertFalse(pv.isProtected());

            // create a strategy that makes "random" protected
            PropertyValue.Strategy newPvs = new PropertyValue.Strategy.MutableProtectionStrategy();
            getDatabase().setPropertyValueStrategy(newPvs);
            getDatabase().setShouldProtect("random", true);

            // get a value for random and check it is protected
            pv = newPvs.getFactoryFor("random").of("123");
            assertEquals(pv.getClass(), pvs.newProtected().of("b").getClass());
            assertTrue(pv.isProtected());

            // create an entry with random property as protected value
            Entry entry = getDatabase().newEntry("Test Random");
            entry.setPropertyValue("random", pv);
            getDatabase().getRootGroup().addEntry(entry);

            // Save database

            FileOutputStream fos = new FileOutputStream("testOutput/test9.kdbx");
            saveDatabase(getDatabase(), getCredentials("123".getBytes()), fos);
            fos.flush();
            fos.close();

            // reload database, "random" is still protected even though it's not protected by default
            FileInputStream fis = new FileInputStream("testOutput/test9.kdbx");
            Database input = loadDatabase(getCredentials("123".getBytes()), fis);

            List<? extends Entry> entries = input.findEntries("random");
            assertEquals(1, entries.size());
            assertTrue(entries.get(0).getPropertyValue("random").isProtected());
            assertFalse(input.getPropertyValueStrategy().getProtectedProperties().contains("random"));
        }

    }

