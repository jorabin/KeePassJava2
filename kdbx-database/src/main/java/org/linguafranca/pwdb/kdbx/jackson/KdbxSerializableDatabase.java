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

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.api.WstxOutputProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.format.Helpers;
import org.linguafranca.pwdb.kdbx.jackson.converter.ValueDeserializer;
import org.linguafranca.pwdb.kdbx.jackson.converter.ValueSerializer;
import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;
import org.linguafranca.pwdb.security.StreamEncryptor;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class KdbxSerializableDatabase implements SerializableDatabase {

    /**
     * By default, deserialization will fail if an unknown property is found. Historically,
     * this has been because the file mapping is incomplete, rather than the incoming file being wrong.
     * So use this feature with caution, if at all.
     */
    public static boolean FAIL_ON_UNKNOWN_PROPERTIES = true;

    public KeePassFile keePassFile;
    private StreamEncryptor encryptor;

    private PropertyValue.Strategy propertyValueStrategy = new PropertyValue.Strategy.Default();

    public static KeePassFile createEmptyDatabase()  {

        InputStream inputStream = KdbxSerializableDatabase.class.getClassLoader()
                .getResourceAsStream("base.kdbx.xml");
        XmlMapper mapper = new XmlMapper();
        try {
            return mapper.readValue(inputStream, KeePassFile.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public KdbxSerializableDatabase() {
    }

    public KdbxSerializableDatabase(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
    }

    @Override
    public KdbxSerializableDatabase load(InputStream inputStream) throws IOException {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                KdbxSerializableDatabase.FAIL_ON_UNKNOWN_PROPERTIES);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(PropertyValue.class, new ValueDeserializer(encryptor, propertyValueStrategy));
        mapper.registerModule(module);
        keePassFile = mapper.readValue(inputStream, KeePassFile.class);
        return this;
    }


    @Override
    public void save(OutputStream outputStream) {
        try {
            SimpleModule module = new SimpleModule();
            module.addSerializer(PropertyValue.class, new ValueSerializer(encryptor, propertyValueStrategy));
            // disable auto-detection, only use annotated values
            XmlMapper mapper = XmlMapper.builder()
                    .disable(MapperFeature.AUTO_DETECT_CREATORS,
                            MapperFeature.AUTO_DETECT_FIELDS,
                            MapperFeature.AUTO_DETECT_GETTERS,
                            MapperFeature.AUTO_DETECT_SETTERS,
                            MapperFeature.AUTO_DETECT_IS_GETTERS)
                    .build();
            mapper.registerModule(module);
            mapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

            // set the serializer to Woodstox
            System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
            xmlOutputFactory.setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, true);
            xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);
            xmlOutputFactory.setProperty(WstxInputProperties.P_RETURN_NULL_FOR_DEFAULT_NAMESPACE, true);
            
            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
            XMLStreamWriter sw = xmlOutputFactory.createXMLStreamWriter(osw);
            try {
                sw.setPrefix("xml", "http://www.w3.org/XML/1998/namespace");

                mapper.writeValue(sw, keePassFile);

                sw.writeEndDocument();
            } finally {
                sw.close();
                osw.close();
            }

        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public byte[] getHeaderHash() {
        return keePassFile.meta.headerHash;
    }

    @Override
    public void setHeaderHash(byte[] hash) {
        this.keePassFile.meta.headerHash = hash;
    }

    
    public static void addBinary(KeePassFile keePassFile, int index, byte[] payload) {
        KeePassFile.Binary newBin = new KeePassFile.Binary();
        newBin.setId(index);
        newBin.setValue(Helpers.encodeBase64Content(payload, true));
        newBin.setCompressed(true);
        if (keePassFile.meta.binaries == null) {
            keePassFile.createBinaries();
        }
        keePassFile.meta.binaries.add(newBin);
    }

    // TODO this gets binary at index but does not get binary with ID
    @Override
    public byte[] getBinary(int index) {
        KeePassFile.Binary binary = keePassFile.meta.binaries.get(index);
        String value = binary.getValue();
        return Helpers.decodeBase64Content(value.getBytes(), binary.getCompressed());
    }

    @Override
    public int getBinaryCount() {
        if (Objects.isNull(keePassFile.meta.binaries)){
            return 0;
        }
        return keePassFile.meta.binaries.size();
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

    @Override
    public StreamEncryptor getEncryption() {
        return this.encryptor;
    }

    @Override
    public void setEncryption(StreamEncryptor encryption) {
       this.encryptor = encryption;
    }

    @Override
    public void addBinary(int index, byte[] payload) {
        addBinary(keePassFile, index, payload);
    }

    public PropertyValue.Strategy getPropertyValueStrategy() {
        return propertyValueStrategy;
    }

    public void setPropertyValueStrategy(PropertyValue.Strategy propertyValueStrategy) {
        this.propertyValueStrategy = propertyValueStrategy;
    }

}
