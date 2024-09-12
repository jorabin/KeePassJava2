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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Base implementation of group
 *
 * @author Jo
 */
public abstract class AbstractGroup<D extends Database<D>> implements Group<D> {

    @Override
    public List<? extends Group<D>> findGroups(String group) {
        ArrayList<Group<D>> result = new ArrayList<>();
        for (Group<D> g: getGroups()) {
            if (g.getName().equals(group)) {
                result.add(g);
            }
        }
        return result;
    }

    @Override
    public List<? extends Entry<D>> findEntries(String find, boolean recursive) {
        /*
         * Local helper class to avoid violating DRY in {@link AbstractGroup#findEntries(String, boolean)}.
         * Would-be lambda in Java 8.
         */
        class TextMatcher implements Entry.Matcher {

            private final String text;

            private TextMatcher(String text) {
                this.text = text;
            }

            @Override
            public boolean matches(Entry<? extends Database<?>> entry) {
                return entry.match(text);
            }
        }

        return findEntries(new TextMatcher(find), recursive);
    }

    @Override
    public List<? extends Entry<D>> findEntries(Entry.Matcher matcher, boolean recursive) {
        List <Entry<D>> result = new ArrayList<>(getEntries().size());
        for (Entry<D> entry: getEntries()){
            if (entry.match(matcher)){
                result.add(entry);
            }
        }
        if (recursive) {
            for (Group<D> group: getGroups()) {
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
    public void copy(Group<D> parent) {
        for (Group<D> child : parent.getGroups()) {
            Group<D> addedGroup = addGroup(this.getDatabase().newGroup(child));
            addedGroup.copy(child);
        }
        for (Entry<D> entry : parent.getEntries()) {
            addEntry(this.getDatabase().newEntry(entry));
        }
    }

    @Override
    public String getPath() {
        Stack<Group<D>> parents = new Stack<>();
        Group<D> parent = this;
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
