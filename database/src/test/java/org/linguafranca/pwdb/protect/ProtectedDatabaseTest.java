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

package org.linguafranca.pwdb.protect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.basic.BasicDatabase;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProtectedDatabaseTest {

    private ProtectedDatabase database;

    @BeforeEach
    void setUp() {
        database = new BasicDatabase();
    }

    @Test
    void testShouldProtect() {
        String propertyName = "password";
        database.setShouldProtect(propertyName, true);
        assertTrue(database.shouldProtect(propertyName));
    }

    @Test
    void testSetShouldProtect() {
        String propertyName = "password";
        database.setShouldProtect(propertyName, true);
        assertTrue(database.shouldProtect(propertyName));

        database.setShouldProtect(propertyName, false);
        assertFalse(database.shouldProtect(propertyName));
    }

    @Test
    void testListShouldProtect() {
        String propertyName1 = "password";
        String propertyName2 = "username";
        database.setShouldProtect(propertyName1, true);
        database.setShouldProtect(propertyName2, true);

        List<String> protectedProperties = database.listShouldProtect();
        assertTrue(protectedProperties.contains(propertyName1));
        assertTrue(protectedProperties.contains(propertyName2));
    }

    @Test
    void testGetPropertyValueStrategy() {
        PropertyValue.Strategy strategy = database.getPropertyValueStrategy();
        assertNotNull(strategy);
    }

    @Test
    void testSetPropertyValueStrategy() {
        PropertyValue.Strategy newStrategy = new PropertyValue.Strategy.Default();
        database.setPropertyValueStrategy(newStrategy);
        assertEquals(newStrategy, database.getPropertyValueStrategy());
    }

    @Test
    void testSupportsPropertyValueStrategy() {
        assertTrue(database.supportsPropertyValueStrategy());
    }
}