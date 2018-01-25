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

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.base.AbstractDatabase;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.stream_3_1.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.pwdb.Credentials;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static org.linguafranca.pwdb.kdbx.Helpers.base64FromUuid;
import static org.linguafranca.pwdb.kdbx.dom.DomHelper.*;

/**
 * The class wraps a {@link DomSerializableDatabase} as a {@link org.linguafranca.pwdb.Database}.
 *
 * @author jo
 */
public class DomDatabaseWrapper extends AbstractDatabase<DomDatabaseWrapper, DomGroupWrapper, DomEntryWrapper, DomIconWrapper> {

    private Document document;
    private Element dbRootGroup;
    Element dbMeta;

    private DomSerializableDatabase domDatabase = DomSerializableDatabase.createEmptyDatabase();


    public DomDatabaseWrapper () throws IOException {
        init();
    }

    public DomDatabaseWrapper (StreamFormat streamFormat, Credentials credentials, InputStream inputStream) throws IOException {
        streamFormat.load(domDatabase, credentials, inputStream);
        init();
    }

    public static DomDatabaseWrapper load (Credentials credentials, InputStream inputStream) throws IOException {
        return new DomDatabaseWrapper(new KdbxStreamFormat(), credentials, inputStream);
    }

    private void init() {
        document = domDatabase.getDoc();
        try {
            dbRootGroup = ((Element) DomHelper.xpath.evaluate("/KeePassFile/Root/Group", document, XPathConstants.NODE));
            dbMeta = ((Element) DomHelper.xpath.evaluate("/KeePassFile/Meta", document, XPathConstants.NODE));
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Credentials credentials, OutputStream outputStream) throws IOException {
        new KdbxStreamFormat().save(domDatabase, credentials, outputStream);
        setDirty(false);
    }

    public void save(StreamFormat streamFormat, Credentials credentials, OutputStream outputStream) throws IOException {
        streamFormat.save(domDatabase, credentials, outputStream);
        setDirty(false);
    }

    public boolean shouldProtect(String name) {
        Element protectionElement = DomHelper.getElement("MemoryProtection/Protect" + name, dbMeta, false);
        if (protectionElement == null) {
            return false;
        }
        return Boolean.valueOf(protectionElement.getTextContent());
    }

    @Override
    public DomGroupWrapper getRootGroup() {
        return new DomGroupWrapper(dbRootGroup, this, false);
    }

    @Override
    public DomGroupWrapper newGroup() {
        return new DomGroupWrapper(document.createElement(DomHelper.GROUP_ELEMENT_NAME), this, true);
    }

    @Override
    public DomEntryWrapper newEntry() {
        return new DomEntryWrapper(document.createElement(DomHelper.ENTRY_ELEMENT_NAME), this, true);
    }

    @Override
    public DomIconWrapper newIcon() {
        return new DomIconWrapper(document.createElement(DomHelper.ICON_ELEMENT_NAME));
    }

    @Override
    public DomIconWrapper newIcon(Integer i) {
        DomIconWrapper icon =  newIcon();
        icon.setIndex(i);
        return icon;
    }

    @Override
    public DomGroupWrapper getRecycleBin() {
        String UUIDcontent = getElementContent(RECYCLE_BIN_UUID_ELEMENT_NAME, dbMeta);
        if (UUIDcontent != null){
            final UUID uuid = Helpers.uuidFromBase64(UUIDcontent);
            if (uuid.getLeastSignificantBits() != 0 && uuid.getMostSignificantBits() != 0) {
                for (DomGroupWrapper g: getRootGroup().getGroups()) {
                    if (g.getUuid().equals(uuid)) {
                        return g;
                    }
                }
                // the recycle bin seems to have been lost, better create another one
            }
            // uuid was 0 i.e. there isn't one
        }
        // no recycle bin group set up
        if (!isRecycleBinEnabled()) {
            return null;
        }

        DomGroupWrapper g = newGroup();
        g.setName("Recycle Bin");
        getRootGroup().addGroup(g);
        ensureElementContent(RECYCLE_BIN_UUID_ELEMENT_NAME, dbMeta, base64FromUuid(g.getUuid()));
        touchElement(RECYCLE_BIN_CHANGED_ELEMENT_NAME, dbMeta);
        return g;
    }

    @Override
    public boolean isRecycleBinEnabled() {
        return Boolean.valueOf(getElementContent(RECYCLE_BIN_ENABLED_ELEMENT_NAME, dbMeta));
    }

    @Override
    public void enableRecycleBin(boolean enable) {
        setElementContent(RECYCLE_BIN_ENABLED_ELEMENT_NAME, dbMeta, ((Boolean) enable).toString());
    }

    public String getName() {
        return DomHelper.getElementContent("DatabaseName", dbMeta);
    }

    public void setName(String name) {
        DomHelper.setElementContent("DatabaseName", dbMeta, name);
        DomHelper.touchElement("DatabaseNameChanged", dbMeta);
        setDirty(true);
    }

    @Override
    public String getDescription() {
        return DomHelper.getElementContent("DatabaseDescription", dbMeta);
    }

    @Override
    public void setDescription(String description) {
        DomHelper.setElementContent("DatabaseDescription", dbMeta, description);
        DomHelper.touchElement("DatabaseDescriptionChanged", dbMeta);
        setDirty(true);
    }


}
