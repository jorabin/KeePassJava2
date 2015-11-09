package org.linguafranca.db;

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
 * <p>At some point support for binary properties may be added
 * to this interface
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

    String STANDARD_PROPERTY_NAME_USER_NAME = "UserName";
    String STANDARD_PROPERTY_NAME_PASSWORD = "Password";
    String STANDARD_PROPERTY_NAME_URL = "URL";
    String STANDARD_PROPERTY_NAME_TITLE = "Title";
    String STANDARD_PROPERTY_NAME_NOTES = "Notes";

    List<String> STANDARD_PROPERTY_NAMES =  Collections.unmodifiableList(Arrays.asList(
            STANDARD_PROPERTY_NAME_USER_NAME,
            STANDARD_PROPERTY_NAME_PASSWORD,
            STANDARD_PROPERTY_NAME_URL,
            STANDARD_PROPERTY_NAME_TITLE,
            STANDARD_PROPERTY_NAME_NOTES));

    /**
     * Interface to implement when using the {@link #match(Entry.Matcher)}
     * method
     */
    interface Matcher {
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
     * entry's ancestor groups and the tile of this entry.
     */
    String getPath();

    /**
     * Gets the value of a property.
     *
     * <p>All implementations of Entry are required to support reading and writing of
     * {@link #STANDARD_PROPERTY_NAMES}.
     * @param name the name of the property to get
     * @return a value or null if the property is not known, or if setting of arbitrary properties is not supported
     */
    String getProperty(String name);

    /**
     * Sets the value of a property.
     *
     * <p>Other than the {@link #STANDARD_PROPERTY_NAMES} support for this methd is optional.
     *
     * @param name the name of the property to set
     * @param value the value to set it to
     * @throws UnsupportedOperationException if the name is not one of the standard properties and properties
     * are not supported
     */
    void setProperty(String name, String value);

    /**
     * Returns a list of property names known to the entry.
     *
     * <p>All implementations of Entry are required to support reading and writing of
     * {@link #STANDARD_PROPERTY_NAMES}.
     * @return a list that is modifiable by the caller without affecting the Entry.
     */
    List<String> getPropertyNames();

    /**
     * Get the parent of this entry
     * @return a parent
     */
    Group getParent();

    /**
     * Get the UUID of this entry
     *
     * @return a UUID
     */
    UUID getUuid();

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
     */
    String getPassword();

    /**
     * Sets the plaintext password for this Entry.
     *
     * <p>Implementations should Touch LastModifiedTime when this method is called.
     *
     * @param pass a password
     */
    void setPassword(String pass);

    String getUrl();

    void setUrl(String url);

    boolean matchUrl(String url);

    String getTitle();

    void setTitle(String title);

    boolean matchTitle(String text);

    String getNotes();

    void setNotes(String notes);

    boolean matchNotes(String text);

    Icon getIcon();

    void setIcon(Icon icon);

    Date getLastAccessTime();

    Date getCreationTime();

    Date getExpiryTime();

    Date getLastModificationTime();
}
