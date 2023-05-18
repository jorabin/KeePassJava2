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

import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.simple.converter.EmptyStringConverter;
import org.linguafranca.pwdb.kdbx.simple.converter.ValueConverter;
import org.linguafranca.pwdb.kdbx.simple.model.EntryClasses;
import org.linguafranca.pwdb.kdbx.simple.model.KeePassFile;
import org.linguafranca.pwdb.security.StreamEncryptor;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class SimpleSerializableDatabase implements SerializableDatabase {

    public KeePassFile keePassFile;
    private StreamEncryptor encryption;

    public  SimpleSerializableDatabase(){

    }
    public SimpleSerializableDatabase(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
    }


    /**
     * Create an empty underlying KeePassFile instance
     *
     * @return a new database
     */
    static KeePassFile createEmptyDatabase() {
        InputStream inputStream = SimpleDatabase.class.getClassLoader().getResourceAsStream("base.kdbx.xml");
        try {
            return getSerializer(new StreamEncryptor.None()).read(KeePassFile.class, inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SimpleSerializableDatabase load(InputStream inputStream) {
        try {
            // decrypt the encrypted fields in the inner XML stream
            // InputStream plainTextXmlStream = new XmlInputStreamFilter(inputStream, new KdbxInputTransformer(encryption));
            // read the now entirely decrypted stream into database
            keePassFile = getSerializer(encryption).read(KeePassFile.class, inputStream);
            // ensure that parent fields are set
            fixUp(keePassFile.root.group);
            return this;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(OutputStream outputStream) throws IOException {
        // encrypt the fields in the XML inner stream
        // XmlOutputStreamFilter plainTextOutputStream = new XmlOutputStreamFilter(outputStream, new KdbxOutputTransformer(encryption));

        // set up the "protected" attributes of fields that need inner stream encryption
        prepareForSave(keePassFile.root.group);

        // and save the database out
        try {
            getSerializer(encryption).write(this.keePassFile, outputStream);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Utility to get a simple framework persister
     * @return a persister
     */
     public static Serializer getSerializer(StreamEncryptor encryption) {
        Registry registry = new Registry();
        try {
            registry.bind(String.class, EmptyStringConverter.class);
            registry.bind(EntryClasses.StringProperty.Value.class, new ValueConverter(encryption));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Strategy strategy = new AnnotationStrategy(new RegistryStrategy(registry));
        return new Persister(strategy);
    }

    /**
     * Utility to mark fields that need to be encrypted and vice versa
     *
     * @param parent the group to start from
     */
    private static void prepareForSave(SimpleGroup parent){
        for (SimpleGroup group: parent.group) {
            prepareForSave(group);
        }
        for (SimpleEntry entry: parent.entry) {
            for (EntryClasses.StringProperty property : entry.string) {
                boolean shouldProtect = parent.database.shouldProtect(property.getKey());
                property.getValue().setProtectOnOutput(shouldProtect || property.getValue().getProtectOnOutput());
            }
            if (Objects.nonNull(entry.history)) {
                for (SimpleEntry entry2 : entry.history) {
                    for (EntryClasses.StringProperty property : entry2.string) {
                        boolean shouldProtect = parent.database.shouldProtect(property.getKey());
                        property.getValue().setProtectOnOutput(shouldProtect || property.getValue().getProtectOnOutput());
                    }
                }
            }
        }
    }

    /**
     * On load add parents
     * @param parent a parent to recurse
     */
    static void fixUp(SimpleGroup parent){
        for (SimpleGroup group: parent.group) {
            group.parent = parent;
            group.database = parent.database;
            fixUp(group);
        }
        for (SimpleEntry entry: parent.entry) {
            entry.database = parent.database;
            entry.parent = parent;
        }
    }

    @Override
    public StreamEncryptor getEncryption() {
        return encryption;
    }

    @Override
    public void setEncryption(StreamEncryptor encryption) {
        this.encryption = encryption;
    }

    @Override
    public byte[] getHeaderHash() {
        return keePassFile.meta.headerHash.getContent();
    }

    @Override
    public void setHeaderHash(byte[] hash) {
        keePassFile.meta.headerHash = new KeePassFile.ByteArray(hash);
    }

    @Override
    public void addBinary(int index, byte[] value) {
        addBinary(keePassFile, index, value);
    }

    @Override
    public byte[] getBinary(int index) {
        KeePassFile.Binary binary = keePassFile.getBinaries().get(index);
        String value = binary.getValue();
        return Helpers.decodeBase64Content(value.getBytes(), binary.getCompressed());
    }

    @Override
    public int getBinaryCount() {
        if (Objects.isNull(keePassFile.getBinaries())){
            return 0;
        }
        return keePassFile.getBinaries().size();
    }

    public static void addBinary(KeePassFile keePassFile, int index, byte[] value) {
        // create a new binary to put in the store
        KeePassFile.Binary newBin = new KeePassFile.Binary();
        newBin.setId(index);
        newBin.setValue(Helpers.encodeBase64Content(value, true));
        newBin.setCompressed(true);
        if (keePassFile.getBinaries() == null) {
            keePassFile.createBinaries();
        }
        keePassFile.getBinaries().add(newBin);
    }



    public KeePassFile getKeePassFile() {
        return keePassFile;
    }

    public void setKeePassFile(KeePassFile keypassFile) {
        this.keePassFile = keypassFile;
    }
}
