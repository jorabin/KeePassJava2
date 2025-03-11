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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME.PASSWORD;
import static org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME.USER_NAME;

public interface ProtectedPropertyTest {

    void newDatabase();
    
    Database getDatabase();

    @BeforeEach
    default void setup() throws Exception {
        newDatabase();
    }

    @Test
    default void testShouldProtect() {
        String propertyName = PASSWORD;
        assertThrows(UnsupportedOperationException.class,
                () -> getDatabase().setShouldProtect(propertyName, true),
                "By default the ProtectionStrategy should be immutable");
        assertTrue(getDatabase().shouldProtect(propertyName));
    }

    @Test
    default void testSetShouldProtect() {
        getDatabase().setPropertyValueStrategy(new PropertyValue.Strategy.MutableProtectionStrategy());
        String propertyName = PASSWORD;
        getDatabase().setShouldProtect(propertyName, true);
        assertTrue(getDatabase().shouldProtect(propertyName));

        getDatabase().setShouldProtect(propertyName, false);
        assertFalse(getDatabase().shouldProtect(propertyName));
    }

    @Test
    default void testListShouldProtect() {
        getDatabase().setPropertyValueStrategy(new PropertyValue.Strategy.MutableProtectionStrategy());
        String propertyName1 = PASSWORD;
        String propertyName2 = USER_NAME;
        getDatabase().setShouldProtect(propertyName1, true);
        getDatabase().setShouldProtect(propertyName2, true);

        List<String> protectedProperties = getDatabase().listShouldProtect();
        assertTrue(protectedProperties.contains(propertyName1));
        assertTrue(protectedProperties.contains(propertyName2));
    }

    @Test
    default void testGetPropertyValueStrategy() {
        PropertyValue.Strategy strategy = getDatabase().getPropertyValueStrategy();
        assertNotNull(strategy);
    }

    @Test
    default void testSetPropertyValueStrategy() {
        PropertyValue.Strategy newStrategy = new PropertyValue.Strategy.Default();
        getDatabase().setPropertyValueStrategy(newStrategy);
        assertEquals(newStrategy, getDatabase().getPropertyValueStrategy());
    }

    @Test
    default void testSupportsPropertyValueStrategy() {
        assertTrue(getDatabase().supportsPropertyValueStrategy());
    }
}