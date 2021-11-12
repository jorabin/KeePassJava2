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

import org.apache.commons.codec.binary.Base64;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.SerializableDatabase;
import org.linguafranca.pwdb.kdbx.StreamEncryptor;
import org.linguafranca.pwdb.kdbx.jaxb.binding.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class JaxbSerializableDatabase implements SerializableDatabase {

    protected KeePassFile keePassFile;
    private StreamEncryptor encryption;
    private ObjectFactory objectFactory = new ObjectFactory();


    @Override
    public JaxbSerializableDatabase load(InputStream inputStream) {
        try {
            JAXBContext jc = JAXBContext.newInstance(KeePassFile.class);
            Unmarshaller u = jc.createUnmarshaller();
            u.setListener(new Unmarshaller.Listener() {
                @Override
                public void afterUnmarshal(Object target, Object parent) {
                    try {
                        if (target instanceof StringField.Value) {
                            StringField.Value value = (StringField.Value) target;
                            if (value.getProtected() !=null && value.getProtected()) {
                                byte[] encrypted = Base64.decodeBase64(value.getValue().getBytes());
                                String decrypted = new String(encryption.decrypt(encrypted), "UTF-8");
                                value.setValue(decrypted);
                                value.setProtected(false);
                            }
                        }
                        if (target instanceof JaxbGroupBinding && (parent instanceof JaxbGroupBinding)) {
                            ((JaxbGroupBinding) target).parent = ((JaxbGroupBinding) parent);
                        }
                        if (target instanceof JaxbEntryBinding && (parent instanceof JaxbGroupBinding)) {
                            ((JaxbEntryBinding) target).parent = ((JaxbGroupBinding) parent);
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException();
                    }
                }
            });
            keePassFile = (KeePassFile) u.unmarshal(inputStream);
            return this;
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(OutputStream outputStream) throws IOException {
        final List<String> toEncrypt = new ArrayList<>();
        if (keePassFile.getMeta().getMemoryProtection().getProtectTitle()) {
            toEncrypt.add(org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_TITLE);
        }
        if (keePassFile.getMeta().getMemoryProtection().getProtectURL()) {
            toEncrypt.add(org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_URL);
        }
        if (keePassFile.getMeta().getMemoryProtection().getProtectUserName()) {
            toEncrypt.add(org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_USER_NAME);
        }
        if (keePassFile.getMeta().getMemoryProtection().getProtectPassword()) {
            toEncrypt.add(org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_PASSWORD);
        }
        if (keePassFile.getMeta().getMemoryProtection().getProtectNotes()) {
            toEncrypt.add(org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME_NOTES);
        }
        try {
            JAXBContext jc = JAXBContext.newInstance(KeePassFile.class);
            Marshaller u = jc.createMarshaller();
            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            u.setListener(new Marshaller.Listener() {
                @Override
                public void beforeMarshal(Object source) {
                    try {
                        if (source instanceof StringField) {
                            StringField field = (StringField) source;
                            if (toEncrypt.contains(field.getKey())) {
                                byte[] encrypted = encryption.encrypt(field.getValue().getValue().getBytes());
                                String b64 = new String(Base64.encodeBase64(encrypted), "UTF-8");
                                field.getValue().setValue(b64);
                                field.getValue().setProtected(true);
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException();
                    }
                }
            });
            u.marshal(keePassFile, outputStream);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
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
        return keePassFile.getMeta().getHeaderHash();
    }

    @Override
    public void setHeaderHash(byte[] hash) {
        keePassFile.getMeta().setHeaderHash(hash);
    }

    @Override
    public void addBinary(int index, byte[] value) {
        addBinary(keePassFile, objectFactory, index, value);
    }

    public static void addBinary(KeePassFile keePassFile, ObjectFactory objectFactory, int index, byte[] value) {
        // create a new binary to put in the store
        Binaries.Binary newBin = objectFactory.createBinariesBinary();
        newBin.setID(index);
        newBin.setValue(Helpers.zipBinaryContent(value));
        newBin.setCompressed(true);
        if (keePassFile.getMeta().getBinaries() == null) {
            keePassFile.getMeta().setBinaries(objectFactory.createBinaries());
        }
        keePassFile.getMeta().getBinaries().getBinary().add(newBin);
    }

    public KeePassFile getKeePassFile() {
        return keePassFile;
    }

    public void setKeePassFile(KeePassFile keypassFile) {
        this.keePassFile = keypassFile;
    }
}
