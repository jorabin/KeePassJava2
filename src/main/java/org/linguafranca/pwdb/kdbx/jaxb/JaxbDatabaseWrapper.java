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
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.SerializableDatabase;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.pwdb.kdbx.jaxb.binding.KeePassFile;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;
import org.linguafranca.pwdb.kdbx.jaxb.binding.StringField;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jo
 */
public class JaxbDatabaseWrapper extends AbstractDatabase {

    private KeePassFile keePassFile;
    private ObjectFactory objectFactory = new ObjectFactory();
    private Group root;

    public JaxbDatabaseWrapper(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
        this.root = new JaxbGroupWrapper(this, null, keePassFile.getRoot().getGroup());
        // initialise all wrappers
        visit(new Visitor.Default() {});
    }

    public static JaxbDatabaseWrapper load(KdbxCreds creds, InputStream inputStream) {
        StreamFormat format = new KdbxStreamFormat();
        JaxbSerializableDatabase db = new JaxbSerializableDatabase();

        return getJaxbDatabaseWrapper(format, creds, inputStream);
    }

    @NotNull
    private static JaxbDatabaseWrapper getJaxbDatabaseWrapper(StreamFormat format, KdbxCreds creds, InputStream inputStream) {
        JaxbSerializableDatabase db = new JaxbSerializableDatabase();
        try {
            format.load(db, creds, inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return new JaxbDatabaseWrapper(db.getKeePassFile());
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
        return new JaxbEntryWrapper(this, null, objectFactory.createEntry());
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

    public KeePassFile getKeePassFile() {
        return keePassFile;
    }
}
