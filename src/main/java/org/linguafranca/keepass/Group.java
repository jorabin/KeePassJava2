package org.linguafranca.keepass;

import java.util.List;
import java.util.UUID;

/**
 * @author Jo
 */
public interface Group {
    boolean isRootGroup();

    String getName();

    void setName(String name);

    Group getParent();

    void setParent(Group parent);

    UUID getUuid();

    Icon getIcon();

    void setIcon(Icon icon);

    /**
     * Returns a list of groups that are the children of this group.
     * <p>The list returned is modifiable by the caller without affecting the status of the database.
     * @return a modifiable list of groups
     */
    List<Group> getGroups();

    Group addGroup(Group group);

    /**
     *
     * @param group
     * @return
     */
    List<Group> findGroups(String group);

    Group findGroup(Group group);

    Group removeGroup(Group group);

    List<Entry> getEntries();

    List<Entry> findEntries(String entry);

    List<Entry> findEntries(Entry.Matcher matcher, boolean recursive);

    Entry addEntry(Entry entry);

    Entry removeEntry(Entry entry);

    String getPath();

    @Override
    String toString();
}
