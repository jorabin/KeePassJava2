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

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import org.apache.commons.codec.binary.Base64;
import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.jaxb.base.ValueBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.*;
import org.linguafranca.pwdb.security.StreamEncryptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class JaxbSerializableDatabase implements SerializableDatabase {

    private final ObjectFactory objectFactory = new ObjectFactory();
    protected KeePassFile keePassFile;
    private StreamEncryptor encryption;

    public JaxbSerializableDatabase() {

    }

    public JaxbSerializableDatabase(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
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

    @Override
    public JaxbSerializableDatabase load(InputStream inputStream) {
        try {
            JAXBContext jc = JAXBContext.newInstance(KeePassFile.class, ValueBinding.class);
            Unmarshaller u = jc.createUnmarshaller();
            u.setListener(new Unmarshaller.Listener() {
                @Override
                public void afterUnmarshal(Object target, Object parent) {
                    if (target instanceof StringField.Value) {
                        StringField.Value value = (StringField.Value) target;
                        if (value.getProtected() != null && value.getProtected()) {
                            byte[] encrypted = Base64.decodeBase64(value.getValue().getBytes());
                            String decrypted = new String(encryption.decrypt(encrypted), StandardCharsets.UTF_8);
                            value.setValue(decrypted);
                            value.setProtected(null);
                            value.protectOnOutput=true;
                        }
                    }
                    if (target instanceof JaxbGroupBinding && (parent instanceof JaxbGroupBinding)) {
                        ((JaxbGroupBinding) target).parent = ((JaxbGroupBinding) parent);
                    }
                    if (target instanceof JaxbEntryBinding && (parent instanceof JaxbGroupBinding)) {
                        ((JaxbEntryBinding) target).parent = ((JaxbGroupBinding) parent);
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
    public void save(OutputStream outputStream) {
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
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setListener(new Marshaller.Listener() {
                String savedValue="";

                // this changes the content on save, so we need to change it back again, see comment below
                @Override
                public void beforeMarshal(Object source) {
                    if (source instanceof StringField) {
                        StringField field = (StringField) source;
                        if (toEncrypt.contains(field.getKey()) || field.getValue().protectOnOutput) {
                            savedValue = field.getValue().getValue();
                            byte [] encrypted = encryption.encrypt(field.getValue().getValue().getBytes(StandardCharsets.UTF_8));
                            byte [] base64Encoded = Base64.encodeBase64(encrypted);
                            field.getValue().setValue(new String(base64Encoded));
                            field.getValue().setProtected(true);
                        } else {
                            field.getValue().setProtected(false);
                        }
                        field.getValue().setProtectInMemory(false);
                    }
                }

                // turns out that undoing the content change we made in beforeMarshal is the easiest way of doing this
                // after a couple of days of looking at it. Making a clone before serialization
                // is not practical and creating an adapter is not practical either, believe me, I tried.
                // That said, if you are a JAXB whizz, and you know better ...
                @Override
                public void afterMarshal(Object source) {
                    if (source instanceof StringField) {
                        StringField field = (StringField) source;
                        if (field.getValue().getProtected()) {
                            field.getValue().setValue(savedValue);
                            field.getValue().setProtected(false);
                        }
                    }
                }
            });

            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);

            // this tidies up output of boolean ="False" attributes and indentation, which is incorrect
            // in marshaller and also uses tabs rather than spaces, which is more economical
            IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(xmlStreamWriter){

                @Override
                public void writeStartDocument() throws XMLStreamException {
                    setIndentStep("\t");
                    super.writeStartDocument();
                }

                @Override
                public void writeAttribute(String localName, String value) throws XMLStreamException {
                    if (localName.equals("ProtectInMemory")) {
                        return;
                    }
                    if (localName.equals("Protected")) {
                        if (!value.equalsIgnoreCase("true")){
                            return;
                        }
                    }
                    super.writeAttribute(localName, value);
                }
            };
            marshaller.marshal(keePassFile, writer);

        } catch (Exception e) {
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

    @Override
    public byte[] getBinary(int index) {
        return keePassFile.getMeta().getBinaries().getBinary().get(index).getValue();
    }

    @Override
    public int getBinaryCount() {
        if (Objects.isNull(keePassFile.getMeta().getBinaries())) {
            return 0;
        }
        return keePassFile.getMeta().getBinaries().getBinary().size();
    }

    public KeePassFile getKeePassFile() {
        return keePassFile;
    }

    public void setKeePassFile(KeePassFile keypassFile) {
        this.keePassFile = keypassFile;
    }
}
