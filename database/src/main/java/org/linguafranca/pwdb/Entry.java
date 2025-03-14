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

package org.linguafranca.pwdb;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Interface for a Database Entry. Database entries provide support
 * at least for the Standard Properties of
 * <ul>
 *     <li>Title
 *     <li>Username
 *     <li>Password
 *     <li>URL
 *     <li>Notes
 * </ul>
 *
 * <p>Entries have a UUID and have an Icon, which are not properties.
 *
 * <p>Support for additional textual properties may be provided
 * by a database implementation.
 *
 * <p>Entries provide support for tracking when they are used.
 * At some point support for accessing a history of modifications
 * may be added to this interface
 *
 * @author Jo
 */
public interface Entry {
    /**
     * Standard properties are attributes of Entries that are accessible either by
     * dedicated methods, such as getPassword, or by {@link #getProperty(String)}
     */

    String STANDARD_PROPERTY_NAME_TITLE = "Title";
    String STANDARD_PROPERTY_NAME_USER_NAME = "UserName";
    String STANDARD_PROPERTY_NAME_PASSWORD = "Password";
    String STANDARD_PROPERTY_NAME_URL = "URL";
    String STANDARD_PROPERTY_NAME_NOTES = "Notes";

    /** allows for static import of name */
    interface STANDARD_PROPERTY_NAME {
        String TITLE = STANDARD_PROPERTY_NAME_TITLE;
        String USER_NAME = STANDARD_PROPERTY_NAME_USER_NAME;
        String PASSWORD = STANDARD_PROPERTY_NAME_PASSWORD;
        String URL = STANDARD_PROPERTY_NAME_URL;
        String NOTES = STANDARD_PROPERTY_NAME_NOTES;
    }

    List<String> STANDARD_PROPERTY_NAMES = List.of(
            STANDARD_PROPERTY_NAME_TITLE,
            STANDARD_PROPERTY_NAME_USER_NAME,
            STANDARD_PROPERTY_NAME_PASSWORD,
            STANDARD_PROPERTY_NAME_URL,
            STANDARD_PROPERTY_NAME_NOTES);

    /**
     * Interface to implement when using the {@link #match(Entry.Matcher)}
     * method
     */
    interface Matcher {
        // needs to be raw type so that we can use lambdas
        boolean matches(Entry entry);
    }

    /**
     * Determines if an entry matches the text supplied. A match is recorded if
     * the text matches the Url, Title, Username or Notes. Match means that any
     * portion of the field to lower case contains the field supplied, to lowercase.
     *
     * @param text the text to find
     * @return true if this entry matches the text
     */
    boolean match(String text);

    /**
     * Returns the result of applying the matcher to this Entry
     *
     * @param matcher the matcher to use
     * @return the result of applying the matcher
     */
    boolean match(Matcher matcher);

    /**
     * Returns an XPath-like representation of this
     * entry's ancestor groups and the title of this entry.
     */
    String getPath();

    /**
     * Gets the value of a property as a String. Use of this method is not recommended for fields with protected values.
     * Use {@link #getPropertyValue(String)}
     *
     * <p>All implementations of Entry are required to support reading and writing of
     * {@link #STANDARD_PROPERTY_NAMES}.
     * @param name the name of the property to get
     * @return a value or null if the property is not known, or if setting of arbitrary properties is not supported
     * @see Database#supportsNonStandardPropertyNames()
     */
    String getProperty(String name);

    /**
     * Gets the value of a property as a PropertyValue.
     *
     * <p>All implementations of Entry are required to support reading and writing of
     * {@link #STANDARD_PROPERTY_NAMES}.
     * @param name the name of the property to get
     * @return a value or null if the property is not known, or if setting of arbitrary properties is not supported
     * @see Database#supportsNonStandardPropertyNames()
     */
    PropertyValue getPropertyValue(String name);

    /**
     * Sets the value of a property as a String in memory.
     * Use of this method is not recommended for fields with protected values.
     * Use {@link #setPropertyValue(String, PropertyValue)}
     *
     * <p>Other than the {@link #STANDARD_PROPERTY_NAMES} support for this method is optional.
     *
     * @param name the name of the property to set
     * @param value the value to set it to
     * @throws UnsupportedOperationException if the name is not one of the standard properties and
     * non-standard properties are not supported
     * @see Database#supportsNonStandardPropertyNames()
     */
    Entry setProperty(String name, String value);

    /**
     * Sets the value of a property as a property value in memory. The method does not check whether the
     * passed PropertyValue is protected relative to {@link Database#shouldProtect(String)}
     *
     * <p>Other than the {@link #STANDARD_PROPERTY_NAMES} support for this method is optional.
     *
     * @param name the name of the property to set
     * @param value the value to set it to
     * @throws UnsupportedOperationException if the name is not one of the standard properties and
     * non-standard properties are not supported
     * @see Database#supportsNonStandardPropertyNames()
     */
    Entry setPropertyValue(String name, PropertyValue value);

    /**
     * Adds (or modifies) the value of a property as a property value in memory. The method uses the
     * PropertyValue.Strategy to determine how to determine the storage format in memory.
     *
     * <p>Other than the {@link #STANDARD_PROPERTY_NAMES} support for this method is optional.
     *
     * @param name the name of the property to set
     * @param value the value to set it to
     * @throws UnsupportedOperationException if the name is not one of the standard properties and
     * non-standard properties are not supported
     * @see Database#supportsNonStandardPropertyNames()
     */
    Entry addProperty(String name, byte[] value);

    /**
     * Adds (or modifies) the value of a property as a property value in memory. The method uses the
     * PropertyValue.Strategy to determine how to determine the storage format in memory.
     *
     * <p>Other than the {@link #STANDARD_PROPERTY_NAMES} support for this method is optional.
     *
     * @param name the name of the property to set
     * @param value the value to set it to
     * @throws UnsupportedOperationException if the name is not one of the standard properties and
     * non-standard properties are not supported
     * @see Database#supportsNonStandardPropertyNames()
     */
    Entry addProperty(String name, char[] value);

    /**
     * Adds (or modifies) the value of a property as a property value in memory. The method uses the
     * PropertyValue.Strategy to determine how to determine the storage format in memory.
     *
     * <p>Other than the {@link #STANDARD_PROPERTY_NAMES} support for this method is optional.
     *
     * @param name the name of the property to set
     * @param value the value to set it to
     * @throws UnsupportedOperationException if the name is not one of the standard properties and
     * non-standard properties are not supported
     * @see Database#supportsNonStandardPropertyNames()
     */
    Entry addProperty(String name, CharSequence value);

    /**
     * Adds a newly created entry to the Group parent of the current Entry, if there is one. Intended primary for
     * fluid building of entries within a group.
     *
     * @throws IllegalStateException if there is no parent
     * @param name the title of the entry
     * @return the newly created entry
     */
    Entry addEntry(String name);

    /**
     * Adds a newly created entry to the Group parent of the current Entry, if there is one. Intended primary for
     * fluid building of entries within a group.
     *
     * @throws IllegalStateException if there is no parent
     * @return the newly created entry
     */
    Entry addEntry();

    /**
     * Removes this property, if it exists and if it is a non-standard property
     *
     * @return true if the property exists and was removed, false otherwise
     * @param name the value of the property to remove
     * @throws UnsupportedOperationException if non-standard properties are not supported
     * @throws IllegalArgumentException if <i>name</i> is a standard property
     * @see Database#supportsNonStandardPropertyNames()
     */
    boolean removeProperty(String name) throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Returns a list of property names known to the entry.
     *
     * <p>All implementations of Entry are required to support reading and writing of
     * {@link #STANDARD_PROPERTY_NAMES}.
     * @return a list that is modifiable by the caller without affecting the Entry.
     */
    List<String> getPropertyNames();

    /**
     * Gets the value of a binary property.
     *
     * <p>Support for this method is optional.
     *
     * @param name the name of the property to get
     * @return a value or null if the property is not known, or if setting of arbitrary properties is not supported
     * @see Database#supportsBinaryProperties
     */
    byte[] getBinaryProperty(String name);

    /**
     * Sets the value of a binary property.
     *
     * <p>Support for this method is optional.
     *
     * @param name the name of the property to set
     * @param value the value to set it to
     * @throws UnsupportedOperationException if binary properties are not supported
     * @see Database#supportsBinaryProperties()
     */
    void setBinaryProperty(String name, byte[] value);

    /**
     * Removes this binary property, if it exists.
     *
     * @return true if the property was removed, false otherwise
     * @param name the value of the property to remove
     * @throws UnsupportedOperationException if binary properties are not supported
     * @see Database#supportsBinaryProperties()
     */
    boolean removeBinaryProperty(String name) throws UnsupportedOperationException;

    /**
     * Returns a list of binary property names known to the entry.
     *
     * <p>All implementations of Entry are required to support reading and writing of
     * {@link #STANDARD_PROPERTY_NAMES}.
     * @return a list that is modifiable by the caller without affecting the Entry.
     * @throws UnsupportedOperationException if binary properties are not supported
     * @see Database#supportsBinaryProperties()
     */
    List<String> getBinaryPropertyNames();

    /**
     * Get the database this entry belongs to
     * @return a database
     */
    Database getDatabase();

    /**
     * Get the parent of this entry
     * @return a parent or null if this entry currently does not have a parent
     */
    @Nullable Group getParent();

    /**
     * Get the UUID of this entry. Databases (like KDB) that do not natively support
     * UUIDs must provide a surrogate here.
     *
     * @return a UUID
     */
    @NotNull UUID getUuid();

    /**
     * Get the username field of this entry
     * @return a username
     */
    String getUsername();

    /**
     * set the username
     * @param username username to set
     */
    void setUsername(String username);

    /**
     * Returns true if this username to lowercase contains the passed username to lowercase.
     * @param username a username to match
     * @return true if matches
     */
    boolean matchUsername(String username);

    /**
     * Gets the (unencrypted) password field for this entry.
     *
     * <p>Implementations should Touch LastAccessedTime when this method is called.
     *
     * @return a password
     * @deprecated use {@link #getPropertyValue(String)} with {@link #STANDARD_PROPERTY_NAME_PASSWORD}
     */
    @Deprecated
    String getPassword();

    /**
     * Sets the plaintext password for this Entry.
     *
     * <p>Implementations should Touch LastModifiedTime when this method is called.
     *
     * @param pass a password
     * @deprecated use {@link #setPropertyValue(String, PropertyValue)} with {@link #STANDARD_PROPERTY_NAME_PASSWORD}
     */
    @Deprecated
    void setPassword(String pass);

    /**
     * Gets the URL for this entry.
     *
     * <p>Implementations should Touch LastAccessedTime when this method is called.
     *
     * @return a string representation of a URL
     */
    String getUrl();

    /**
     * Sets the url for this Entry.
     *
     * <p>Implementations should Touch LastModifiedTime when this method is called.
     *
     * @param url the value to set
     */
    void setUrl(String url);

    /**
     * Returns true if this url to lowercase contains the passed url to lowercase.
     * @param url a string to match
     * @return true if matches
     */
    boolean matchUrl(String url);

    /**
     * Gets the title of this entry.
     *
     * <p>Implementations should Touch LastAccessedTime when this method is called.
     *
     * @return a title
     */
    String getTitle();

    /**
     * Sets the title for this Entry.
     *
     * <p>Implementations should Touch LastModifiedTime when this method is called.
     *
     * @param title the value to set
     */
    void setTitle(String title);

    /**
     * Returns true if this title to lowercase contains the passed title to lowercase.
     * @param text a string to match
     * @return true if matches
     */
    boolean matchTitle(String text);

    /**
     * Gets the notes field for this entry.
     *
     * <p>Implementations should Touch LastAccessedTime when this method is called.
     *
     * @return the notes field
     */
    String getNotes();

    /**
     * Sets the notes for this Entry.
     *
     * <p>Implementations should Touch LastModifiedTime when this method is called.
     *
     * @param notes the value to set
     */
    void setNotes(String notes);

    /**
     * Returns true if the notes to lowercase contains the passed string to lowercase.
     * @param text a string to match
     * @return true if matches
     */
    boolean matchNotes(String text);

    /**
     * Returns the {@link Icon} associated with this entry.
     * @return an Icon
     */
    Icon getIcon();

    /**
     * Sets the {@link Icon} associated with this entry.
     * @param icon an Icon
     */
    void setIcon(Icon icon);

    /**
     * Returns the date at which any value was retrieved from this entry.
     * <p>
     * Implementations SHOULD set this to the creation date or earlier if the entry has never been used.
     */
    Date getLastAccessTime();

    /**
     * Returns the date at which this entry was created
     */
    Date getCreationTime();

    /**
     * Returns true if this entry is to be considered as expired at some point
     */
    boolean getExpires();

    /**
     * Set true for the date returned by {@link #getExpiryTime()} to be considered an expiry time
     * @see #setExpiryTime(Date)
     */
    void setExpires(boolean expires);

    /**
     * Returns a date at which the entry should be considered to have expired, if {@link #getExpires()} is true -
     * otherwise returns an arbitrary date.
     */
    Date getExpiryTime();

    /**
     * Sets the expiry date of this element.
     * @throws IllegalArgumentException if expiryTime is null.
     * @see  org.linguafranca.pwdb.Entry#setExpires(boolean)
     */
    void setExpiryTime(Date expiryTime) throws IllegalArgumentException;

    /**
     * Returns the date that the entry was last modified
     * <p>
     * Implementations SHOULD set this to the creation date or earlier if the entry has never been used.
     */
    Date getLastModificationTime();
}
