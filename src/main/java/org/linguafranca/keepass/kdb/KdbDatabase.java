package org.linguafranca.keepass.kdb;

import org.linguafranca.keepass.Entry;
import org.linguafranca.keepass.Group;
import org.linguafranca.keepass.Icon;
import org.linguafranca.keepass.AbstractDatabase;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * The class holds a simple in memory representation of the tree structure of groups and leaf Entry objects.
 *
 * @author jo
 */
public class KdbDatabase extends AbstractDatabase {
    private String description;
    private KdbGroup rootGroup;

    static SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public KdbDatabase() {
        // KDB files don't have a single root group, this is a synthetic surrogate
        this.rootGroup = new KdbGroup();
        rootGroup.setRoot(true);
        rootGroup.setName("Root");
        rootGroup.setIcon(new KdbIcon(1));
        rootGroup.setUuid(UUID.randomUUID());
    }

    /**
     * Primarily intended for finding the parent of an Entry, when deserializing KDB data.
     *
     * @param uuid the UUID of the group to find (these are a
     *             simple transposition of the groupId int on deserialization)
     *
     * @return a group or null if none found
     */
    public KdbGroup findGroup(final UUID uuid) {
        GroupFinder groupFinder = new GroupFinder(uuid);
        this.visit(groupFinder);
        return (KdbGroup) groupFinder.foundGroup;
    }

    @Override
    public Group getRootGroup() {
        return rootGroup;
    }

    @Override
    public Group newGroup() {
        return new KdbGroup();
    }

    @Override
    public Entry newEntry() {
        return new KdbEntry();
    }

    @Override
    public Icon newIcon() {
        return new KdbIcon(0);
    }

    @Override
    public Icon newIcon(Integer i) {
        return new KdbIcon(i);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    static class GroupFinder extends DefaultVisitor {
        Group foundGroup = null;
        UUID uuid;

        public GroupFinder(UUID uuid) {
            if (uuid==null) {
                throw new IllegalArgumentException("UUID cannot be null");
            }
            this.uuid = uuid;
        }

        @Override
        public void startVisit(Group group) {
            if (group != null && uuid.equals(group.getUuid())) {
                foundGroup = group;
            }
        }
    }
}
