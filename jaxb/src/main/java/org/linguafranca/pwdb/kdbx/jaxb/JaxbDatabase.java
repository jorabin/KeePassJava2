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

package org.linguafranca.pwdb.kdbx.jaxb;

import org.jetbrains.annotations.NotNull;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.base.AbstractDatabase;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.pwdb.kdbx.jaxb.binding.Binaries;
import org.linguafranca.pwdb.kdbx.jaxb.binding.KeePassFile;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

/**
 * Implementation of {@link org.linguafranca.pwdb.Database} for JAXB.
 *
 * @author jo
 */

public class JaxbDatabase extends AbstractDatabase<JaxbDatabase, JaxbGroup, JaxbEntry, JaxbIcon> {

    private KeePassFile keePassFile;
    private ObjectFactory objectFactory = new ObjectFactory();
    private JaxbGroup root;

    public JaxbDatabase() {
        this(createEmptyDatabase().getKeePassFile());
    }

    private JaxbDatabase(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
        this.root = new JaxbGroup(this, keePassFile.getRoot().getGroup());
    }

    public static JaxbDatabase createEmptyDatabase() {
        InputStream inputStream = JaxbDatabase.class.getClassLoader().getResourceAsStream("base.kdbx.xml");
        KeePassFile keePassFile = new JaxbSerializableDatabase().load(inputStream).keePassFile;
        keePassFile.getRoot().getGroup().setUUID(UUID.randomUUID());
        return new JaxbDatabase(keePassFile);
    }

    public static JaxbDatabase load(Credentials creds, InputStream inputStream) {
        StreamFormat format = new KdbxStreamFormat();
        return load(format, creds, inputStream);
    }

    @NotNull
    public static JaxbDatabase load(StreamFormat format, Credentials creds, InputStream inputStream) {
        JaxbSerializableDatabase db = new JaxbSerializableDatabase();
        try {
            format.load(db, creds, inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return new JaxbDatabase(db.getKeePassFile());
    }

    @Override
    public void save(Credentials creds, OutputStream outputStream) throws IOException {
        save(new KdbxStreamFormat(), creds, outputStream);
    }

    public void save(StreamFormat format, Credentials creds, OutputStream outputStream) throws IOException {
        JaxbSerializableDatabase jsd = new JaxbSerializableDatabase();
        jsd.setKeePassFile(this.keePassFile);
        format.save(jsd, creds, outputStream);
        setDirty(false);
    }

    @Override
    public JaxbGroup getRootGroup() {
        return root;
    }

    @Override
    public JaxbGroup getRecycleBin() {
        UUID recycleBinUuid = this.keePassFile.getMeta().getRecycleBinUUID();
        JaxbGroup g = findGroup(recycleBinUuid);
        if (g == null && !isRecycleBinEnabled()) {
            return null;
        }
        if (g == null) {
            g = newGroup("Recycle Bin");
            getRootGroup().addGroup(g);
            this.keePassFile.getMeta().setRecycleBinUUID(g.getUuid());
            this.keePassFile.getMeta().setRecycleBinChanged(new Date());
        }
        return g;
    }

    @Override
    public boolean isRecycleBinEnabled() {
        return this.keePassFile.getMeta().getRecycleBinEnabled();
    }

    @Override
    public void enableRecycleBin(boolean enable) {
        this.keePassFile.getMeta().setRecycleBinEnabled(enable);
    }

    @Override
    public JaxbGroup newGroup() {
        return new JaxbGroup(this);
    }

    @Override
    public JaxbEntry newEntry() {
        return new JaxbEntry(this);
    }

    @Override
    public JaxbIcon newIcon() {
        return new JaxbIcon();
    }

    @Override
    public JaxbIcon newIcon(Integer i) {
        return new JaxbIcon(i);
    }

    @Override
    public String getDescription() {
        return keePassFile.getMeta().getDatabaseDescription();
    }

    @Override
    public void setDescription(String description) {
        keePassFile.getMeta().setDatabaseDescription(description);
    }

    @Override
    public boolean shouldProtect(String propertyName) {
        switch (propertyName.toLowerCase()) {
            case "title":
                return keePassFile.getMeta().getMemoryProtection().getProtectTitle();
            case "username":
                return keePassFile.getMeta().getMemoryProtection().getProtectUserName();
            case "password":
                return keePassFile.getMeta().getMemoryProtection().getProtectPassword();
            case "url":
                return keePassFile.getMeta().getMemoryProtection().getProtectURL();
            case "notes":
                return keePassFile.getMeta().getMemoryProtection().getProtectNotes();
            default:
                return false;
        }
    }

    @Override
    public String getName() {
        return keePassFile.getMeta().getDatabaseName();
    }

    @Override
    public void setName(String s) {
        keePassFile.getMeta().setDatabaseName(s);
        keePassFile.getMeta().setDatabaseNameChanged(new Date());
    }

    public KeePassFile getKeePassFile() {
        return keePassFile;
    }

    ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void createBinary(byte[] value, Integer index) {
        JaxbSerializableDatabase.addBinary(getKeePassFile(), getObjectFactory(), index, value);
    }
}
