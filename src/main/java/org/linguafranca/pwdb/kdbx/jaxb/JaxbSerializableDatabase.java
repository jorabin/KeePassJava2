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
import org.linguafranca.pwdb.kdbx.SerializableDatabase;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author jo
 */
public class JaxbSerializableDatabase implements SerializableDatabase {

    private KeePassFile keePassFile;
    private Encryption encryption;


    @Override
    public SerializableDatabase load(InputStream inputStream) {
        try {
            JAXBContext jc = JAXBContext.newInstance(KeePassFile.class);
            Unmarshaller u = jc.createUnmarshaller();
            keePassFile = (KeePassFile) u.unmarshal(inputStream);
            decryptGroup(keePassFile.getRoot().getGroup());
            return this;
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private void decryptGroup(Group group) {
        try {
            for (Entry entry : group.getEntry()) {
                for (StringField field : entry.getString()) {
                    if (field.getValue().getProtected()) {
                        byte[] encrypted = Base64.decodeBase64(field.getValue().getValue().getBytes());
                        String decrypted = new String(encryption.decrypt(encrypted), "UTF-8");
                        field.getValue().setValue(decrypted);
                        field.getValue().setProtected(false);
                    }
                }
            }
            for (Group group1 : group.getGroup()) {
                decryptGroup(group1);
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(OutputStream outputStream) throws IOException {
        List<String> toEncrypt = new ArrayList<>();
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
        encryptGroup(keePassFile.getRoot().getGroup(), toEncrypt);
        try {
            JAXBContext jc = JAXBContext.newInstance(KeePassFile.class);
            Marshaller u = jc.createMarshaller();
            u.marshal(keePassFile, outputStream);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private void encryptGroup(Group group, List<String> encrypt) {
        try {
            for (Entry entry : group.getEntry()) {
                for (StringField field : entry.getString()) {
                    if (encrypt.contains(field.getKey())) {
                        byte[] encrypted = encryption.encrypt(field.getValue().getValue().getBytes());
                        String b64 = new String(Base64.encodeBase64(encrypted), "UTF-8");
                        field.getValue().setValue(b64);
                        field.getValue().setProtected(true);
                    }
                }
            }
            for (Group group1 : group.getGroup()) {
                encryptGroup(group1, encrypt);
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Encryption getEncryption() {
        return encryption;
    }

    @Override
    public void setEncryption(Encryption encryption) {
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

    public KeePassFile getKeePassFile() {
        return keePassFile;
    }

    public static JaxbSerializableDatabase createEmptyDatabase () {
        JaxbSerializableDatabase result = new JaxbSerializableDatabase();

        ObjectFactory objectFactory = new ObjectFactory();
        result.keePassFile = objectFactory.createKeePassFile();
        KeePassFile.Meta meta = objectFactory.createKeePassFileMeta();
        result.keePassFile.setMeta(meta);
        Date now = new Date();
        meta.setGenerator("KeepassJava2");
        meta.setDatabaseName("New Database");
        meta.setDatabaseNameChanged(now);
        meta.setDatabaseDescription("New Database created by KeePassJava2");
        meta.setDatabaseDescriptionChanged(now);
        meta.setDefaultUserNameChanged(now);
        meta.setMaintenanceHistoryDays(365);
        meta.setMasterKeyChanged(now);
        meta.setMasterKeyChangeRec(-1);
        meta.setMasterKeyChangeForce(-1);

        KeePassFile.Meta.MemoryProtection p = objectFactory.createKeePassFileMetaMemoryProtection();
        p.setProtectTitle(false);
        p.setProtectUserName(false);
        p.setProtectPassword(true);
        p.setProtectURL(false);
        p.setProtectNotes(false);

        meta.setMemoryProtection(p);

        meta.setRecycleBinEnabled(true);
        meta.setRecycleBinUUID(new UUID(0,0));
        meta.setRecycleBinChanged(now);

        meta.setEntryTemplatesGroup(new UUID(0,0));
        meta.setEntryTemplatesGroupChanged(now);
        meta.setLastSelectedGroup(new UUID(0,0));
        meta.setLastTopVisibleGroup(new UUID(0,0));

        meta.setHistoryMaxItems(10);
        meta.setHistoryMaxSize(6291456);

        Group rootGroup = objectFactory.createGroup();
        rootGroup.setUUID(UUID.randomUUID());
        rootGroup.setName("Root");
        rootGroup.setIconID(48);

        Times times = objectFactory.createTimes();
        times.setLastModificationTime(now);
        times.setCreationTime(now);
        times.setLastAccessTime(now);
        times.setExpiryTime(now);
        times.setExpires(false);
        times.setUsageCount(0);
        times.setLocationChanged(now);
        rootGroup.setTimes(times);
        rootGroup.setIsExpanded(true);
        rootGroup.setEnableAutoType(true);
        rootGroup.setEnableSearching(true);
        rootGroup.setLastTopVisibleEntry(new UUID(0,0));

        KeePassFile.Root root = objectFactory.createKeePassFileRoot();
        result.keePassFile.setRoot(root);
        root.setGroup(rootGroup);

        return result;
    }
}
