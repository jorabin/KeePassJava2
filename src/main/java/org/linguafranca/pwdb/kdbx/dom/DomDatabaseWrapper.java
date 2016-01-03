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

import org.linguafranca.security.Credentials;
import org.linguafranca.pwdb.base.AbstractDatabase;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.linguafranca.pwdb.kdbx.dom.DomHelper.*;
/**
 * The class wraps a {@link DomSerializableDatabase} as a {@link org.linguafranca.pwdb.Database}.
 *
 * @author jo
 */
public class DomDatabaseWrapper extends AbstractDatabase {

    Document document;
    Element dbRootGroup;
    Element dbMeta;

    DomSerializableDatabase domDatabase = DomSerializableDatabase.createEmptyDatabase();


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

    public void save(Credentials credentials, OutputStream outputStream) throws IOException {
        new KdbxStreamFormat().save(domDatabase, credentials, outputStream);
        setDirty(false);
    }

    public void save(StreamFormat streamFormat, Credentials credentials, OutputStream outputStream) throws IOException {
        streamFormat.save(domDatabase, credentials, outputStream);
        setDirty(false);
    }

    public boolean shouldProtect(String name) {
        Element protectionElement = getElement("MemoryProtection/Protect" + name, dbMeta, false);
        if (protectionElement == null) {
            return false;
        }
        return Boolean.valueOf(protectionElement.getTextContent());
    }

    @Override
    public Group getRootGroup() {
        return new DomGroupWrapper(dbRootGroup, this, false);
    }

    @Override
    public Group newGroup() {
        return new DomGroupWrapper(document.createElement(GROUP_ELEMENT_NAME), this, true);
    }

    @Override
    public Entry newEntry() {
        return new DomEntryWrapper(document.createElement(ENTRY_ELEMENT_NAME), this, true);
    }

    @Override
    public Icon newIcon() {
        return new DomIconWrapper(document.createElement(ICON_ELEMENT_NAME));
    }

    @Override
    public Icon newIcon(Integer i) {
        Icon icon =  newIcon();
        icon.setIndex(i);
        return icon;
    }

    public String getName() {
        return getElementContent("DatabaseName", dbMeta);
    }

    public void setName(String name) {
        setElementContent("DatabaseName", dbMeta, name);
        touchElement("DatabaseNameChanged", dbMeta);
        setDirty(true);
    }

    @Override
    public String getDescription() {
        return getElementContent("DatabaseDescription", dbMeta);
    }

    @Override
    public void setDescription(String description) {
        setElementContent("DatabaseDescription", dbMeta, description);
        touchElement("DatabaseDescriptionChanged", dbMeta);
        setDirty(true);
    }
}
