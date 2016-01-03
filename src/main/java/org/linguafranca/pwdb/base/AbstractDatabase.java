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

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Visitor;

import java.util.List;

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
            try {
                // all implementations must support setting of STANDARD_PROPERTY_NAMES
                result.setProperty(propertyName, entry.getProperty(propertyName));
            } catch (UnsupportedOperationException e) {
                // oh well, we tried
            }
        }
        result.setIcon(this.newIcon(entry.getIcon().getIndex()));
        // everything else should have been copied via properties
        return result;
    }
}
