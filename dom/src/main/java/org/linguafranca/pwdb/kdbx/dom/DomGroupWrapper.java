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

package org.linguafranca.pwdb.kdbx.dom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.base.AbstractGroup;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.w3c.dom.Element;

import java.util.*;

import static org.linguafranca.pwdb.kdbx.dom.DomHelper.*;


/**
 * Class wraps Groups from a {@link DomSerializableDatabase} as {@link org.linguafranca.pwdb.Group}
 *
 * @author jo
 */
public class DomGroupWrapper extends AbstractGroup<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper> {

    static Map<String, ValueCreator> mandatoryGroupElements = new HashMap<String, ValueCreator>() {{
        put(UUID_ELEMENT_NAME, new UuidValueCreator());
        put(NAME_ELEMENT_NAME, new ConstantValueCreator(""));
        put(NOTES_ELEMENT_NAME, new ConstantValueCreator(""));
        put(ICON_ELEMENT_NAME, new ConstantValueCreator("0"));
        put(TIMES_ELEMENT_NAME, new ConstantValueCreator(""));
        put(LAST_MODIFICATION_TIME_ELEMENT_NAME, new DateValueCreator());
        put(CREATION_TIME_ELEMENT_NAME, new DateValueCreator());
        put(LAST_ACCESS_TIME_ELEMENT_NAME, new DateValueCreator());
        put(EXPIRY_TIME_ELEMENT_NAME, new DateValueCreator());
        put(EXPIRES_ELEMENT_NAME, new ConstantValueCreator("False"));
        put(USAGE_COUNT_ELEMENT_NAME, new ConstantValueCreator("0"));
        put(LOCATION_CHANGED, new DateValueCreator());
    }};


    private final Element element;
    private final DomDatabaseWrapper database;

    public DomGroupWrapper(Element element, DomDatabaseWrapper database, boolean newGroup) {
        this.database = database;
        this.element = element;
        if (newGroup) {
            ensureElements(this.element, mandatoryGroupElements);
        }
    }

    @Override
    public boolean isRootGroup() {
        Element parent = ((Element) element.getParentNode());
        return parent != null && (parent.getTagName().equals("Root"));
    }

    @Override
    public boolean isRecycleBin() {
        String UUIDcontent = getElementContent(RECYCLE_BIN_UUID_ELEMENT_NAME, database.dbMeta);
        if (UUIDcontent != null){
            UUID uuid = Helpers.uuidFromBase64(UUIDcontent);
            return uuid.equals(this.getUuid());
        }
        return false;
    }

    @Override
    public @Nullable DomGroupWrapper getParent() {
        Element parent = ((Element) element.getParentNode());
        if (parent == null) {
            return null;
        }
        // if the element is the root group there is no parent
        if (element == element.getOwnerDocument().getDocumentElement().getElementsByTagName(GROUP_ELEMENT_NAME).item(0)){
            return null;
        }
        return new DomGroupWrapper(parent, database, false);
    }

    @Override
    public void setParent(DomGroupWrapper parent) {
        parent.addGroup(this);
    }

    @Override
    public List<DomGroupWrapper> getGroups() {
        List<Element> elements = getElements(GROUP_ELEMENT_NAME, this.element);
        List<DomGroupWrapper> result = new ArrayList<>(elements.size());
        for (Element e: elements){
            result.add(new DomGroupWrapper(e, database, false));
        }
        return result;
    }

    @Override
    public int getGroupsCount() {
        List<Element> elements = getElements(GROUP_ELEMENT_NAME, this.element);
        return elements.size();
    }

    @Override
    public DomGroupWrapper addGroup(DomGroupWrapper group) {
        if (group.isRootGroup()) {
            throw new IllegalStateException("Cannot set root group as child of another group");
        }
        // skip if this is a new group with no parent
        if (group.getParent() != null) {
            group.getParent().touch();
            group.getParent().removeGroup(group);
        }
        element.appendChild(group.element);
        touchElement("Times/LocationChanged", group.element);
        touch();
        return group;
    }

    @Override
    public DomGroupWrapper removeGroup(DomGroupWrapper g1) {
        element.removeChild(g1.element);
        database.setDirty(true);
        return g1;
    }

    @Override
    public List<DomEntryWrapper> getEntries() {
        List<Element> elements = getElements(ENTRY_ELEMENT_NAME, this.element);
        List<DomEntryWrapper> entries = new ArrayList<>(elements.size());
        for(Element e: elements) {
            entries.add(new DomEntryWrapper(e, database, false));
        }
        return entries;
    }

    @Override
    public int getEntriesCount() {
        return getElementsCount(ENTRY_ELEMENT_NAME, this.element);
    }

    @Override
    public DomEntryWrapper addEntry(DomEntryWrapper entry) {
        if (entry.getParent() != null) {
            entry.element.getParentNode().removeChild(element);
        }
        element.appendChild(entry.element);
        database.setDirty(true);
        return entry;
    }

    @Override
    public DomEntryWrapper removeEntry(DomEntryWrapper e12) {
        element.removeChild(e12.element);
        database.setDirty(true);
        return e12;
    }

    @Override
    public String getName() {
        return getElementContent(NAME_ELEMENT_NAME, element);
    }

    @Override
    public void setName(String name) {
        setElementContent(NAME_ELEMENT_NAME, element, name);
        database.setDirty(true);
    }

    @Override
    public UUID getUuid() {
        String encodedUuid = getElementContent(UUID_ELEMENT_NAME, element);
        return Helpers.uuidFromBase64(encodedUuid);
    }

    @Override
    public DomIconWrapper getIcon() {
        return new DomIconWrapper(getElement(ICON_ELEMENT_NAME, element, false));
    }

    @Override
    public void setIcon(DomIconWrapper icon) {
        setElementContent(ICON_ELEMENT_NAME, element, String.valueOf(icon.getIndex()));
        database.setDirty(true);
    }

    @NotNull
    @Override
    public DomDatabaseWrapper getDatabase() {
        return database;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomGroupWrapper that = (DomGroupWrapper) o;

        return element.equals(that.element) && database.equals(that.database);

    }

    private void touch() {
        touchElement(LAST_MODIFICATION_TIME_ELEMENT_NAME, this.element);
        this.database.setDirty(true);
    }
}
