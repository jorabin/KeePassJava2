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

package org.linguafranca.pwdb.base;

import org.linguafranca.pwdb.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base implementation of Database
 *
 * @author Jo
 */
public abstract class AbstractDatabase<D extends Database<D, G, E, I>, G extends Group<D, G, E, I>, E extends Entry<D,G,E,I>, I extends Icon> implements Database<D, G, E, I> {

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
    public void visit(G group, Visitor visitor) {

        if (visitor.isEntriesFirst()) {
            for (E entry : group.getEntries()) {
                visitor.visit(entry);
            }
        }

        for (G g : group.getGroups()) {
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
    public List<? extends E> findEntries(Entry.Matcher matcher) {
        return getRootGroup().findEntries(matcher, true);
    }

    @Override
    public List<? extends E> findEntries(String find) {
        return getRootGroup().findEntries(find, true);
    }

    @Override
    public G newGroup(String name) {
        G result = newGroup();
        result.setName(name);
        return result;
    }

    @Override
    public G newGroup(Group group) {
        G result = newGroup();
        result.setName(group.getName());
        result.setIcon(this.newIcon(group.getIcon().getIndex()));
        return result;
    }

    @Override
    public E newEntry(String title) {
        E result = newEntry();
        result.setTitle(title);
        return result;
    }

    @Override
    public E newEntry(Entry<?,?,?,?> entry) {
        E result = newEntry();
        for (String propertyName: entry.getPropertyNames()) {
            try {
                // all implementations must support setting of STANDARD_PROPERTY_NAMES
                result.setProperty(propertyName, entry.getProperty(propertyName));
            } catch (UnsupportedOperationException e) {
                // oh well, we tried
            }
        }
        try {
            for (String propertyName: (entry.getBinaryPropertyNames())) {
                try {
                    // all implementations must support setting of STANDARD_PROPERTY_NAMES
                    result.setBinaryProperty(propertyName, entry.getBinaryProperty(propertyName));
                } catch (UnsupportedOperationException e) {
                    // oh well, we tried
                }
            }
        } catch (UnsupportedOperationException e) {
            // never mind
        }
        result.setIcon(this.newIcon(entry.getIcon().getIndex()));
        // everything else should have been copied via properties
        return result;
    }

    @Override
    public E findEntry(final UUID uuid) {
        List<? extends E> entries = findEntries(new Entry.Matcher() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getUuid().equals(uuid);
            }
        });
        if (entries.size() > 1) {
            throw new IllegalStateException("Two entries same UUID");
        }
        if (entries.size() == 0) {
            return null;
        }
        return entries.get(0);
    }

    @Override
    public boolean deleteEntry(final UUID uuid) {
        E e = findEntry(uuid);
        if (e == null) {
            return false;
        }
        e.getParent().removeEntry(e);
        if (isRecycleBinEnabled()) {
            getRecycleBin().addEntry(e);
        }
        return true;
    }

    @Override
    public G findGroup(final UUID uuid){
        final List<G> groups = new ArrayList<>();
        visit(new Visitor.Default() {
            // ignore sub groups of the recycle bin
            boolean recycle;
            @Override
            public void startVisit(Group group) {
                if (!recycle && group.getUuid().equals(uuid)) {
                    groups.add((G) group);
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
        if (groups.size() == 0) {
            return null;
        }
        return groups.get(0);
    }

    @Override
    public boolean deleteGroup(final UUID uuid) {
        G g = findGroup(uuid);
        if (g==null) {
            return false;
        }
        g.getParent().removeGroup(g);
        if (isRecycleBinEnabled()) {
            getRecycleBin().addGroup(g);
        }
        return true;
    }

    @Override
    public void emptyRecycleBin() {
        G recycle = getRecycleBin();
        if (recycle == null) {
            return;
        }
        for (G g: recycle.getGroups()){
            recycle.removeGroup(g);
        }
        for (E e: recycle.getEntries()){
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
        return true;
    }
}
