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

package org.linguafranca.pwdb.kdbx.jackson;

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Each kind of content that only some KDBX versions hold is found, removed for the versions that can't
 * hold it, and put back
 */
public class KdbxVersionContentTest {

    private static final Instant TIME = Instant.parse("2026-10-03T12:00:00Z");
    private static final UUID GROUP_UUID = UUID.randomUUID();

    private static KeePassFile.CustomData customData() {
        KeePassFile.CustomData.CustomDataItem item = new KeePassFile.CustomData.CustomDataItem();
        item.key = "key";
        item.value = "value";
        item.lastModificationTime = TIME;
        KeePassFile.CustomData customData = new KeePassFile.CustomData();
        customData.setItems(new ArrayList<>(List.of(item)));
        return customData;
    }

    /** a database with one of each kind of content */
    private static KdbxDatabase database() {
        KdbxDatabase database = new KdbxDatabase();
        KeePassFile keePassFile = database.keePassFile;

        keePassFile.meta.settingsChanged = TIME;
        keePassFile.meta.customData = customData();
        KeePassFile.Icon icon = new KeePassFile.Icon();
        icon.uuid = UUID.randomUUID();
        icon.data = new byte[]{1};
        icon.name = "icon";
        icon.lastModificationTime = TIME;
        keePassFile.meta.customIcons = new ArrayList<>(List.of(icon));

        KdbxGroup group = (KdbxGroup) database.getRootGroup().addGroup("group");
        group.tags = "tag";
        group.previousParentGroup = GROUP_UUID;
        group.customData = customData();

        KdbxEntry entry = (KdbxEntry) group.addEntry("entry");
        entry.qualityCheck = false;
        entry.previousParentGroup = GROUP_UUID;
        entry.customData = customData();
        return database;
    }

    @Test
    public void minimumMinorVersion() {
        assertEquals(1, KdbxVersionContent.minimumMinorVersion(database().keePassFile, 4));
        assertEquals(0, KdbxVersionContent.minimumMinorVersion(new KdbxDatabase().keePassFile, 4));
    }

    @Test
    public void nothingRemovedFor41() {
        KdbxVersionContent removed = KdbxVersionContent.remove(database().keePassFile, 4, 1);
        assertTrue(removed.removed().isEmpty(), removed.removed().toString());
    }

    @Test
    public void removedFor40() {
        KdbxDatabase database = database();
        KdbxVersionContent removed = KdbxVersionContent.remove(database.keePassFile, 4, 0);
        assertEquals(List.of(
                "Meta CustomData item LastModificationTime (1)",
                "custom icon LastModificationTime (1)",
                "custom icon Name (1)",
                "entry CustomData item LastModificationTime (1)",
                "entry PreviousParentGroup (1)",
                "entry QualityCheck (1)",
                "group CustomData item LastModificationTime (1)",
                "group PreviousParentGroup (1)",
                "group Tags (1)"), removed.removed());

        KdbxGroup group = (KdbxGroup) database.getRootGroup().getGroups().get(0);
        KdbxEntry entry = (KdbxEntry) group.getEntries().get(0);
        assertNull(group.tags);
        assertNull(entry.qualityCheck);
        assertNull(database.keePassFile.meta.customIcons.get(0).name);
        // 4.0 content stays
        assertNotNull(group.customData);
        assertEquals(TIME, database.keePassFile.meta.settingsChanged);

        removed.restore();
        assertEquals("tag", group.tags);
        assertEquals(GROUP_UUID, group.previousParentGroup);
        assertEquals(false, entry.qualityCheck);
        assertEquals(GROUP_UUID, entry.previousParentGroup);
        assertEquals("icon", database.keePassFile.meta.customIcons.get(0).name);
        assertEquals(TIME, database.keePassFile.meta.customIcons.get(0).lastModificationTime);
        assertEquals(TIME, entry.customData.getItems().get(0).lastModificationTime);
    }

    @Test
    public void removedFor31() {
        KdbxDatabase database = database();
        KdbxVersionContent removed = KdbxVersionContent.remove(database.keePassFile, 3, 1);
        assertTrue(removed.removed().contains("Meta SettingsChanged (1)"));
        assertTrue(removed.removed().contains("group CustomData (1)"));
        assertTrue(removed.removed().contains("entry CustomData (1)"));
        assertTrue(removed.removed().contains("group Tags (1)"));

        KdbxGroup group = (KdbxGroup) database.getRootGroup().getGroups().get(0);
        KdbxEntry entry = (KdbxEntry) group.getEntries().get(0);
        assertNull(group.customData);
        assertNull(entry.customData);
        assertNull(database.keePassFile.meta.settingsChanged);
        // Meta CustomData is in 3.1, its items' times aren't
        assertNotNull(database.keePassFile.meta.customData);
        assertNull(database.keePassFile.meta.customData.getItems().get(0).lastModificationTime);

        removed.restore();
        assertNotNull(group.customData);
        assertNotNull(entry.customData);
        assertEquals(TIME, database.keePassFile.meta.settingsChanged);
        assertEquals(TIME, group.customData.getItems().get(0).lastModificationTime);
    }
}
