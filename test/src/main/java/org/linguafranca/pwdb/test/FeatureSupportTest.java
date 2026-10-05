/*
 * Copyright (c) 2026. Jo Rabin
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
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Feature;
import org.linguafranca.pwdb.Group;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that a database answers {@link Database#supports(Feature)} consistently with what its getters
 * and setters do: getters answer "nothing there" for an unsupported feature, setters throw
 * {@link UnsupportedOperationException} except when setting "nothing", and attributes are never null
 */
public interface FeatureSupportTest {

    void newDatabase();
    Database getDatabase();

    @BeforeEach
    default void featureSupportSetUp() {
        newDatabase();
    }

    @Test
    default void supportsAgreesWithSupportsMethods() {
        Database database = getDatabase();
        assertEquals(database.supportsNonStandardPropertyNames(), database.supports(Feature.AD_HOC_PROPERTIES));
        assertEquals(database.supportsBinaryProperties(), database.supports(Feature.BINARY_PROPERTIES));
        assertEquals(database.supportsRecycleBin(), database.supports(Feature.RECYCLE_BIN));
        assertEquals(database.supportsPropertyValueStrategy(), database.supports(Feature.PROPERTY_VALUE_STRATEGY));
        if (database.supports(Feature.MULTIPLE_BINARY_PROPERTIES)) {
            assertTrue(database.supports(Feature.BINARY_PROPERTIES));
        }
    }

    @Test
    default void streamFormatIsNotNull() {
        assertNotNull(getDatabase().getStreamFormat());
        assertNotNull(getDatabase().getStreamFormat().getStreamConfiguration());
    }

    @Test
    default void nameAndDescription() {
        Database database = getDatabase();
        assertNotNull(database.getName());
        assertNotNull(database.getDescription());

        database.setDescription(null);
        assertEquals("", database.getDescription());

        // setting no name is always allowed
        database.setName(null);
        assertEquals("", database.getName());
        database.setName("");
        assertEquals("", database.getName());

        if (database.supports(Feature.DATABASE_NAME)) {
            database.setName("A name");
            assertEquals("A name", database.getName());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> database.setName("A name"));
            assertEquals("", database.getName());
        }
    }

    @Test
    default void adHocProperties() {
        Entry entry = getDatabase().newEntry();
        for (String name : Entry.STANDARD_PROPERTY_NAMES) {
            assertNotNull(entry.getProperty(name), name);
            assertNotNull(entry.getPropertyValue(name), name);
            assertThrows(IllegalArgumentException.class, () -> entry.removeProperty(name), name);
        }
        assertNull(entry.getProperty("ad hoc"));
        assertNull(entry.getPropertyValue("ad hoc"));
        assertFalse(entry.removeProperty("ad hoc"));

        if (getDatabase().supports(Feature.AD_HOC_PROPERTIES)) {
            entry.setProperty("ad hoc", "value");
            assertEquals("value", entry.getProperty("ad hoc"));
            assertTrue(entry.removeProperty("ad hoc"));
        } else {
            assertThrows(UnsupportedOperationException.class, () -> entry.setProperty("ad hoc", "value"));
            assertNull(entry.getProperty("ad hoc"));
        }
    }

    @Test
    default void binaryProperties() {
        Entry entry = getDatabase().newEntry();
        assertNotNull(entry.getBinaryPropertyNames());
        assertTrue(entry.getBinaryPropertyNames().isEmpty());
        assertNull(entry.getBinaryProperty("one"));
        assertFalse(entry.removeBinaryProperty("one"));

        if (!getDatabase().supports(Feature.BINARY_PROPERTIES)) {
            assertThrows(UnsupportedOperationException.class, () -> entry.setBinaryProperty("one", new byte[]{1}));
            assertTrue(entry.getBinaryPropertyNames().isEmpty());
            return;
        }

        entry.setBinaryProperty("one", new byte[]{1});
        assertArrayEquals(new byte[]{1}, entry.getBinaryProperty("one"));
        // replacing the value of the same name is always allowed
        entry.setBinaryProperty("one", new byte[]{1, 1});
        assertArrayEquals(new byte[]{1, 1}, entry.getBinaryProperty("one"));

        if (getDatabase().supports(Feature.MULTIPLE_BINARY_PROPERTIES)) {
            entry.setBinaryProperty("two", new byte[]{2});
            assertEquals(2, entry.getBinaryPropertyNames().size());
        } else {
            assertThrows(UnsupportedOperationException.class, () -> entry.setBinaryProperty("two", new byte[]{2}));
            assertEquals(1, entry.getBinaryPropertyNames().size());
        }

        // the list returned can be changed by the caller
        entry.getBinaryPropertyNames().clear();
        assertTrue(entry.removeBinaryProperty("one"));
        assertNull(entry.getBinaryProperty("one"));
    }

    @Test
    default void propertyProtection() {
        Database database = getDatabase();
        if (database.supports(Feature.PROPERTY_VALUE_STRATEGY)) {
            assertNotNull(database.getPropertyValueStrategy());
            return;
        }
        assertFalse(database.shouldProtect(Entry.STANDARD_PROPERTY_NAME_PASSWORD));
        assertTrue(database.listShouldProtect().isEmpty());
        // setting "not protected" is always allowed
        database.setShouldProtect(Entry.STANDARD_PROPERTY_NAME_PASSWORD, false);
        assertThrows(UnsupportedOperationException.class,
                () -> database.setShouldProtect(Entry.STANDARD_PROPERTY_NAME_PASSWORD, true));
        // there is no strategy to return
        assertThrows(UnsupportedOperationException.class, database::getPropertyValueStrategy);
    }

    @Test
    default void recycleBin() {
        Database database = getDatabase();
        if (database.supports(Feature.RECYCLE_BIN)) {
            return;
        }
        assertFalse(database.isRecycleBinEnabled());
        assertNull(database.getRecycleBin());
        // setting "not enabled" is always allowed
        database.enableRecycleBin(false);
        assertThrows(UnsupportedOperationException.class, () -> database.enableRecycleBin(true));
    }

    @Test
    default void entryAttributesAreNotNull() {
        Entry entry = getDatabase().newEntry();
        assertNotNull(entry.getUuid());
        assertNotNull(entry.getTitle());
        assertNotNull(entry.getUsername());
        assertNotNull(entry.getUrl());
        assertNotNull(entry.getNotes());
        assertNotNull(entry.getIcon());
        assertNotNull(entry.getPropertyNames());
        assertNotNull(entry.getBinaryPropertyNames());
        assertNotNull(entry.getCreationTime());
        assertNotNull(entry.getLastModificationTime());
        assertNotNull(entry.getLastAccessTime());
        assertNotNull(entry.getExpiryTime());
        assertSame(getDatabase(), entry.getDatabase());
    }

    @Test
    default void groupAttributesAreNotNull() {
        Group group = getDatabase().newGroup();
        assertNotNull(group.getUuid());
        assertNotNull(group.getName());
        assertNotNull(group.getIcon());
        assertNotNull(group.getGroups());
        assertNotNull(group.getEntries());
        assertNotNull(group.getCreationTime());
        assertNotNull(group.getLastModificationTime());
        assertNotNull(group.getLastAccessTime());
        assertNotNull(group.getExpiryTime());
        assertSame(getDatabase(), group.getDatabase());
        assertSame(getDatabase(), getDatabase().getRootGroup().getDatabase());
    }
}
