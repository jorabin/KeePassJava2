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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.pwdb.Visitor;
import org.linguafranca.security.Credentials;
import org.linguafranca.pwdb.base.AbstractDatabase;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;

import java.io.IOException;
import java.io.InputStream;
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

    public static KdbDatabase load(Credentials credentials, InputStream inputStream) throws IOException {
        return KdbSerializer.createKdbDatabase(credentials, new KdbHeader(), inputStream);
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

    static class GroupFinder extends Visitor.Default {
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
