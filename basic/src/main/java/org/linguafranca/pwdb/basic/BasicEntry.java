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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.abstractdb.AbstractEntry;

import java.time.Instant;
import java.util.*;

public class BasicEntry extends AbstractEntry {
    protected BasicDatabase database;
    protected BasicGroup parent;

    private final UUID uuid;
    private BasicIcon icon;
    private final Map<String, PropertyValue> properties = new HashMap<>();
    private final Map<String, PropertyValue> binaries = new HashMap<>();

    private final Instant creationTime;
    private Instant accessTime;
    private Instant modifiedTime;
    private Instant expiryTime;
    private boolean isExpires;


    public BasicEntry() {
        this.uuid = UUID.randomUUID();
        this.creationTime = Instant.now();
        this.icon = new BasicIcon(1);
        for (String name : Entry.STANDARD_PROPERTY_NAMES) {
            properties.put(name, new PropertyValue.StringStore(""));
        }
    }

    public BasicEntry(BasicDatabase basicDatabase) {
        this();
        this.database = basicDatabase;
    }

    @Override
    protected void touch() {
        database.setDirty(true);
    }

    @Override
    public String getProperty(String name) {
        if (!STANDARD_PROPERTY_NAMES.contains(name) && !database.supportsNonStandardPropertyNames()) {
            throw new IllegalArgumentException("Property " + name + " is not a standard property name");
        }
        if (!properties.containsKey(name)) {
            return null;
        }
        updateAccessTime();
        return properties.get(name).getValueAsString();
    }

    @Override
    public BasicEntry setProperty(String name, String value) {
        updateModifiedTime();
        properties.put(name, PropertyValue.StringStore.getFactory().of(value));
        return this;
    }

    @Override
    public boolean removeProperty(String name) throws IllegalArgumentException, UnsupportedOperationException {
        if (Entry.STANDARD_PROPERTY_NAMES.contains(name)) {
            throw new IllegalArgumentException("Cannot remove standard property");
        }
        if (!database.supportsNonStandardPropertyNames()) {
            throw new UnsupportedOperationException("Database does not support non-standard properties");
        }
        boolean exists = properties.containsKey(name);
        if (exists) {
            updateModifiedTime();
        }
        properties.remove(name);
        return exists;
    }

    @Override
    public List<String> getPropertyNames() {
        return new ArrayList<>(properties.keySet());
    }

    @Override
    public PropertyValue getPropertyValue(String name) {
        return properties.get(name);
    }

    @Override
    public BasicEntry setPropertyValue(String name, PropertyValue value) {
        properties.put(name, value);
        return this;
    }

    public BasicEntry addProperty(String name, byte[] value){
        properties.put(name, database.getPropertyValueStrategy().getFactoryFor(name).of(value));
        return this;
    }
    public BasicEntry addProperty(String name, char[] value) {
        properties.put(name, database.getPropertyValueStrategy().getFactoryFor(name).of(value));
        return this;
    }

    public BasicEntry addProperty(String name, CharSequence value){
        properties.put(name, database.getPropertyValueStrategy().getFactoryFor(name).of(value));
        return this;
    }
    @Override
    public byte[] getBinaryProperty(String name) {
        if (!binaries.containsKey(name)) {
            return null;
        }
        updateAccessTime();
        return binaries.get(name).getValueAsBytes();
    }

    @Override
    public void setBinaryProperty(String name, byte[] value) {
        updateModifiedTime();
        binaries.put(name, PropertyValue.BytesStore.getFactory().of(value));
    }

    @Override
    public boolean removeBinaryProperty(String name) throws UnsupportedOperationException {
        boolean exists = binaries.containsKey(name);
        if (exists) {updateModifiedTime();}
        binaries.remove(name);
        return exists;
    }

    @Override
    public List<String> getBinaryPropertyNames() {
        return List.of(binaries.keySet().toArray(String[]::new));
    }

    @Override
    public BasicDatabase getDatabase() {
        return database;
    }

    @Override
    public @Nullable BasicGroup getParent() {
        return parent;
    }

    @Override
    public @NotNull UUID getUuid() {
        return uuid;
    }

    @Override
    public BasicIcon getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Icon icon) {
        this.icon = (BasicIcon) icon;
    }

    @Override
    public Date getLastAccessTime() {
        if (accessTime == null) {
            return fromInstant(this.creationTime);
        }
        return fromInstant(this.accessTime);
    }

    @Override
    public Date getCreationTime() {
        return fromInstant(creationTime);
    }

    @Override
    public boolean getExpires() {
        return this.isExpires;
    }

    @Override
    public void setExpires(boolean expires) {
        this.isExpires = expires;
    }

    @Override
    public Date getExpiryTime() {
        return fromInstant(this.expiryTime);
    }

    @Override
    public void setExpiryTime(Date expiryTime) throws IllegalArgumentException {
        this.expiryTime = expiryTime.toInstant();
    }

    @Override
    public Date getLastModificationTime() {
        if (modifiedTime == null) {
            return fromInstant(this.creationTime);
        }
        return fromInstant(this.modifiedTime);
    }

    private void updateAccessTime() {
        accessTime = Instant.now();
        touch();
    }

    private void updateModifiedTime() {
        modifiedTime = Instant.now();
        touch();
    }

    private Date fromInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return Date.from(instant);
    }
}
