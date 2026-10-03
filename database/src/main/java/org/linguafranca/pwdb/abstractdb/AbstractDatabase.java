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

package org.linguafranca.pwdb.abstractdb;

import org.linguafranca.pwdb.*;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base implementation of Database
 *
 * @author Jo
 */
public abstract class AbstractDatabase implements Database {

    private boolean isDirty;

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    @Override
    public void visit(Visitor visitor) {
        visitor.startVisit(getRootGroup());
        visit(getRootGroup(), visitor);
        visitor.endVisit(getRootGroup());
    }

    @Override
    public void visit(Group group, Visitor visitor) {

        if (visitor.isEntriesFirst()) {
            for (Entry entry : group.getEntries()) {
                visitor.visit(entry);
            }
        }

        for (Group g : group.getGroups()) {
            visitor.startVisit(g);
            visit(g, visitor);
            visitor.endVisit(g);
        }

        if (!visitor.isEntriesFirst()) {
            for (Entry entry : group.getEntries()) {
                visitor.visit(entry);
            }
        }
    }

    @Override
    public List<Entry> findEntries(Entry.Matcher matcher) {
        return getRootGroup().findEntries(matcher, true);
    }

    @Override
    public List<Entry> findEntries(String find) {
        return getRootGroup().findEntries(find, true);
    }

    @Override
    public Group newGroup(String name) {
        Group result = newGroup();
        result.setName(name);
        return result;
    }

    @Override
    public Group newGroup(Group group) {
        Group result = newGroup();
        result.setName(group.getName());
        result.setIcon(this.newIcon(group.getIcon().getIndex()));
        return result;
    }

    @Override
    public Entry newEntry(String title) {
        Entry result = newEntry();
        result.setTitle(title);
        return result;
    }

    @Override
    public Entry newEntry(Entry entry) {
        Entry result = newEntry();
        for (String propertyName: entry.getPropertyNames()) {
            // all implementations support the standard properties
            if (Entry.STANDARD_PROPERTY_NAMES.contains(propertyName) || supports(Feature.AD_HOC_PROPERTIES)) {
                result.setProperty(propertyName, entry.getProperty(propertyName));
            }
        }
        if (supports(Feature.BINARY_PROPERTIES)) {
            for (String propertyName: entry.getBinaryPropertyNames()) {
                result.setBinaryProperty(propertyName, entry.getBinaryProperty(propertyName));
                if (!supports(Feature.MULTIPLE_BINARY_PROPERTIES)) {
                    break;
                }
            }
        }
        result.setIcon(this.newIcon(entry.getIcon().getIndex()));
        // everything else should have been copied via properties
        return result;
    }

    @Override
    public Entry findEntry(final UUID uuid) {
        List<? extends Entry> entries = findEntries(entry -> entry.getUuid().equals(uuid));
        if (entries.size() > 1) {
            throw new IllegalStateException("Two entries same UUID");
        }
        if (entries.isEmpty()) {
            return null;
        }
        return entries.get(0);
    }

    @Override
    public boolean deleteEntry(final UUID uuid) {
        Entry e = findEntry(uuid);
        if (e == null) {
            return false;
        }

        //noinspection ConstantConditions
        e.getParent().removeEntry(e);
        if (isRecycleBinEnabled()) {
            //noinspection ConstantConditions
            getRecycleBin().addEntry(e);
        }
        return true;
    }

    @Override
    public Group findGroup(final UUID uuid){
        final List<Group> groups = new ArrayList<>();
        visit(new Visitor.Default() {
            // set to true while visiting sub groups of recycle bin
            boolean recycle;
            @Override
            public void startVisit(Group group) {
                if (!recycle && group.getUuid().equals(uuid)) {
                    groups.add(group);
                }
                if (group.isRecycleBin()) {
                    recycle = true;
                }
            }

            @Override
            public void endVisit(Group group) {
                if (group.isRecycleBin()) {
                    recycle = false;
                }
            }
        });
        if (groups.size() > 1) {
            throw new IllegalStateException("Two groups same UUID");
        }
        if (groups.isEmpty()) {
            return null;
        }
        return groups.get(0);
    }

    @Override
    public boolean deleteGroup(final UUID uuid) {
        Group g = findGroup(uuid);
        if (g==null) {
            return false;
        }

        //noinspection ConstantConditions
        g.getParent().removeGroup(g);
        if (isRecycleBinEnabled()) {
            //noinspection ConstantConditions
            getRecycleBin().addGroup(g);
        }
        return true;
    }

    @Override
    public void emptyRecycleBin() {
        Group recycle = getRecycleBin();
        if (recycle == null) {
            return;
        }
        for (Group g: recycle.getGroups()){
            recycle.removeGroup(g);
        }
        for (Entry e: recycle.getEntries()){
            recycle.removeEntry(e);
        }
    }

    @Override
    public boolean supportsNonStandardPropertyNames() {
        return true;
    }

    @Override
    public boolean supportsBinaryProperties() {
        return true;
    }

    @Override
    public boolean supportsRecycleBin() {
        return false;
    }

    /**
     * Without a property value strategy no property is protected
     */
    @Override
    public boolean shouldProtect(String propertyName){
        return false;
    }

    /**
     * Without a property value strategy properties can only be set not to be protected
     * @throws UnsupportedOperationException if protect is true
     */
    @Override
    public void setShouldProtect(String propertyName, boolean protect){
        if (protect) {
            throw new UnsupportedOperationException("Property protection is not supported");
        }
    }

    /**
     * Without a property value strategy no property is protected
     */
    @Override
    public List<String> listShouldProtect(){
        return new ArrayList<>();
    }

    /**
     * There is no strategy to return
     * @throws UnsupportedOperationException always
     */
    @Override
    public PropertyValue.Strategy getPropertyValueStrategy(){
        throw new UnsupportedOperationException("Property value strategy is not supported");
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setPropertyValueStrategy(PropertyValue.Strategy strategy){
        throw new UnsupportedOperationException("Property value strategy is not supported");
    }
    @Override
    public boolean supportsPropertyValueStrategy(){
        return false;
    }

    @Override
    @Deprecated
    public void saveNx(Credentials credentials, OutputStream outputStream) {
        try {
            save(credentials, outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public <C extends StreamConfiguration> void saveNx(StreamFormat<C> streamFormat, Credentials credentials, OutputStream outputStream) {
        try {
            save(streamFormat, credentials, outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
