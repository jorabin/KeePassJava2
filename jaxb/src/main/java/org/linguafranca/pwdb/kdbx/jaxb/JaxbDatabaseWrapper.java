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
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.base.AbstractDatabase;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.pwdb.kdbx.jaxb.binding.KeePassFile;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;
import org.linguafranca.security.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class JaxbDatabaseWrapper extends AbstractDatabase {

    private KeePassFile keePassFile;
    private ObjectFactory objectFactory = new ObjectFactory();
    private JaxbGroupWrapper root;

    public JaxbDatabaseWrapper() {
        this(JaxbSerializableDatabase.createEmptyDatabase().getKeePassFile());
    }

    public JaxbDatabaseWrapper(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
        this.root = new JaxbGroupWrapper(this, null, keePassFile.getRoot().getGroup());
        this.root.isRootGroup = true;
        // initialise all wrappers
        visit(new Visitor.Default() {
        });
    }

    public static JaxbDatabaseWrapper load(Credentials creds, InputStream inputStream) {
        StreamFormat format = new KdbxStreamFormat();
        return load(format, creds, inputStream);
    }

    @NotNull
    public static JaxbDatabaseWrapper load(StreamFormat format, Credentials creds, InputStream inputStream) {
        JaxbSerializableDatabase db = new JaxbSerializableDatabase();
        try {
            format.load(db, creds, inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return new JaxbDatabaseWrapper(db.getKeePassFile());
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
    public Group getRootGroup() {
        return root;
    }

    @Override
    public Group newGroup() {
        return new JaxbGroupWrapper(this, null, objectFactory.createGroup());
    }

    @Override
    public Entry newEntry() {
        return new JaxbEntryWrapper(this, null);
    }

    @Override
    public Icon newIcon() {
        return new JaxbIconWrapper();
    }

    @Override
    public Icon newIcon(Integer i) {
        return new JaxbIconWrapper(i);
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
            case "title":  return keePassFile.getMeta().getMemoryProtection().getProtectTitle();
            case "username":  return keePassFile.getMeta().getMemoryProtection().getProtectUserName();
            case "password":  return keePassFile.getMeta().getMemoryProtection().getProtectPassword();
            case "url":  return keePassFile.getMeta().getMemoryProtection().getProtectURL();
            case "notes":  return keePassFile.getMeta().getMemoryProtection().getProtectNotes();
            default: return false;
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
}
