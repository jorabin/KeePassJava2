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

import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

/**
 * Base implementation of Entry
 *
 * @author Jo
 */
public abstract class AbstractEntry implements Entry {

    @Override
    public  boolean matchTitle(String text){
        return (getTitle().toLowerCase().contains(text.toLowerCase()));
    }

    @Override
    public  boolean matchNotes(String text){
        return (getNotes().toLowerCase().contains(text.toLowerCase()));
    }

    @Override
    public  boolean matchUsername(String text){
        return (getUsername().toLowerCase().contains(text.toLowerCase()));
    }

    @Override
    public  boolean matchUrl(String text){
        return (getUrl().toLowerCase().contains(text.toLowerCase()));
    }

    @Override
    public boolean match(String text) {
        return matchTitle(text) || matchNotes(text) || matchUrl(text) || matchUsername(text);
    }

    @Override
    public boolean match(Entry.Matcher matcher) {
        return matcher.matches(this);
    }

    @Override
    public String getPath() {
        Group parent = this.getParent();
        String result = "";
        if (parent != null) {
            result = parent.getPath();
        }
        return result + getTitle();
    }

    public String toString() {
        return this.getPath();
    }
}
