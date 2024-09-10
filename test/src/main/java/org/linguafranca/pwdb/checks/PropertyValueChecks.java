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

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

/**
 * Testing the operation of PropertyValue mechanisms
 */
public abstract class PropertyValueChecks<D extends Database<D, G, E, I>, G extends Group<D, G, E, I>,
        E extends Entry<D, G, E, I>, I extends Icon> {

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
    private void catchException(Consumer<Boolean> f, Class<? extends Exception> c) {
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
    public void testNoPropertyValueSupport() {
        assumeFalse(propertyValueSupported);

        assertFalse(database.supportsPropertyValueStrategy());
        // test a sample of the methods that are supposed to throw if used here
        catchException((z) -> database.setPropertyValueStrategy(null), UnsupportedOperationException.class);
        catchException((z) -> database.getPropertyValueStrategy(), UnsupportedOperationException.class);
        catchException((z) -> database.setShouldProtect(null, true), UnsupportedOperationException.class);
        catchException((z) -> database.listShouldProtect(), UnsupportedOperationException.class);

        E entry = database.newEntry();
        catchException((z) -> entry.getPropertyValue(null), UnsupportedOperationException.class);
        catchException((z) -> entry.setPropertyValue(null, (PropertyValue) null), UnsupportedOperationException.class);

        // these are the defaults established by createDatabase()
        assertTrue(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_PASSWORD));
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_USER_NAME));
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_NOTES));
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_URL));
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_TITLE));
    }


}
