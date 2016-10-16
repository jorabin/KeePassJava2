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

import java.util.List;

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
}
