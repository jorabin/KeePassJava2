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

package org.linguafranca.pwdb.basic;

import org.jetbrains.annotations.Nullable;
import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.protect.ProtectedDatabase;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Objects;

/**
 * A basic implementation of a Database for experimentation and testing
 */
public class BasicDatabase extends ProtectedDatabase {
    private final BasicGroup root = new BasicGroup(this, "Root");
    private String databaseName = "New Database";
    private String databaseDescription = "Database created on " + Instant.now().toString();
    private StreamFormat<?> streamFormat;

    @Override
    public Group getRootGroup() {
        return root;
    }

    @Override
    public Group newGroup() {
        return new BasicGroup(this);
    }

    @Override
    public Entry newEntry() {
        return new BasicEntry(this);
    }

    @Override
    public Icon newIcon() {
        return new BasicIcon();
    }

    @Override
    public Icon newIcon(Integer i) {
        return new BasicIcon(i);
    }

    @Override
    public boolean isRecycleBinEnabled() {
        return false;
    }

    @Override
    public void enableRecycleBin(boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Group getRecycleBin() {
        return null;
    }

    @Override
    public String getName() {
        return databaseName;
    }

    @Override
    public void setName(String name) {
        this.databaseName = name;
    }

    @Override
    public String getDescription() {
        return databaseDescription;
    }

    @Override
    public void setDescription(String description) {
        this.databaseDescription = description;
    }

    @Override
    public void save(Credentials credentials, OutputStream outputStream) throws IOException {
        if (Objects.isNull(streamFormat)) {
            streamFormat = new KdbxStreamFormat(new KdbxHeader(4));
        }
        save(streamFormat, credentials, outputStream);
    }

    @Override
    public <C extends StreamConfiguration> void save(StreamFormat<C> streamFormat,
                                                     Credentials credentials,
                                                     OutputStream outputStream) throws IOException {
        KdbxStreamFormat kdbxStreamFormat = (KdbxStreamFormat) streamFormat;
        KdbxHeader header = kdbxStreamFormat.getStreamConfiguration();
        OutputStream encryptedOutputStream = KdbxSerializer.createEncryptedOutputStream(credentials, header, outputStream);
        BasicDatabaseSerializer bds = new BasicDatabaseSerializer.Xml(header.getInnerStreamEncryptor());
        bds.save(this, encryptedOutputStream);
    }

    @Override
    public StreamFormat<?> getStreamFormat() {
        return streamFormat;
    }


    /**
     * On load add parents
     *
     * @param parent a parent to recurse
     */
    void fixUp(BasicGroup parent) {
        parent.database = this;
        for (Group group : parent.getGroups()) {
            BasicGroup basicGroup = (BasicGroup) group;
            basicGroup.parent = parent;
            fixUp(basicGroup);
        }

        for (Entry entry : parent.getEntries()) {
            BasicEntry basicEntry = (BasicEntry) entry;
            basicEntry.database = this;
            basicEntry.parent = parent;
        }
    }

}
