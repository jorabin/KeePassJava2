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

import org.jetbrains.annotations.NotNull;
import org.linguafranca.pwdb.base.AbstractEntry;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.jaxb.binding.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link org.linguafranca.pwdb.Entry} for JAXB.
 *
 * <p>The class wraps an underlying JAXB generated delegate.
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class JaxbEntry extends AbstractEntry<JaxbDatabase, JaxbGroup, JaxbEntry, JaxbIcon> {

    protected JaxbDatabase database;
    protected JaxbEntryBinding delegate;

    public JaxbEntry(JaxbDatabase jaxbDatabase) {
        this.database = jaxbDatabase;
        this.delegate = new JaxbEntryBinding();

        for (String s: STANDARD_PROPERTY_NAMES) {
            StringField field = jaxbDatabase.getObjectFactory().createStringField();
            field.setKey(s);
            StringField.Value value = jaxbDatabase.getObjectFactory().createStringFieldValue();
            value.setValue("");
            field.setValue(value);
            delegate.getString().add(field);
        }

        Date now = new Date(System.currentTimeMillis() / 1000L * 1000L); // to nearest lower second
        Times times = new Times();
        times.setLastModificationTime(now);
        times.setCreationTime(now);
        times.setLastAccessTime(now);
        times.setExpiryTime(now);
        times.setExpires(false);
        times.setUsageCount(0);
        times.setLocationChanged(now);
        this.delegate.setTimes(times);

        delegate.setUUID(UUID.randomUUID());
    }

    public JaxbEntry(JaxbDatabase database, JaxbEntryBinding entry) {
        this.database = database;
        this.delegate = entry;
    }

    @Override
    public String getProperty(String name) {
        for (StringField field: delegate.getString()){
            if (field.getKey().equals(name)){
                return field.getValue().getValue();
            }
        }
        return null;
    }

    @Override
    public void setProperty(String name, String value) {
        StringField toRemove = null;
        for (StringField field: delegate.getString()){
            if (field.getKey().equals(name)) {
                toRemove = field;
                break;
            }
        }
        if (toRemove != null) {
            delegate.getString().remove(toRemove);
        }

        StringField.Value fieldValue = database.getObjectFactory().createStringFieldValue();
        fieldValue.setValue(value);
        fieldValue.setProtected(false);
        StringField field = database.getObjectFactory().createStringField();
        field.setKey(name);
        field.setValue(fieldValue);
        delegate.getString().add(field);
        touch();
    }

    @Override
    public boolean removeProperty(String name) throws IllegalArgumentException {
        if (STANDARD_PROPERTY_NAMES.contains(name)) throw new IllegalArgumentException("may not remove property: " + name);

        StringField toRemove = null;
        for (StringField field: delegate.getString()){
            if (field.getKey().equals(name)) {
                toRemove = field;
                break;
            }
        }
        if (toRemove == null) {
            return false;
        } else {
            delegate.getString().remove(toRemove);
            touch();
            return true;
        }
    }

    @Override
    public List<String> getPropertyNames() {
        List<String> result = new ArrayList<>();
        for (StringField stringField : delegate.getString()) {
            result.add(stringField.getKey());
        }
        return result;
    }

    @Override
    public byte[] getBinaryProperty(String name) {
        for (BinaryField binaryField : delegate.getBinary()){
            if (binaryField.getKey().equals(name)){
                Integer ref = binaryField.getValue().getRef();
                for (Binaries.Binary binary: database.getKeePassFile().getMeta().getBinaries().getBinary()){
                    if (binary.getID().equals(ref)) {
                        if (binary.getCompressed()) {
                            return Helpers.unzipBinaryContent(binary.getValue());
                        }
                        return binary.getValue();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void setBinaryProperty(String name, byte[] value) {
        // remove old binary property with same name
        BinaryField toRemove = null;
        for (BinaryField binaryField : delegate.getBinary()) {
            if (binaryField.getKey().equals(name)) {
                toRemove = binaryField;
                break;
            }
        }
        if (toRemove != null) {
            delegate.getBinary().remove(toRemove);
        }

        // what is the next free index in the binary store?
        Integer max = -1;
        List<Binaries.Binary> binaryList = database.getKeePassFile().getMeta().getBinaries().getBinary();
        for (Binaries.Binary binary: binaryList){
            if (binary.getID() > max) {
                max = binary.getID();
            }
        }
        max++;

        database.createBinary(value, max);

        // make a reference to it from the entry
        BinaryField binaryField = database.getObjectFactory().createBinaryField();
        binaryField.setKey(name);
        BinaryField.Value fieldValue = database.getObjectFactory().createBinaryFieldValue();
        fieldValue.setRef(max);
        binaryField.setValue(fieldValue);
        delegate.getBinary().add(binaryField);
        touch();
    }

    @Override
    public boolean removeBinaryProperty(String name) throws UnsupportedOperationException {
        BinaryField toRemove = null;
        for (BinaryField binaryField : delegate.getBinary()) {
            if (binaryField.getKey().equals(name)) {
                toRemove = binaryField;
                break;
            }
        }

        if (toRemove == null) {
            return false;
        } else {
            delegate.getBinary().remove(toRemove);
            touch();
            return true;
        }
    }

    @Override
    public List<String> getBinaryPropertyNames() {
        List<String> result = new ArrayList<>();
        for (BinaryField binaryField : delegate.getBinary()) {
            result.add(binaryField.getKey());
        }
        return result;
    }

    @Override
    public JaxbGroup getParent() {
        if (delegate.parent == null) {
            return null;
        }
        return new JaxbGroup(database, ((JaxbGroupBinding) delegate.parent));
    }

    @Override
    public @NotNull UUID getUuid() {
        return delegate.getUUID();
    }

    @Override
    public JaxbIcon getIcon() {
        return new JaxbIcon(delegate.getIconID());
    }

    @Override
    public void setIcon(JaxbIcon icon) {
        delegate.setIconID(icon.getIndex());
        touch();
    }

    @Override
    public Date getLastAccessTime() {
        return delegate.getTimes().getLastAccessTime();
    }

    @Override
    public Date getCreationTime() {
        return delegate.getTimes().getCreationTime();
    }

    @Override
    public boolean getExpires() {
        return delegate.getTimes().getExpires();
    }

    @Override
    public void setExpires(boolean expires) {
        delegate.getTimes().setExpires(true);
    }

    @Override
    public Date getExpiryTime() {
        return delegate.getTimes().getExpiryTime();
    }

    @Override
    public void setExpiryTime(Date expiryTime) throws IllegalArgumentException {
        if (expiryTime == null) throw new IllegalArgumentException("expiryTime may not be null");
        delegate.getTimes().setExpiryTime(expiryTime);
    }

    @Override
    public Date getLastModificationTime() {
        return delegate.getTimes().getLastModificationTime();
    }

    @Override
    protected void touch() {
        database.setDirty(true);
        delegate.getTimes().setLastModificationTime(new Date());
    }
}
