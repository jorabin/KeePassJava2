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

/**
 * Base implementation of Entry
 *
 * @author Jo
 */
public abstract class AbstractEntry implements Entry {

    @Override
    public  boolean matchTitle(String text){
        return (getTitle() != null && getTitle().toLowerCase().contains(text.toLowerCase()));
    }

    @Override
    public  boolean matchNotes(String text){
        return (getNotes()!=null && getNotes().toLowerCase().contains(text.toLowerCase()));
    }

    @Override
    public  boolean matchUsername(String text){
        return (getUsername()!=null && getUsername().toLowerCase().contains(text.toLowerCase()));
    }

    @Override
    public  boolean matchUrl(String text){
        return (getUrl()!=null && getUrl().toLowerCase().contains(text.toLowerCase()));
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

    @Override
    public String getUsername() {
        return getProperty(STANDARD_PROPERTY_NAME_USER_NAME);
    }

    @Override
    public void setUsername(String username) {
        setProperty(STANDARD_PROPERTY_NAME_USER_NAME, username);
        touch();
    }

    /**
     * @deprecated use {@link #getProperty(String)} with {@link #STANDARD_PROPERTY_NAME_PASSWORD}
     */
    @Override
    @Deprecated
    public String getPassword() {
        return getProperty(STANDARD_PROPERTY_NAME_PASSWORD);
    }

    /**
     * @deprecated use {@link #setProperty(String, String)} with {@link #STANDARD_PROPERTY_NAME_PASSWORD}
     */
    @Override
    @Deprecated
    public void setPassword(String pass) {
        setProperty(STANDARD_PROPERTY_NAME_PASSWORD, pass);
        touch();
    }

    @Override
    public String getUrl() {
        return getProperty(STANDARD_PROPERTY_NAME_URL);
    }

    @Override
    public void setUrl(String url) {
        setProperty(STANDARD_PROPERTY_NAME_URL, url);
        touch();
    }

    @Override
    public String getTitle() {
        return getProperty(STANDARD_PROPERTY_NAME_TITLE);
    }

    @Override
    public void setTitle(String title) {
        setProperty(STANDARD_PROPERTY_NAME_TITLE, title);
        touch();
    }

    @Override
    public String getNotes() {
        return getProperty(STANDARD_PROPERTY_NAME_NOTES);
    }

    @Override
    public void setNotes(String notes) {
        setProperty(STANDARD_PROPERTY_NAME_NOTES, notes);
        touch();
    }

    /**
     * For databases without a property value strategy, the property as a string value
     */
    @Override
    public PropertyValue getPropertyValue(String name) {
        String value = getProperty(name);
        return value == null ? null : new PropertyValue.StringStore(value);
    }

    /**
     * For databases without a property value strategy, sets the property from the string value
     */
    @Override
    public Entry setPropertyValue(String name, PropertyValue value) {
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        setProperty(name, value.getValueAsString());
        return this;
    }

    @Override
    public Entry addProperty(String name, byte[] value){
        return setPropertyValue(name, new PropertyValue.StringStore(value));
    }

    @Override
    public Entry addProperty(String name, char[] value){
        return setPropertyValue(name, new PropertyValue.StringStore(value));
    }

    @Override
    public Entry addProperty(String name, CharSequence value){
        return setPropertyValue(name, new PropertyValue.StringStore(value));
    }

    public Entry addEntry(){
        if (this.getParent()!=null) {
            return this.getParent().addEntry();
        }
        throw new IllegalStateException("Entry has no group parent");
    }

    public Entry addEntry(String title){
        if (this.getParent()!=null) {
            return this.getParent().addEntry(title);
        }
        throw new IllegalStateException("Entry has no group parent");
    }


    protected abstract void touch();
}
