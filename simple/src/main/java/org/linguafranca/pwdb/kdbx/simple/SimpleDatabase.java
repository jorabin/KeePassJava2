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

package org.linguafranca.pwdb.kdbx.simple;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.StreamConfiguration;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.base.AbstractDatabase;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.simple.model.KeePassFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.linguafranca.pwdb.kdbx.simple.SimpleSerializableDatabase.createEmptyDatabase;
import static org.linguafranca.pwdb.kdbx.simple.SimpleSerializableDatabase.getSerializer;

/**
 * Implementation of {@link org.linguafranca.pwdb.Database} using the Simple XML framework.
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class SimpleDatabase extends AbstractDatabase<SimpleDatabase, SimpleGroup, SimpleEntry, SimpleIcon>{

    KeePassFile keePassFile;
    StreamFormat<?> streamFormat;

    /**
     * Create a new empty database
     */
    public SimpleDatabase() {
        this(createEmptyDatabase(), null);
    }

    public SimpleDatabase(KeePassFile file, StreamFormat<?> streamFormat) {
        try {
            keePassFile = file;
            keePassFile.root.group.database = this;
            this.streamFormat = streamFormat;
            SimpleSerializableDatabase.fixUp(keePassFile.root.group);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    /**
     * Load plaintext XML
     *
     * @param inputStream contains the XML
     * @return a new Database
     * @throws Exception on load failure
     */
    public static SimpleDatabase loadXml(InputStream inputStream) throws Exception {
        KeePassFile result =  getSerializer().read(KeePassFile.class, inputStream);
        result.root.group.uuid = UUID.randomUUID();
        return new SimpleDatabase(result, null);
    }

    /**
     * Load kdbx file
     *
     * @param credentials credentials to use
     * @param inputStream where to load from
     * @return a new database
     */
    public static SimpleDatabase load(Credentials credentials, InputStream inputStream) throws IOException {
        SimpleSerializableDatabase simpleSerializableDatabase = new SimpleSerializableDatabase();
        StreamFormat<?> streamFormat = new KdbxStreamFormat();
        streamFormat.load(simpleSerializableDatabase, credentials, inputStream);
        return new SimpleDatabase(simpleSerializableDatabase.getKeePassFile(), streamFormat);
    }

    /**
     * Save the database with the same stream format that it was loaded with, or V4 default if none
     * @param credentials credentials to use
     * @param outputStream where to write to
     */
    @Override
    public void save(Credentials credentials, OutputStream outputStream) throws IOException {
        // save with the stream format used to load if it exists, otherwise save V4
        if (Objects.isNull(streamFormat)) {
            streamFormat = new KdbxStreamFormat(new KdbxHeader(4));
        }
        save(streamFormat, credentials, outputStream);
    }

    /**
     * Save the database with a choice of stream format
     * @param streamFormat the format to use
     * @param credentials credentials to use
     * @param outputStream where to write to
     */
    @Override
    public <C extends StreamConfiguration> void save(StreamFormat<C> streamFormat, Credentials credentials,
                                                     OutputStream outputStream) throws IOException{
        SimpleSerializableDatabase simpleSerializableDatabase = new SimpleSerializableDatabase(this.keePassFile);
        streamFormat.save(simpleSerializableDatabase, credentials, outputStream);
        setDirty(false);
    }


    @Override
    public SimpleGroup getRootGroup() {
        return keePassFile.root.getGroup();
    }

    @Override
    public SimpleGroup newGroup() {
        return SimpleGroup.createGroup(this);
    }

    @Override
    public SimpleEntry newEntry() {
        return SimpleEntry.createEntry(this);
    }

    @Override
    public SimpleIcon newIcon() {
        return new SimpleIcon();
    }

    @Override
    public SimpleIcon newIcon(Integer integer) {
        SimpleIcon ic = newIcon();
        ic.setIndex(integer);
        return ic;
    }

    @Override
    public boolean isRecycleBinEnabled() {
        return this.keePassFile.meta.recycleBinEnabled;
    }

    @Override
    public void enableRecycleBin(boolean enable) {
        this.keePassFile.meta.recycleBinEnabled = enable;
    }

    @Override
    public SimpleGroup getRecycleBin() {
        UUID recycleBinUuid = this.keePassFile.meta.recycleBinUUID;
        SimpleGroup g = findGroup(recycleBinUuid);
        if (g == null && isRecycleBinEnabled()) {
            g = newGroup("Recycle Bin");
            getRootGroup().addGroup(g);
            this.keePassFile.meta.recycleBinUUID = g.getUuid();
            this.keePassFile.meta.recycleBinChanged = new Date();
        }
        return g;
    }

    @Override
    public String getName() {
        return keePassFile.meta.databaseName;
    }

    @Override
    public void setName(String s) {
        keePassFile.meta.databaseName = s;
        keePassFile.meta.databaseNameChanged = new Date();
        setDirty(true);
    }

    @Override
    public String getDescription() {
        return keePassFile.meta.databaseDescription;
    }

    @Override
    public void setDescription(String s) {
        keePassFile.meta.databaseDescription = s;
        keePassFile.meta.databaseDescriptionChanged = new Date();
        setDirty(true);
    }

    @Override
    public boolean shouldProtect(String s) {
        return keePassFile.meta.memoryProtection.shouldProtect(s);
    }


    public List<KeePassFile.Binary> getBinaries() {
        return keePassFile.getBinaries();
    }

    public void addBinary(byte [] bytes, int index) {
        SimpleSerializableDatabase.addBinary(this.keePassFile, index, bytes);
    }

    public StreamFormat<?> getStreamFormat() {
        return streamFormat;
    }
}