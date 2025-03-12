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

package org.linguafranca.pwdb.basic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BasicEntryTest {

    private BasicEntry entry;

    @BeforeEach
    void setUp() {
        entry = new BasicEntry(new BasicDatabase());
    }

    @Test
    void testGetProperty() {
        entry.setProperty("name", "value");
        assertEquals("value", entry.getProperty("name"));
    }

    @Test
    void testSetProperty() {
        entry.setProperty("name", "value");
        assertEquals("value", entry.getProperty("name"));
    }

    @Test
    void testRemoveProperty() {
        entry.setProperty("name", "value");
        assertTrue(entry.removeProperty("name"));
        assertNull(entry.getProperty("name"));
    }

    @Test
    void testGetPropertyNames() {
        entry.setProperty("name1", "value1");
        entry.setProperty("name2", "value2");
        List<String> propertyNames = entry.getPropertyNames();
        assertTrue(propertyNames.contains("name1"));
        assertTrue(propertyNames.contains("name2"));
    }

    @Test
    void testGetBinaryProperty() {
        byte[] data = {1, 2, 3};
        entry.setBinaryProperty("binary", data);
        assertArrayEquals(data, entry.getBinaryProperty("binary"));
    }

    @Test
    void testSetBinaryProperty() {
        byte[] data = {1, 2, 3};
        entry.setBinaryProperty("binary", data);
        assertArrayEquals(data, entry.getBinaryProperty("binary"));
    }

    @Test
    void testRemoveBinaryProperty() {
        byte[] data = {1, 2, 3};
        entry.setBinaryProperty("binary", data);
        assertTrue(entry.removeBinaryProperty("binary"));
        assertNull(entry.getBinaryProperty("binary"));
    }

    @Test
    void testGetBinaryPropertyNames() {
        byte[] data1 = {1, 2, 3};
        byte[] data2 = {4, 5, 6};
        entry.setBinaryProperty("binary1", data1);
        entry.setBinaryProperty("binary2", data2);
        List<String> binaryPropertyNames = entry.getBinaryPropertyNames();
        assertTrue(binaryPropertyNames.contains("binary1"));
        assertTrue(binaryPropertyNames.contains("binary2"));
    }

    @Test
    void testGetParent() {
        assertNull(entry.getParent());
    }

    @Test
    void testGetUuid() {
        UUID uuid = entry.getUuid();
        assertNotNull(uuid);
    }

    @Test
    void testGetIcon() {
        assertNotNull(entry.getIcon());
    }

    @Test
    void testSetIcon() {
        entry.setIcon(new BasicIcon(1) {});
        assertEquals(1, entry.getIcon().getIndex());
    }

    @Test
    void testGetLastAccessTime() {
        Date lastAccessTime = entry.getLastAccessTime();
        assertNotNull(lastAccessTime);
    }

    @Test
    void testGetCreationTime() {
        Date creationTime = entry.getCreationTime();
        assertNotNull(creationTime);
    }

    @Test
    void testGetExpires() {
        assertFalse(entry.getExpires());
    }

    @Test
    void testSetExpires() {
        entry.setExpires(true);
        assertTrue(entry.getExpires());
    }

    @Test
    void testGetExpiryTime() {
        assertNull(entry.getExpiryTime());
    }

    @Test
    void testSetExpiryTime() {
        Date expiryTime = Date.from(Instant.now());
        entry.setExpiryTime(expiryTime);
        assertEquals(expiryTime, entry.getExpiryTime());
    }

    @Test
    void testGetLastModificationTime() {
        assertEquals(entry.getCreationTime(), entry.getLastModificationTime());
    }
}
