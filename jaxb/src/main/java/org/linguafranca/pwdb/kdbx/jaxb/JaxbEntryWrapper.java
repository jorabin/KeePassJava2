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

import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.base.AbstractEntry;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.jaxb.binding.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class JaxbEntryWrapper extends AbstractEntry {


    private final JaxbDatabaseWrapper jaxbDatabaseWrapper;
    private org.linguafranca.pwdb.kdbx.jaxb.binding.Entry wrappedEntry;
    private Group parent;

    public JaxbEntryWrapper(JaxbDatabaseWrapper jaxbDatabaseWrapper, Group parent) {
        this.jaxbDatabaseWrapper = jaxbDatabaseWrapper;
        this.parent = parent;
        this.wrappedEntry = jaxbDatabaseWrapper.getObjectFactory().createEntry();

        for (String s: STANDARD_PROPERTY_NAMES) {
            StringField field = jaxbDatabaseWrapper.getObjectFactory().createStringField();
            field.setKey(s);
            StringField.Value value = jaxbDatabaseWrapper.getObjectFactory().createStringFieldValue();
            value.setValue("");
            field.setValue(value);
            this.wrappedEntry.getString().add(field);
        }

        Times times = jaxbDatabaseWrapper.getObjectFactory().createTimes();
        this.wrappedEntry.setTimes(times);
        Date now = new Date();
        times.setLastModificationTime(now);
        times.setCreationTime(now);
        times.setLastAccessTime(now);
        times.setExpiryTime(now);
        times.setExpires(false);
        times.setUsageCount(0);
        times.setLocationChanged(now);

        wrappedEntry.setUUID(UUID.randomUUID());
    }

    public JaxbEntryWrapper(JaxbDatabaseWrapper jaxbDatabaseWrapper, Group parent, org.linguafranca.pwdb.kdbx.jaxb.binding.Entry wrappedEntry) {
        this.wrappedEntry = wrappedEntry;
        this.jaxbDatabaseWrapper = jaxbDatabaseWrapper;
        this.parent = parent;
    }

    @Override
    public String getProperty(String name) {
        for (StringField field: wrappedEntry.getString()){
            if (field.getKey().equals(name)){
                return field.getValue().getValue();
            }
        }
        return null;
    }

    @Override
    public void setProperty(String name, String value) {
        StringField toRemove = null;
        for (StringField field: wrappedEntry.getString()){
            if (field.getKey().equals(name)) {
                toRemove = field;
                break;
            }
        }
        if (toRemove != null) {
            wrappedEntry.getString().remove(toRemove);
        }

        StringField.Value fieldValue = jaxbDatabaseWrapper.getObjectFactory().createStringFieldValue();
        fieldValue.setValue(value);
        fieldValue.setProtected(false);
        StringField field = jaxbDatabaseWrapper.getObjectFactory().createStringField();
        field.setKey(name);
        field.setValue(fieldValue);
        wrappedEntry.getString().add(field);
        touch();
    }

    @Override
    public List<String> getPropertyNames() {
        List<String> result = new ArrayList<>();
        for (StringField stringField : wrappedEntry.getString()) {
            result.add(stringField.getKey());
        }
        return result;
    }

    @Override
    public byte[] getBinaryProperty(String name) {
        for (BinaryField binaryField : wrappedEntry.getBinary()){
            if (binaryField.getKey().equals(name)){
                Integer ref = binaryField.getValue().getRef();
                for (Binaries.Binary binary: jaxbDatabaseWrapper.getKeePassFile().getMeta().getBinaries().getBinary()){
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
        for (BinaryField binaryField : wrappedEntry.getBinary()) {
            if (binaryField.getKey().equals(name)) {
                toRemove = binaryField;
                break;
            }
        }
        if (toRemove != null) {
            wrappedEntry.getBinary().remove(toRemove);
        }

        // what is the next free index in the binary store?
        Integer max = -1;
        List<Binaries.Binary> binaryList = jaxbDatabaseWrapper.getKeePassFile().getMeta().getBinaries().getBinary();
        for (Binaries.Binary binary: binaryList){
            if (binary.getID() > max) {
                max = binary.getID();
            }
        }
        max++;

        // create a new binary to put in the store
        Binaries.Binary newBin = jaxbDatabaseWrapper.getObjectFactory().createBinariesBinary();
        newBin.setID(max);
        newBin.setValue(Helpers.zipBinaryContent(value));
        newBin.setCompressed(true);
        binaryList.add(newBin);

        // make a reference to it from the entry
        BinaryField binaryField = jaxbDatabaseWrapper.getObjectFactory().createBinaryField();
        binaryField.setKey(name);
        BinaryField.Value fieldValue = jaxbDatabaseWrapper.getObjectFactory().createBinaryFieldValue();
        fieldValue.setRef(max);
        binaryField.setValue(fieldValue);
        wrappedEntry.getBinary().add(binaryField);
        touch();
    }

    @Override
    public List<String> getBinaryPropertyNames() {
        List<String> result = new ArrayList<>();
        for (BinaryField binaryField : wrappedEntry.getBinary()) {
            result.add(binaryField.getKey());
        }
        return result;
    }

    @Override
    public Group getParent() {
        return parent;
    }

    @Override
    public UUID getUuid() {
        return wrappedEntry.getUUID();
    }

    @Override
    public Icon getIcon() {
        return new JaxbIconWrapper(wrappedEntry.getIconID());
    }

    @Override
    public void setIcon(Icon icon) {
        wrappedEntry.setIconID(icon.getIndex());
        touch();
    }

    @Override
    public Date getLastAccessTime() {
        return wrappedEntry.getTimes().getLastAccessTime();
    }

    @Override
    public Date getCreationTime() {
        return wrappedEntry.getTimes().getCreationTime();
    }

    @Override
    public boolean getExpires() {
        return wrappedEntry.getTimes().getExpires();
    }

    @Override
    public Date getExpiryTime() {
        return wrappedEntry.getTimes().getExpiryTime();
    }

    @Override
    public Date getLastModificationTime() {
        return wrappedEntry.getTimes().getLastModificationTime();
    }

    @Override
    protected void touch() {
        jaxbDatabaseWrapper.setDirty(true);
        wrappedEntry.getTimes().setLastModificationTime(new Date());
    }

    public org.linguafranca.pwdb.kdbx.jaxb.binding.Entry getBackingEntry(){
        return wrappedEntry;
    }

    public static JaxbEntryWrapper create(JaxbDatabaseWrapper wrapper, JaxbGroupWrapper parent, Entry entry) {
        if (entry instanceof JaxbEntryWrapper) {
            return (JaxbEntryWrapper) entry;
        }
        JaxbEntryWrapper result = new JaxbEntryWrapper(wrapper, parent);
        org.linguafranca.pwdb.kdbx.jaxb.binding.Entry backingEntry = result.getBackingEntry();
        backingEntry.setUUID(entry.getUuid());
        backingEntry.setIconID(entry.getIcon().getIndex());
        backingEntry.getTimes().setExpires(entry.getExpires());
        backingEntry.getTimes().setExpiryTime(entry.getExpiryTime());
        backingEntry.getTimes().setCreationTime(entry.getCreationTime());
        backingEntry.getTimes().setLastAccessTime(entry.getLastAccessTime());
        backingEntry.getTimes().setLastModificationTime(entry.getLastModificationTime());
        backingEntry.getTimes().setCreationTime(entry.getCreationTime());

        for (String propertyName : entry.getPropertyNames()) {
            result.setProperty(propertyName, entry.getProperty(propertyName));
        }

        for (String propertyName : entry.getBinaryPropertyNames()) {
            result.setBinaryProperty(propertyName, entry.getBinaryProperty(propertyName));
        }
        return result;
    }
}
