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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.jaxb.binding.*;
import org.linguafranca.pwdb.kdbx.dom.DomHelper;
import org.linguafranca.pwdb.security.StreamEncryptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
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
            JAXBContext jc = JAXBContext.newInstance(KeePassFile.class);
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
                            value.setProtectInMemory(true);
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
            // Create the Document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();

            JAXBContext jc = JAXBContext.newInstance(KeePassFile.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.marshal(keePassFile, document);

            // encrypt and base64 every element marked as protected
            NodeList protectedContent = (NodeList) DomHelper.xpath.evaluate("//*[@Protected='true']", document, XPathConstants.NODESET);
            for (int i = 0; i < protectedContent.getLength(); i++){
                Element element = ((Element) protectedContent.item(i));
                String decrypted = DomHelper.getElementContent(".", element);
                if (decrypted == null) {
                    decrypted = "";
                }
                byte[] encrypted = encryption.encrypt(decrypted.getBytes());
                // Android compatibility
                String base64 = new String(Base64.encodeBase64(encrypted));
                DomHelper.setElementContent(".", element, base64);
            }

            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.transform(new DOMSource(document), new StreamResult(outputStream));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } catch (JAXBException | XPathExpressionException e) {
            throw new IllegalStateException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
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
