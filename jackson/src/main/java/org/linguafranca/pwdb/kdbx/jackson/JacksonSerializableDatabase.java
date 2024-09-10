/*
 * Copyright 2023 Giuseppe Valente
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
package org.linguafranca.pwdb.kdbx.jackson;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.api.WstxOutputProperties;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.kdbx.Helpers;
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

public class JacksonSerializableDatabase implements SerializableDatabase {

    public KeePassFile keePassFile;
    private StreamEncryptor encryptor;

    public static KeePassFile createEmptyDatabase() throws IOException {

        InputStream inputStream = JacksonSerializableDatabase.class.getClassLoader()
                .getResourceAsStream("base.kdbx.xml");
        XmlMapper mapper = new XmlMapper();
        return mapper.readValue(inputStream, KeePassFile.class);

    }

    public JacksonSerializableDatabase() {
    }

    public JacksonSerializableDatabase(KeePassFile keePassFile) {
        this.keePassFile = keePassFile;
    }

    @Override
    public JacksonSerializableDatabase load(InputStream inputStream) throws IOException {
        XmlMapper mapper = new XmlMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PropertyValue.class, new ValueDeserializer(encryptor, new PropertyValue.Strategy.Default()));
        mapper.registerModule(module);
        keePassFile = mapper.readValue(inputStream, KeePassFile.class);
        return this;
    }


    @Override
    public void save(OutputStream outputStream) {
        try {

            SimpleModule module = new SimpleModule();
            module.addSerializer(PropertyValue.class, new ValueSerializer(encryptor));
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
    static void fixUp(JacksonGroup parent) {

        for (JacksonGroup group : parent.groups) {
            group.parent = parent;
            group.database = parent.database;
            fixUp(group);
        }

        for (JacksonEntry entry : parent.entries) {
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

}
