package org.linguafranca.keepass;

import java.util.*;

/**
 * @author Jo
 */
public interface Entry {

    String STANDARD_PROPERTY_NAME_USER_NAME = "UserName";
    String STANDARD_PROPERTY_NAME_PASSWORD = "Password";
    String STANDARD_PROPERTY_NAME_URL = "URL";
    String STANDARD_PROPERTY_NAME_TITLE = "Title";
    String STANDARD_PROPERTY_NAME_NOTES = "Notes";

    /**
     * Standard properties are attributes of Entries that are accessible either by
     * dedicated methods, such as getPassword, or by {@link #getProperty(String)}
     */
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
        boolean matches (Entry entry);
    }

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
     * Get this entry's UUID
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

    String getPassword();

    void setPassword(String pass);

    String getUrl();

    void setUrl(String url);

    String getTitle();

    void setTitle(String title);

    boolean matchTitle(String text);

    String getNotes();

    void setNotes(String notes);

    boolean matchNote(String text);

    Icon getIcon();

    void setIcon(Icon icon);

    boolean match(String text);

    boolean match(Matcher matcher);

    String getPath();

    Date getLastAccessTime();

    Date getCreationTime();

    Date getExpiryTime();

    Date getLastModificationTime();
}
