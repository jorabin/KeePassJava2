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
import org.linguafranca.pwdb.Icon;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Base implementation of group
 *
 * @author Jo
 */
public abstract class AbstractGroup<D extends Database<D, G, E, I>, G extends org.linguafranca.pwdb.base.AbstractGroup<D, G, E, I>, E extends Entry<D, G, E, I>, I extends Icon> implements Group<D, G, E, I> {

    @Override
    public List<? extends G> findGroups(String group1) {
        ArrayList<G> result = new ArrayList<>();
        for (G g: getGroups()) {
            if (g.getName().equals(group1)) {
                result.add(g);
            }
        }
        return result;
    }

    @Override
    public List<? extends E> findEntries(String find, boolean recursive) {
        List <E> result = new ArrayList<>(getEntries().size());
        if (isRecycleBin()) {
            return result;
        }
        for (E entry: getEntries()){
            if (entry.match(find)){
                result.add(entry);
            }
        }
        if (recursive) {
            for (G group : getGroups()) {
                result.addAll(group.findEntries(find, true));
            }
        }
        return result;
    }

    @Override
    public List<? extends E> findEntries(Entry.Matcher matcher, boolean recursive) {
        List <E> result = new ArrayList<>(getEntries().size());
        if (isRecycleBin()) {
            return result;
        }
        for (E entry: getEntries()){
            if (entry.match(matcher)){
                result.add(entry);
            }
        }
        if (recursive) {
            for (G group : getGroups()) {
                result.addAll(group.findEntries(matcher, true));
            }
        }
        return result;
    }

    @Override
    public void copy(Group<? extends Database, ? extends Group, ? extends Entry, ? extends Icon> parent) {
        for (Group<?,?,?,?> child : parent.getGroups()) {
            G addedGroup = addGroup(this.getDatabase().newGroup(child));
            addedGroup.copy(child);
        }
        for (Entry entry : parent.getEntries()) {
            addEntry(this.getDatabase().newEntry(entry));
        }
    }

    @Override
    public String getPath() {
        Stack<Group> parents = new Stack<>();
        Group parent = this;
        parents.push(this);
        while ((parent=parent.getParent()) != null) {
            parents.push(parent);
        }
        String result = "/";
        while (parents.size() > 0) {
            result = result + parents.pop().getName() + "/";
        }
        return result;
    }

    @Override
    public String toString() {
        return this.getPath();
    }
}
