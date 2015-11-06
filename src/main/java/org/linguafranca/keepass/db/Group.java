package org.linguafranca.keepass.db;

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

    List<Group> getGroups();

    Group addGroup(Group group);

    List<Group> findGroups(String group1);

    Group findGroup(Group group);

    Group removeGroup(Group g1);

    List<Entry> getEntries();

    List<Entry> findEntries(String entry1);

    List<Entry> findEntries(Entry.Matcher matcher, boolean recursive);

    Entry addEntry(Entry entry);

    Entry removeEntry(Entry e12);

    String getPath();

    @Override
    String toString();
}
