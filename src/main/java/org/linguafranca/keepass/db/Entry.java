package org.linguafranca.keepass.db;

import java.util.Date;
import java.util.UUID;

/**
 * @author Jo
 */
public interface Entry {
    /**
     * Interface to implement when using the {@link #match(Entry.Matcher)}
     * method
     */
    interface Matcher {
        boolean matches (Entry entry);
    }

    String getProperty(String name);

    void setProperty(String name, String value);

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

    @Override
    String toString();

}
