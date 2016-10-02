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

package org.linguafranca.pwdb.kdbx.jaxb;

import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.base.AbstractEntry;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.mem.*;


import java.util.*;

/**
 * @author jo
 */
public class JaxbEntryWrapper extends AbstractEntry {


    private final JaxbDatabaseWrapper jaxbDatabaseWrapper;
    private org.linguafranca.pwdb.kdbx.mem.Entry entry;
    private Group parent;
    private HashMap<String, String> strings = new HashMap<>();
    private HashMap<String, byte[]> binaries = new HashMap<>();

    public JaxbEntryWrapper(JaxbDatabaseWrapper jaxbDatabaseWrapper, Group parent, org.linguafranca.pwdb.kdbx.mem.Entry entry) {
        this.entry = entry;
        this.jaxbDatabaseWrapper = jaxbDatabaseWrapper;
        this.parent = parent;
        for (StringField field: entry.getString()) {
            strings.put(field.getKey(), field.getValue().getValue());
        }
        for (BinaryField field: entry.getBinary()) {
            int ref = field.getValue().getRef();
            Binaries bins = jaxbDatabaseWrapper.getKeePassFile().getMeta().getBinaries();
            Binaries.Binary binary = null;
            for (Binaries.Binary item: bins.getBinary()) {
                if (item.getID()==ref) {
                    binary = item;
                    break;
                }
            }
            if (binary == null) {
               binaries.put(field.getKey(), null);
                continue;
            }
            binaries.put(field.getKey(), Helpers.getBinaryContent(binary.getValue(), binary.getCompressed()));
        }
    }

    @Override
    public String getProperty(String name) {
        return strings.get(name);
    }

    @Override
    public void setProperty(String name, String value) {
        strings.put(name, value);
        touch();
    }

    @Override
    public List<String> getPropertyNames() {
        return new ArrayList<>(strings.keySet());
    }

    @Override
    public byte[] getBinaryProperty(String name) {
        return binaries.get(name);
    }

    @Override
    public void setBinaryProperty(String name, byte[] value) {
        binaries.put(name, value);
        touch();
    }

    @Override
    public List<String> getBinaryPropertyNames() {
        return new ArrayList<>(binaries.keySet());
    }

    @Override
    public Group getParent() {
        return parent;
    }

    @Override
    public UUID getUuid() {
        return entry.getUUID();
    }

    @Override
    public String getUsername() {
        return getProperty(STANDARD_PROPERTY_NAME_USER_NAME);
    }

    @Override
    public void setUsername(String username) {
        setProperty(STANDARD_PROPERTY_NAME_USER_NAME, username);
        touch();
    }

    @Override
    public String getPassword() {
        return getProperty(STANDARD_PROPERTY_NAME_PASSWORD);
    }

    @Override
    public void setPassword(String pass) {
        setProperty(STANDARD_PROPERTY_NAME_PASSWORD, pass);
        touch();
    }

    @Override
    public String getUrl() {
        return getProperty(STANDARD_PROPERTY_NAME_URL);
    }

    @Override
    public void setUrl(String url) {
        setProperty(STANDARD_PROPERTY_NAME_URL, url);
        touch();
    }

    @Override
    public String getTitle() {
        return getProperty(STANDARD_PROPERTY_NAME_TITLE);
    }

    @Override
    public void setTitle(String title) {
        setProperty(STANDARD_PROPERTY_NAME_TITLE, title);
        touch();
    }

    @Override
    public String getNotes() {
        return getProperty(STANDARD_PROPERTY_NAME_NOTES);
    }

    @Override
    public void setNotes(String notes) {
        setProperty(STANDARD_PROPERTY_NAME_NOTES, notes);
        touch();
    }

    @Override
    public Icon getIcon() {
        return new JaxbIconWrapper(entry.getIconID());
    }

    @Override
    public void setIcon(Icon icon) {
        entry.setIconID(icon.getIndex());
        touch();
    }

    @Override
    public Date getLastAccessTime() {
        return entry.getTimes().getLastAccessTime();
    }

    @Override
    public Date getCreationTime() {
        return entry.getTimes().getCreationTime();
    }

    @Override
    public boolean getExpires() {
        return entry.getTimes().getExpires();
    }

    @Override
    public Date getExpiryTime() {
        return entry.getTimes().getExpiryTime();
    }

    @Override
    public Date getLastModificationTime() {
        return entry.getTimes().getLastModificationTime();
    }

    private void touch() {
        jaxbDatabaseWrapper.setDirty(true);
        entry.getTimes().setLastModificationTime(new Date());
    }

    public org.linguafranca.pwdb.kdbx.mem.Entry getBackingEntry(){
        return entry;
    }
}
