/*
 * Copyright 2023 Giuseppe Valente
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

package org.linguafranca.pwdb.kdbx.jackson;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.StreamConfiguration;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;
import org.linguafranca.pwdb.protect.ProtectedDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.linguafranca.pwdb.kdbx.jackson.JacksonSerializableDatabase.createEmptyDatabase;

public class JacksonDatabase extends ProtectedDatabase<JacksonDatabase, JacksonGroup, JacksonEntry, JacksonIcon> {

    KeePassFile keePassFile;
    StreamFormat<?> streamFormat;

    public JacksonDatabase() throws IOException {
        this(createEmptyDatabase(), null);
    }

    public JacksonDatabase(KeePassFile file, StreamFormat<?> streamFormat) {
        try {
            keePassFile = file;
            keePassFile.root.group.database = this;
            this.streamFormat = streamFormat;
            JacksonSerializableDatabase.fixUp(keePassFile.root.group);
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
    public static JacksonDatabase loadXml(InputStream inputStream) throws Exception {

        KeePassFile keePassFile = new JacksonSerializableDatabase().load(inputStream).keePassFile;
        keePassFile.root.group.uuid = UUID.randomUUID();
        return new JacksonDatabase(keePassFile, null);
    }

    /**
     * Load kdbx file
     *
     * @param credentials credentials to use
     * @param inputStream where to load from
     * @return a new database
     */
    public static JacksonDatabase load(Credentials credentials, InputStream inputStream) throws IOException {
        JacksonSerializableDatabase jsd = new JacksonSerializableDatabase();
        StreamFormat<?> streamFormat = new KdbxStreamFormat();
        streamFormat.load(jsd, credentials, inputStream);
        return new JacksonDatabase(jsd.keePassFile, streamFormat);
    }

    /**
     * Save the database with the same stream format that it was loaded with, or V4
     * default if none
     * 
     * @param credentials  credentials to use
     * @param outputStream where to write to
     */
    @Override
    public void save(Credentials credentials, OutputStream outputStream) throws IOException {
        if (Objects.isNull(streamFormat)) {
            streamFormat = new KdbxStreamFormat(new KdbxHeader(4));
        }
        save(streamFormat, credentials, outputStream);
    }

    /**
     * Save the database with a choice of stream format
     * 
     * @param streamFormat the format to use
     * @param credentials  credentials to use
     * @param outputStream where to write to
     */
    @Override
    public <C extends StreamConfiguration> void save(StreamFormat<C> streamFormat, Credentials credentials,
            OutputStream outputStream) throws IOException {
            keePassFile.meta.generator = "KeePassJava2-Jackson";
            JacksonSerializableDatabase jacksonSerializableDatabase = new JacksonSerializableDatabase(this.keePassFile);
            jacksonSerializableDatabase.setPropertyValueStrategy(this.getPropertyValueStrategy());
            streamFormat.save(jacksonSerializableDatabase, credentials, outputStream);
            setDirty(false);
    }

    @Override
    public JacksonGroup getRootGroup() {
        return keePassFile.root.group;
    }

    @Override
    public JacksonGroup newGroup() {
        return JacksonGroup.createGroup(this);
    }

    @Override
    public JacksonEntry newEntry() {
        return JacksonEntry.createEntry(this);
    }

    @Override
    public JacksonIcon newIcon() {
        return new JacksonIcon();
    }

    @Override
    public JacksonIcon newIcon(Integer integer) {
        JacksonIcon ic = newIcon();
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
    public JacksonGroup getRecycleBin() {
        UUID recycleBinUuid = this.keePassFile.meta.recycleBinUUID;
        JacksonGroup g = findGroup(recycleBinUuid);
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
        if (keePassFile.meta.binaries == null) {
            keePassFile.createBinaries();
        }
        return keePassFile.meta.binaries;
    }

    public void addBinary(byte[] bytes, int index) {
        JacksonSerializableDatabase.addBinary(this.keePassFile, index, bytes);
    }

    public StreamFormat<?> getStreamFormat() {
        return streamFormat;
    }

}
