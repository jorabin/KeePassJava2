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

package org.linguafranca.pwdb.checks;

import org.junit.Test;
import org.linguafranca.pwdb.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * Testing the operation of PropertyValue mechanisms
 */
public abstract class PropertyValueChecks<D extends Database<D, G, E, I>, G extends Group<D, G, E, I>,
        E extends Entry<D, G, E, I>, I extends Icon> {

    public abstract void saveDatabase(D database, Credentials credentials, OutputStream outputStream) throws IOException;
    public abstract D loadDatabase(Credentials credentials, InputStream inputStream) throws IOException;
    public abstract Credentials getCreds(byte[] creds);

    private final boolean propertyValueSupported;
    protected D database;

    public PropertyValueChecks(boolean propertyValueSupported) throws IOException {
        this.database = createDatabase();
        this.propertyValueSupported = propertyValueSupported;
    }

    public abstract D createDatabase() throws IOException;

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
    public void noPropertyValueSupport() {
        if (propertyValueSupported) return;

        assertFalse(database.supportsPropertyValueStrategy());
        // test a sample of the methods that are supposed to thrown if used here
        catchException((z) -> database.setPropertyValueStrategy(null), UnsupportedOperationException.class);
        catchException((z) -> database.getPropertyValueStrategy(), UnsupportedOperationException.class);
        catchException((z) -> database.setShouldProtect(null, true), UnsupportedOperationException.class);
        catchException((z) -> database.listShouldProtect(), UnsupportedOperationException.class);

        E entry = database.newEntry();
        catchException((z) -> entry.getPropertyValue(null), UnsupportedOperationException.class);
        catchException((z) -> entry.setPropertyValue(null, (PropertyValue) null), UnsupportedOperationException.class);

    }

    @Test
    public void checkDefaults(){
        // these are the defaults established by createDatabase()
        assertTrue(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_PASSWORD));
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_USER_NAME));
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_NOTES));
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_URL));
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_TITLE));
    }

    @Test
    public void expectedStorageType(){
        if (!propertyValueSupported) {
            return;
        }

        // Password is protected
        PropertyValue.Strategy pvs = database.getPropertyValueStrategy();
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
        PropertyValue.Strategy newPvs = new PropertyValue.Strategy() {
            @Override
            public List<String> getProtectedProperties() {
                List<String> pp = new ArrayList<>(pvs.getProtectedProperties());
                pp.add("random");
                return pp;
            }

            @Override
            public PropertyValue.Factory<? extends PropertyValue> newProtected() {
                return pvs.newProtected();
            }

            @Override
            public PropertyValue.Factory<? extends PropertyValue> newUnprotected() {
                return pvs.newUnprotected();
            }
        };
        database.setPropertyValueStrategy(newPvs);

        // get a value for random and check it is protected
        pv = newPvs.getFactoryFor("random").of("123");
        assertEquals(pv.getClass(), pvs.newProtected().of("b").getClass());
        assertTrue(pv.isProtected());

        // create an entry with random property as protected value
        E entry = database.newEntry("Test Random");
        entry.setPropertyValue("random", pv);
        database.getRootGroup().addEntry(entry);

        // Save database

        try {
            FileOutputStream fos = new FileOutputStream("testOutput/propertyValueReload.kdbx");
            saveDatabase(database, getCreds("123".getBytes()), fos);
            fos.flush();
            fos.close();

            // reload database, "random" is still protected even though it's not protected by default
            FileInputStream fis = new FileInputStream("testOutput/propertyValueReload.kdbx");
            D input = loadDatabase(getCreds("123".getBytes()), fis);
            List<? extends E> entries = input.findEntries("random");
            assertEquals(1, entries.size());
            assertTrue(entries.get(0).getPropertyValue("random").isProtected());
            assertFalse(input.getPropertyValueStrategy().getProtectedProperties().contains("random"));


        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
