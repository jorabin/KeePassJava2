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
import org.linguafranca.pwdb.base.AbstractDatabase;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat.Version;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.pwdb.Credentials;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
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
    private int databaseVersion; // mainly used in DomHelper and Helpers for formatting date Strings according to KDBX version, probably should be removed eventually
    Element dbMeta;

    private DomSerializableDatabase domDatabase = DomSerializableDatabase.createEmptyDatabase();
    
    /**
     * Creates a DomDatabaseWrapper object assuming a database version of 4
     * @throws IOException
     */
    public DomDatabaseWrapper () throws IOException {
    	databaseVersion = 4;
        init();
    }
    
    public DomDatabaseWrapper (StreamFormat streamFormat, Credentials credentials, InputStream inputStream) throws IOException {
    	databaseVersion = streamFormat.getStreamFormatVersion();
        streamFormat.load(domDatabase, credentials, inputStream);
        init();
    }

    //TODO: add support for KDBX4.1
    public static DomDatabaseWrapper load (@NotNull Credentials credentials, @NotNull InputStream inputStream) throws IOException {
    	byte[] all = inputStream.readAllBytes();
    	inputStream = new ByteArrayInputStream(all);

        // hacky way of detecting database version
        // this should really be changed, especially if other database types are to be supported
    	if(all[10] == 4 && all[8] == 0) {
    		return new DomDatabaseWrapper(new KdbxStreamFormat(Version.KDBX4),
                    checkNotNull(credentials, "Credentials must not be null"),
                    checkNotNull(inputStream, "InputStream must not be null"));
    	}
    	else {
    		return new DomDatabaseWrapper(new KdbxStreamFormat(Version.KDBX31),
                    checkNotNull(credentials, "Credentials must not be null"),
                    checkNotNull(inputStream, "InputStream must not be null"));
    	}
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
    	databaseVersion = 4;
    	save(4, credentials, outputStream);
    }
    
    /**
     * Saves the database to a stream assuming a default format for a KDBX database matching databaseVersionFormat
     * @param databaseVersionFormat the version (3, 4) of the KDBX database
     * @param credentials
     * @param outputStream
     * @throws IOException
     */
    public void save(int databaseVersionFormat, Credentials credentials, OutputStream outputStream) throws IOException {
    	setDatabaseVersion(databaseVersionFormat);
    	if(databaseVersion == 3) {
    		new KdbxStreamFormat(Version.KDBX31).save(domDatabase, credentials, outputStream);
            setDirty(false);
    	}
    	else {
    		new KdbxStreamFormat(Version.KDBX4).save(domDatabase, credentials, outputStream);
            setDirty(false);
    	}
    }

    public void save(StreamFormat streamFormat, Credentials credentials, OutputStream outputStream) throws IOException {
    	databaseVersion = streamFormat.getStreamFormatVersion();
        streamFormat.save(domDatabase, credentials, outputStream);
        setDirty(false);
    }

    /**
     * Saves the database to a stream using a user-defined KDBX header for determining version,
     * cipher, KDF, etc.
     * @param customKdbxHeader
     * @param credentials
     * @param outputStream
     * @throws IOException
     */
    public void save(KdbxHeader customKdbxHeader, Credentials credentials, OutputStream outputStream) throws IOException {
    	if(customKdbxHeader.getVersion() == 3) {
    		databaseVersion = 3;
    		new KdbxStreamFormat(Version.KDBX31).save(customKdbxHeader, domDatabase, credentials, outputStream);
            setDirty(false);
    	}
    	else {
    		databaseVersion = 4;
    		new KdbxStreamFormat(Version.KDBX4).save(customKdbxHeader, domDatabase, credentials, outputStream);
            setDirty(false);
    	}	
    }
    
    public boolean shouldProtect(String name) {
        Element protectionElement = DomHelper.getElement("MemoryProtection/Protect" + name, dbMeta, false);
        if (protectionElement == null) {
            return false;
        }
        return Boolean.valueOf(protectionElement.getTextContent());
    }

    
    public int getDatabaseVersion() {
    	return databaseVersion;
    }
    
    public void setDatabaseVersion(int version) {
    	if(version == 4 || version == 3) {
    		databaseVersion = version;
    	}
    	else {
    		throw new IllegalArgumentException("Version must be 3 or 4");
    	}
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
        String UUIDcontent = String.valueOf(getElementContent(RECYCLE_BIN_UUID_ELEMENT_NAME, dbMeta));
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
        touchElement(RECYCLE_BIN_CHANGED_ELEMENT_NAME, dbMeta, databaseVersion);
        return g;
    }

    @Override
    public boolean isRecycleBinEnabled() {
        return Boolean.valueOf(String.valueOf(getElementContent(RECYCLE_BIN_ENABLED_ELEMENT_NAME, dbMeta)));
    }

    @Override
    public void enableRecycleBin(boolean enable) {
        setElementContent(RECYCLE_BIN_ENABLED_ELEMENT_NAME, dbMeta, ((Boolean) enable).toString());
    }

    public String getName() {    	
        return String.valueOf(DomHelper.getElementContent("DatabaseName", dbMeta));
    }

    public void setName(String name) {
        DomHelper.setElementContent("DatabaseName", dbMeta, name);
        DomHelper.touchElement("DatabaseNameChanged", dbMeta, databaseVersion);
        setDirty(true);
    }

    @Override
    public String getDescription() {	
        return String.valueOf(DomHelper.getElementContent("DatabaseDescription", dbMeta));
    }

    @Override
    public void setDescription(String description) {
        DomHelper.setElementContent("DatabaseDescription", dbMeta, description);
        DomHelper.touchElement("DatabaseDescriptionChanged", dbMeta, databaseVersion);
        setDirty(true);
    }
}
