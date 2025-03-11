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

package org.linguafranca.pwdb.kdbx.jackson;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.StreamConfiguration;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;
import org.linguafranca.pwdb.protect.ProtectedDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.linguafranca.pwdb.kdbx.jackson.KdbxSerializableDatabase.createEmptyDatabase;

public class KdbxDatabase extends ProtectedDatabase {

    KeePassFile keePassFile;
    StreamFormat<?> streamFormat;

    public KdbxDatabase() {
        this(createEmptyDatabase(), null);
    }

    public KdbxDatabase(KeePassFile file, StreamFormat<?> streamFormat) {
        try {
            keePassFile = file;
            keePassFile.root.group.database = this;
            this.streamFormat = streamFormat;
            KdbxSerializableDatabase.fixUp(keePassFile.root.group);
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
    public static KdbxDatabase loadXml(InputStream inputStream) throws Exception {

        KeePassFile keePassFile = new KdbxSerializableDatabase().load(inputStream).keePassFile;
        keePassFile.root.group.uuid = UUID.randomUUID();
        return new KdbxDatabase(keePassFile, null);
    }

    /**
     * Load kdbx file
     *
     * @param credentials credentials to use
     * @param inputStream where to load from
     * @return a new database
     */
    public static KdbxDatabase load(Credentials credentials, InputStream inputStream) throws IOException {
        KdbxSerializableDatabase jsd = new KdbxSerializableDatabase();
        StreamFormat<?> streamFormat = new KdbxStreamFormat();
        streamFormat.load(jsd, credentials, inputStream);
        return new KdbxDatabase(jsd.keePassFile, streamFormat);
    }

    /**
     * Save the database with the same stream format that it was loaded with, or V4
     * default if none
     *
     * @param credentials  credentials to use
     * @param outputStream where to write to - closes stream
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
     * @param outputStream where to write to - call closes output stream
     */
    @Override
    public <C extends StreamConfiguration> void save(StreamFormat<C> streamFormat, Credentials credentials,
                                                     OutputStream outputStream) throws IOException {
        keePassFile.meta.generator = "KeePassJava2-V3-Jackson";
        KdbxSerializableDatabase kdbxSerializableDatabase = new KdbxSerializableDatabase(this.keePassFile);
        kdbxSerializableDatabase.setPropertyValueStrategy(this.getPropertyValueStrategy());
        streamFormat.save(kdbxSerializableDatabase, credentials, outputStream);
        setDirty(false);
    }

    @Override
    public KdbxGroup getRootGroup() {
        return keePassFile.root.group;
    }

    @Override
    public KdbxGroup newGroup() {
        return KdbxGroup.createGroup(this);
    }

    @Override
    public KdbxEntry newEntry() {
        return KdbxEntry.createEntry(this);
    }

    @Override
    public KdbxIcon newIcon() {
        return new KdbxIcon();
    }

    @Override
    public KdbxIcon newIcon(Integer integer) {
        KdbxIcon ic = newIcon();
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
    public KdbxGroup getRecycleBin() {
        UUID recycleBinUuid = this.keePassFile.meta.recycleBinUUID;
        KdbxGroup g = (KdbxGroup) findGroup(recycleBinUuid);
        if (g == null && isRecycleBinEnabled()) {
            g = (KdbxGroup) newGroup("Recycle Bin");
            getRootGroup().addGroup(g);
            this.keePassFile.meta.recycleBinUUID = g.getUuid();
            this.keePassFile.meta.recycleBinChanged = new Date();
        }
        return g;
    }

    @Override
    public boolean supportsRecycleBin() {
        return true;
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
        KdbxSerializableDatabase.addBinary(this.keePassFile, index, bytes);
    }

    public StreamFormat<?> getStreamFormat() {
        return streamFormat;
    }

}
