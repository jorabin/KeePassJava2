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
import org.linguafranca.pwdb.security.StreamEncryptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
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
            // a new database is written as KDBX 4.1 unless told otherwise
            this.streamFormat = Objects.requireNonNullElseGet(streamFormat, () -> new KdbxStreamFormat(new KdbxHeader(4)));
            fixUp(keePassFile.root.group);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Read plaintext XML, leaving the stream open
     *
     * @param inputStream contains the XML - the caller closes it
     * @return a new Database
     * @throws IOException on read failure
     * @since 3.1.0
     */
    public static KdbxDatabase readXml(InputStream inputStream) throws IOException {
        KdbxSerializableDatabase jsd = new KdbxSerializableDatabase();
        jsd.setEncryption(new StreamEncryptor.None());
        KeePassFile keePassFile = jsd.read(inputStream).keePassFile;
        keePassFile.root.group.uuid = UUID.randomUUID();
        return new KdbxDatabase(keePassFile, null);
    }

    /**
     * Read a kdbx file, leaving the stream open
     *
     * @param credentials credentials to use
     * @param inputStream where to read from - the caller closes it
     * @return a new database
     * @since 3.1.0
     */
    public static KdbxDatabase read(Credentials credentials, InputStream inputStream) throws IOException {
        return read(new KdbxStreamFormat(), credentials, inputStream);
    }

    /**
     * Read a database using the stream format supplied, leaving the stream open,
     * e.g. {@link StreamFormat.None} to read XML written by
     * {@link #write(StreamFormat, Credentials, OutputStream)} with that format
     *
     * @param streamFormat the format of the input
     * @param credentials credentials to use
     * @param inputStream where to read from - the caller closes it
     * @return a new database
     * @since 3.1.0
     */
    public static KdbxDatabase read(StreamFormat<?> streamFormat, Credentials credentials, InputStream inputStream) throws IOException {
        KdbxSerializableDatabase jsd = new KdbxSerializableDatabase();
        streamFormat.read(jsd, credentials, inputStream);
        return new KdbxDatabase(jsd.keePassFile, streamFormat);
    }

    /**
     * Load plaintext XML and close the stream
     *
     * @param inputStream contains the XML
     * @return a new Database
     * @throws Exception on load failure
     * @deprecated use {@link #readXml(InputStream)} (issue #109)
     */
    @Deprecated
    public static KdbxDatabase loadXml(InputStream inputStream) throws Exception {
        try (inputStream) {
            return readXml(inputStream);
        }
    }

    /**
     * Load kdbx file and close the stream
     *
     * @param credentials credentials to use
     * @param inputStream where to load from
     * @return a new database
     * @deprecated use {@link #read(Credentials, InputStream)} (issue #109)
     */
    @Deprecated
    public static KdbxDatabase load(Credentials credentials, InputStream inputStream) throws IOException {
        return load(new KdbxStreamFormat(), credentials, inputStream);
    }

    /**
     * Load a database using the stream format supplied and close the stream
     *
     * @param streamFormat the format of the input
     * @param credentials credentials to use
     * @param inputStream where to load from
     * @return a new database
     * @deprecated use {@link #read(StreamFormat, Credentials, InputStream)} (issue #109)
     */
    @Deprecated
    public static KdbxDatabase load(StreamFormat<?> streamFormat, Credentials credentials, InputStream inputStream) throws IOException {
        try (inputStream) {
            return read(streamFormat, credentials, inputStream);
        }
    }

    /**
     * Load kdbx file - avoiding checked exceptions
     *
     * @deprecated use {@link #read(Credentials, InputStream)} (issue #109)
     */
    @Deprecated
    public static KdbxDatabase loadNx(Credentials credentials, InputStream inputStream) {
        try {
            return load(credentials, inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the database with its stream format ({@link #getStreamFormat()}): the format it was read with,
     * or KDBX 4.1 for a new database, leaving the stream open
     *
     * @param credentials  credentials to use
     * @param outputStream where to write to - the caller closes it
     * @since 3.1.0
     */
    @Override
    public void write(Credentials credentials, OutputStream outputStream) throws IOException {
        write(streamFormat, credentials, outputStream);
    }

    /**
     * Write the database with a choice of stream format, leaving the stream open
     *
     * @param streamFormat the format to use
     * @param credentials  credentials to use
     * @param outputStream where to write to - the caller closes it
     * @since 3.1.0
     */
    @Override
    public <C extends StreamConfiguration> void write(StreamFormat<C> streamFormat, Credentials credentials,
                                                      OutputStream outputStream) throws IOException {
        keePassFile.meta.generator = "KeePassJava2-V3-Jackson";
        KdbxSerializableDatabase kdbxSerializableDatabase = new KdbxSerializableDatabase(this.keePassFile);
        kdbxSerializableDatabase.setPropertyValueStrategy(this.getPropertyValueStrategy());
        streamFormat.write(kdbxSerializableDatabase, credentials, outputStream);
        setDirty(false);
    }

    /**
     * Save the database with its stream format ({@link #getStreamFormat()}), and close the stream
     *
     * @param credentials  credentials to use
     * @param outputStream where to write to - closes stream
     * @deprecated use {@link #write(Credentials, OutputStream)} (issue #109)
     */
    @Override
    @Deprecated
    public void save(Credentials credentials, OutputStream outputStream) throws IOException {
        try (outputStream) {
            write(credentials, outputStream);
        }
    }

    /**
     * Save the database with a choice of stream format and close the stream
     *
     * @param streamFormat the format to use
     * @param credentials  credentials to use
     * @param outputStream where to write to - call closes output stream
     * @deprecated use {@link #write(StreamFormat, Credentials, OutputStream)} (issue #109)
     */
    @Override
    @Deprecated
    public <C extends StreamConfiguration> void save(StreamFormat<C> streamFormat, Credentials credentials,
                                                     OutputStream outputStream) throws IOException {
        try (outputStream) {
            write(streamFormat, credentials, outputStream);
        }
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
            this.keePassFile.meta.recycleBinChanged = Instant.now();
        }
        return g;
    }

    @Override
    public boolean supportsRecycleBin() {
        return true;
    }

    @Override
    public String getName() {
        return Objects.requireNonNullElse(keePassFile.meta.databaseName, "");
    }

    @Override
    public void setName(String s) {
        keePassFile.meta.databaseName = Objects.requireNonNullElse(s, "");
        keePassFile.meta.databaseNameChanged = Instant.now();
        setDirty(true);
    }

    @Override
    public String getDescription() {
        return Objects.requireNonNullElse(keePassFile.meta.databaseDescription, "");
    }

    @Override
    public void setDescription(String s) {
        keePassFile.meta.databaseDescription = Objects.requireNonNullElse(s, "");
        keePassFile.meta.databaseDescriptionChanged = Instant.now();
        setDirty(true);
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

    @Override
    public <C extends StreamConfiguration> void setStreamFormat(StreamFormat<C> streamFormat) {
        if (streamFormat == null) {
            throw new IllegalArgumentException("streamFormat may not be null");
        }
        this.streamFormat = streamFormat;
    }


    /**
     * On load add parents
     *
     * @param parent a parent to recurse
     */
    static void fixUp(KdbxGroup parent) {

        for (KdbxGroup group : parent.groups) {
            group.parent = parent;
            group.database = parent.database;
            fixUp(group);
        }

        for (KdbxEntry entry : parent.entries) {
            entry.database = parent.database;
            entry.parent = parent;
        }
    }


}
