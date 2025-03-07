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

package org.linguafranca.pwdb.base;

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Base implementation of group
 *
 * @author Jo
 */
public abstract class AbstractGroup implements Group {

    public Entry addEntry() {
        return getDatabase().newEntry();
    }

    @Override
    public Entry addEntry(String title) {
        return getDatabase().newEntry(title);
    }

    @Override
    public List<? extends Group> findGroups(String group) {
        ArrayList<Group> result = new ArrayList<>();
        for (Group g: getGroups()) {
            if (g.getName().equals(group)) {
                result.add(g);
            }
        }
        return result;
    }

    @Override
    public List<? extends Entry> findEntries(String find, boolean recursive) {
        return findEntries(entry -> entry.match(find), recursive);
    }

    @Override
    public List<? extends Entry> findEntries(Entry.Matcher matcher, boolean recursive) {
        List <Entry> result = new ArrayList<>(getEntries().size());
        for (Entry entry: getEntries()){
            if (entry.match(matcher)){
                result.add(entry);
            }
        }
        if (recursive) {
            for (Group group: getGroups()) {
                // don't recurse into recycle bin
                if (group.isRecycleBin()) {
                    continue;
                }
                result.addAll(group.findEntries(matcher, true));
            }
        }
        return result;
    }

    @Override
    public void copy(Group parent) {
        for (Group child : parent.getGroups()) {
            Group addedGroup = addGroup(this.getDatabase().newGroup(child));
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
        StringBuilder result = new StringBuilder("/");
        while (!parents.isEmpty()) {
            result.append(parents.pop().getName()).append("/");
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return this.getPath();
    }
}
