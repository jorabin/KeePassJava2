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

import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Content of the XML that only some KDBX versions can hold, following KeePass's descriptions of
 * <a href="https://keepass.info/help/kb/kdbx_4.html">KDBX 4</a> and
 * <a href="https://keepass.info/help/kb/kdbx_4.1.html">KDBX 4.1</a>:
 * <ul>
 *     <li>4.0 adds CustomData on groups and entries, and SettingsChanged in Meta</li>
 *     <li>4.1 adds Tags and PreviousParentGroup on groups, QualityCheck and PreviousParentGroup on
 *     entries, Name and LastModificationTime on custom icons, and LastModificationTime on CustomData
 *     items</li>
 * </ul>
 * Used to choose the version to write, and to leave out, for the duration of a write, content that the
 * version being written can't hold.
 */
class KdbxVersionContent {

    private final int majorVersion;
    private final int minorVersion;
    private final boolean remove;
    /* what was found, by description, with a count */
    private final Map<String, Integer> found = new TreeMap<>();
    /* puts back what was removed */
    private final List<Runnable> restore = new ArrayList<>();

    private KdbxVersionContent(int majorVersion, int minorVersion, boolean remove) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.remove = remove;
    }

    /**
     * The lowest minor version of the major version given that holds all the content
     */
    static int minimumMinorVersion(KeePassFile keePassFile, int majorVersion) {
        if (majorVersion != 4) {
            return 1;
        }
        KdbxVersionContent content = new KdbxVersionContent(4, 0, false);
        content.visit(keePassFile);
        return content.found.isEmpty() ? 0 : 1;
    }

    /**
     * Remove the content the version given can't hold
     *
     * @return the removal, to {@link #restore()} after writing and to report what was removed
     */
    static KdbxVersionContent remove(KeePassFile keePassFile, int majorVersion, int minorVersion) {
        KdbxVersionContent content = new KdbxVersionContent(majorVersion, minorVersion, true);
        content.visit(keePassFile);
        return content;
    }

    /**
     * Put back the content that was removed
     */
    void restore() {
        restore.forEach(Runnable::run);
    }

    /**
     * What was removed, e.g. "group Tags (3)", empty if nothing
     */
    List<String> removed() {
        List<String> result = new ArrayList<>();
        found.forEach((description, count) -> result.add(description + " (" + count + ")"));
        return result;
    }

    private boolean holds40() {
        return majorVersion >= 4;
    }

    private boolean holds41() {
        return majorVersion > 4 || (majorVersion == 4 && minorVersion >= 1);
    }

    /* note a value the version can't hold and, if removing, remove it and remember how to put it back */
    private <T> void check(boolean held, String description, Supplier<T> getter, Consumer<T> setter) {
        T value = getter.get();
        // empty values aren't written (see KdbxSerializableDatabase), so they don't count
        if (held || Objects.isNull(value) || (value instanceof String && ((String) value).isEmpty())) {
            return;
        }
        found.merge(description, 1, Integer::sum);
        if (remove) {
            setter.accept(null);
            restore.add(() -> setter.accept(value));
        }
    }

    private void visit(KeePassFile keePassFile) {
        KeePassFile.Meta meta = keePassFile.meta;
        if (meta != null) {
            check(holds40(), "Meta SettingsChanged", () -> meta.settingsChanged, v -> meta.settingsChanged = v);
            visitCustomDataItems(meta.customData, "Meta CustomData item LastModificationTime");
            if (meta.customIcons != null) {
                for (KeePassFile.Icon icon : meta.customIcons) {
                    check(holds41(), "custom icon Name", () -> icon.name, v -> icon.name = v);
                    check(holds41(), "custom icon LastModificationTime", () -> icon.lastModificationTime, v -> icon.lastModificationTime = v);
                }
            }
        }
        if (keePassFile.root != null && keePassFile.root.group != null) {
            visit(keePassFile.root.group);
        }
    }

    private void visit(KdbxGroup group) {
        check(holds41(), "group Tags", () -> group.tags, v -> group.tags = v);
        check(holds41(), "group PreviousParentGroup", () -> group.previousParentGroup, v -> group.previousParentGroup = v);
        visitCustomDataItems(group.customData, "group CustomData item LastModificationTime");
        check(holds40(), "group CustomData", () -> group.customData, v -> group.customData = v);
        if (group.entries != null) {
            for (KdbxEntry entry : group.entries) {
                visit(entry, "entry");
                if (entry.history != null && entry.history.getEntry() != null) {
                    for (KdbxEntry historyEntry : entry.history.getEntry()) {
                        visit(historyEntry, "history entry");
                    }
                }
            }
        }
        if (group.groups != null) {
            for (KdbxGroup child : group.groups) {
                visit(child);
            }
        }
    }

    private void visit(KdbxEntry entry, String kind) {
        check(holds41(), kind + " QualityCheck", () -> entry.qualityCheck, v -> entry.qualityCheck = v);
        check(holds41(), kind + " PreviousParentGroup", () -> entry.previousParentGroup, v -> entry.previousParentGroup = v);
        visitCustomDataItems(entry.customData, kind + " CustomData item LastModificationTime");
        check(holds40(), kind + " CustomData", () -> entry.customData, v -> entry.customData = v);
    }

    private void visitCustomDataItems(KeePassFile.CustomData customData, String description) {
        if (customData == null || customData.getItems() == null) {
            return;
        }
        for (KeePassFile.CustomData.CustomDataItem item : customData.getItems()) {
            check(holds41(), description, () -> item.lastModificationTime, v -> item.lastModificationTime = v);
        }
    }
}
