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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.pwdb.Entry;

import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.base.AbstractEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * The class holds a KDB Entry
 *
 * @author jo
 */
public class KdbEntry extends AbstractEntry<KdbDatabase, KdbGroup, KdbEntry, KdbIcon> {
    KdbGroup parent;
    private UUID uuid = UUID.randomUUID();
    private String title = "";
    private String url = "";
    private String notes = "";
    private KdbIcon icon = new KdbIcon(0);
    private String username = "";
    private String password = "";
    private Date creationTime = new Date((System.currentTimeMillis()/1000L)*1000L); // to the next lower second
    private Date lastModificationTime = creationTime;
    private Date lastAccessTime = creationTime;
    private boolean expires = false;
    private Date expiryTime = new Date(Long.MAX_VALUE);
    private String binaryDescription = "";
    private byte[] binaryData = new byte[0];

    @Override
    public String getProperty(String name) {
        switch (name) {
            case STANDARD_PROPERTY_NAME_USER_NAME: return getUsername();
            case STANDARD_PROPERTY_NAME_PASSWORD: return getPassword();
            case STANDARD_PROPERTY_NAME_URL: return getUrl();
            case STANDARD_PROPERTY_NAME_TITLE: return getTitle();
            case STANDARD_PROPERTY_NAME_NOTES: return getNotes();
            default: return null;
        }
    }

    @Override
    public void setProperty(String name, String value) {
        switch (name) {
            case STANDARD_PROPERTY_NAME_USER_NAME: setUsername(value); break;
            case STANDARD_PROPERTY_NAME_PASSWORD: setPassword(value); break;
            case STANDARD_PROPERTY_NAME_URL: setUrl(value); break;
            case STANDARD_PROPERTY_NAME_TITLE: setTitle(value); break;
            case STANDARD_PROPERTY_NAME_NOTES: setNotes(value); break;
            default: throw new UnsupportedOperationException("Cannot set non-standard properties in KDB format");
        }
    }

    @Override
    public boolean removeProperty(String name) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot remove non-standard properties in KDB format");
    }

    @Override
    public boolean removeBinaryProperty(String name) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot remove binary properties in KDB format");
    }

    @Override
    public List<String> getPropertyNames() {
        return new ArrayList<>(Entry.STANDARD_PROPERTY_NAMES);
    }

    @Override
    public KdbGroup getParent() {
        return parent;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String pass) {
        this.password = pass;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public KdbIcon getIcon() {
        return icon;
    }

    @Override
    public void setIcon(KdbIcon icon) {
        this.icon = icon;
    }
    
    void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    void setLastModificationTime(Date lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setExpiryTime(Date expiryTime) {
        if (expiryTime == null) throw new IllegalArgumentException("expiryTime may not be null");
        this.expiryTime = expiryTime;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public String getBinaryDescription() {
        return binaryDescription;
    }

    void setBinaryDescription(String binaryDescription) {
        this.binaryDescription = binaryDescription;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    void setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
    }

    public String toString() {
        String time = KdbDatabase.isoDateFormat.format(creationTime);
        return getPath() + String.format(" (%s, %s, %s) %s [%s]", url, username, notes.substring(0,Math.min(notes.length(), 24)), time, binaryDescription);
    }

    @Override
    public boolean match(String text) {
        return super.match(text) || this.getBinaryDescription().toLowerCase().contains(text.toLowerCase());
    }

    @Override
    public byte[] getBinaryProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBinaryProperty(String name, byte[] value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getBinaryPropertyNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExpires(boolean expires) {
        this.expires = expires;
    }

    @Override
    public boolean getExpires() {
        return expires;
    }

    @Override
    protected void touch() {
        lastModificationTime = new Date();
    }
}
