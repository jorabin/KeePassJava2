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

package org.linguafranca.pwdb.basic;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.protect.ProtectedDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * This class is responsible for serializing and deserializing a BasicDatabase to and from a stream.
 */
public interface BasicDatabaseSerializer {

    /**
     * Serialize a BasicDatabase to a stream
     *
     * @param database     the database to serialize
     * @param outputStream the stream to serialize to
     */
    void save(BasicDatabase database, OutputStream outputStream) throws IOException;

    /**
     * Deserialize a BasicDatabase from a stream
     *
     * @param inputStream the serialized form of the database
     * @return the deserialized database
     */
    BasicDatabase load(InputStream inputStream) throws IOException;

    /**
     * XML implementation of containing BasicDatabaseSerializer
     */
    class Xml implements BasicDatabaseSerializer {
        private final ObjectMapper objectMapper = init();

        @Override
        public void save(BasicDatabase database, OutputStream outputStream) throws IOException {
            objectMapper.writer().withRootName("database").writeValue(outputStream, database);
        }

        @Override
        public BasicDatabase load(InputStream inputStream) throws IOException {
            BasicDatabase database2 = objectMapper.readValue(inputStream, BasicDatabase.class);
            database2.visit(new BasicDatabase.FixupVisitor(database2));
            return database2;
        }

        private ObjectMapper init() {
            SimpleModule module = new SimpleModule()
                    .addDeserializer(PropertyValue.class, new PropertyValueDeserializer())
                    .addSerializer(PropertyValue.class, new PropertyValueSerializer());
            XmlMapper mapper = XmlMapper.builder()
                    // ignore everything except fields
                    .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                    .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
                    .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
                    .visibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE)
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    // even though the source file is not annotated, we want annotations in the Configuration.class
                    .enable(MapperFeature.USE_ANNOTATIONS)
                    // add mixin to main classes
                    .addMixIn(BasicGroup.class, Configuration.class)
                    .addMixIn(BasicEntry.class, Configuration.class)
                    .addMixIn(BasicDatabase.class, Configuration.class)
                    .build();


            mapper
                    // for PropertyValue serialization
                    .registerModule(module)
                    // for Instant processing
                    .registerModule(new JavaTimeModule())
                    // pretty print
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    // suppress empty fields
                    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

            return mapper;
        }

        /**
         * Configure special treatment on serialization of properties
         */
        public static class PropertyValueSerializer extends JsonSerializer<PropertyValue> {
            /**
             * Don't serialize empty properties
             * <p>
             * TODO: at present, policy is to have all 5 standard properties present with empty
             * value if necessary. Review policy. Deserialization messes up constructor
             * behaviour which is to initialise those properties that way.
             */
            @Override
            public boolean isEmpty(SerializerProvider provider, PropertyValue value) {
                // TODO: return (value.getValueAsString().isEmpty());
                return false;
            }

            /**
             * write protected properties with a flag to show they need to be protected on input
             */
            @Override
            public void serialize(PropertyValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

                final ToXmlGenerator xmlGenerator = (ToXmlGenerator) gen;
                xmlGenerator.writeStartObject();

                if (value.isProtected()) {
                    xmlGenerator.setNextIsAttribute(true);
                    xmlGenerator.writeStringField("protected", "true");
                }
                xmlGenerator.setNextIsAttribute(false);
                xmlGenerator.setNextIsUnwrapped(true);
                xmlGenerator.writeStringField("text", value.getValueAsString());
                xmlGenerator.writeEndObject();
            }
        }

        /**
         * Property deserialization
         */
        public static class PropertyValueDeserializer extends JsonDeserializer<PropertyValue> {
            PropertyValue.Strategy strategy = new PropertyValue.Strategy.Default();

            @Override
            public PropertyValue deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                JsonNode node = jsonParser.getCodec().readTree(jsonParser);

                if (node.isTextual()) {
                    return strategy.newUnprotected().of(node.textValue());
                }
                if (node.has("protected")) {
                    return strategy.newProtected().of(node.get("").asText());
                }
                throw new IllegalStateException("Unknown property value format " + node);
            }
        }

        /**
         * Class is for binding Jackson annotations to a later Jackson "Mix-In"
         */
        public abstract static class Configuration {
            // Tell Jackson how to treat lists of Entry and Groups
            @JacksonXmlProperty(localName = "entry")
            @JacksonXmlElementWrapper(useWrapping = false)
            protected List<BasicEntry> entries;
            @JacksonXmlProperty(localName = "group")
            @JacksonXmlElementWrapper(useWrapping = false)
            protected List<BasicGroup> groups;
            // tell Jackson to ignore these fields
            @JsonIgnore
            BasicGroup parent;
            @JsonIgnore
            BasicDatabase database;
            @JsonIgnore
            ProtectedDatabase valueStrategy;
            @JsonIgnore
            boolean isDirty;
        }
    }
}
