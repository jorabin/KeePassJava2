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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.abstractdb.AbstractDatabase;
import org.linguafranca.pwdb.io.NonClosingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * The class holds a simple in memory representation of the tree structure of groups and leaf Entry objects.
 *
 * @author jo
 */
public class KdbDatabase extends AbstractDatabase {
    private String description = "";
    private KdbStreamFormat streamFormat = new KdbStreamFormat();
    private final KdbGroup rootGroup;

    // local time, as KDB files are thought to hold local times
    static final DateTimeFormatter isoDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault());

    public KdbDatabase() {
        // KDB files don't have a single root group, this is a synthetic surrogate
        this.rootGroup = new KdbGroup();
        rootGroup.database = this;
        rootGroup.setRoot(true);
        rootGroup.setName("Root");
        rootGroup.setIcon(new KdbIcon(1));
        rootGroup.setUuid(UUID.randomUUID());
    }

    /**
     * Read a KDB file, leaving the stream open
     *
     * @param credentials credentials to use
     * @param inputStream where to read from - the caller closes it
     * @return a new database
     * @since 3.1.0
     */
    public static KdbDatabase read(Credentials credentials, InputStream inputStream) throws IOException {
        // the serializer closes its decryption chain, which closes the stream under it
        KdbHeader kdbHeader = new KdbHeader();
        KdbDatabase database = KdbSerializer.createKdbDatabase(credentials, kdbHeader, new NonClosingInputStream(inputStream));
        database.streamFormat = new KdbStreamFormat(kdbHeader);
        return database;
    }

    /**
     * Load a KDB file and close the stream
     *
     * @deprecated use {@link #read(Credentials, InputStream)} (issue #109)
     */
    @Deprecated
    public static KdbDatabase load(Credentials credentials, InputStream inputStream) throws IOException {
        try (inputStream) {
            return read(credentials, inputStream);
        }
    }

    /**
     * Load a KDB file - avoiding checked exceptions
     *
     * @deprecated use {@link #read(Credentials, InputStream)} (issue #109)
     */
    @Deprecated
    public static KdbDatabase loadNx(Credentials credentials, InputStream inputStream) {
        try {
            return KdbDatabase.load(credentials, inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public KdbGroup getRootGroup() {
        return rootGroup;
    }

    @Override
    public KdbGroup newGroup() {
        KdbGroup group = new KdbGroup();
        group.database = this;
        return group;
    }

    @Override
    public KdbEntry newEntry() {
        KdbEntry entry = new KdbEntry();
        entry.database = this;
        return entry;
    }

    @Override
    public KdbIcon newIcon() {
        return new KdbIcon(0);
    }

    @Override
    public KdbIcon newIcon(Integer i) {
        return new KdbIcon(i);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
    }

    @Override
    public void write(Credentials credentials, OutputStream outputStream) {
        throw new UnsupportedOperationException("Cannot write KDB files in this implementation");
    }

    @Override
    public <C extends StreamConfiguration> void write(StreamFormat<C> streamFormat, Credentials credentials,
                                                      OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void save(Credentials credentials, OutputStream outputStream) {
        throw new UnsupportedOperationException("Cannot save KDB files in this implementation");
    }

    @Override
    @Deprecated
    public <C extends StreamConfiguration> void save(StreamFormat<C> streamFormat, Credentials credentials,
                                                     OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean shouldProtect(String propertyName) {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setName(String s) {
        if (s != null && !s.isEmpty()) {
            throw new UnsupportedOperationException("KDB databases don't have a name");
        }

    }

    private static class GroupFinder extends Visitor.Default {
        Group foundGroup = null;
        UUID uuid;

        GroupFinder(UUID uuid) {
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

    @Override
    public boolean isRecycleBinEnabled() {
        return false;
    }

    @Override
    public void enableRecycleBin(boolean enable) {
        if (enable) {
            throw new UnsupportedOperationException("KDB files don't have a recycle bin");
        }
    }

    @Override
    public KdbGroup getRecycleBin() {
        return null;
    }

    @Override
    public boolean supportsNonStandardPropertyNames() {
        return false;
    }

    @Override
    public boolean supportsBinaryProperties() {
        return true;
    }

    /**
     * KDB databases have no name, and an entry has at most one attachment
     */
    @Override
    public boolean supports(Feature feature) {
        return switch (feature) {
            case DATABASE_NAME, MULTIPLE_BINARY_PROPERTIES -> false;
            default -> super.supports(feature);
        };
    }

    @SuppressWarnings("RedundantMethodOverride")
    @Override
    public boolean supportsRecycleBin() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends StreamConfiguration> StreamFormat<C> getStreamFormat(){
        return (StreamFormat<C>) streamFormat;
    }
}
