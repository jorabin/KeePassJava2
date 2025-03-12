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
package org.linguafranca.pwdb.kdbx.jackson.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.apache.commons.codec.binary.Base64;
import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.security.StreamEncryptor;

import java.io.IOException;


public class ValueSerializer extends StdSerializer<PropertyValue> {

    private final StreamEncryptor encryptor;

    public ValueSerializer(StreamEncryptor encryptor) {
        super(ValueSerializer.class, false);
        this.encryptor = encryptor;
    }

    private String encrypt(byte[] bytes) {
        //Cipher
        byte[] encrypted = encryptor.encrypt(bytes);
        //Convert to base64
        return new String(Base64.encodeBase64(encrypted));
    }

    @Override
    public void serialize(PropertyValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        final ToXmlGenerator xmlGenerator = (ToXmlGenerator) gen;
        xmlGenerator.writeStartObject();

        String stringToWrite = value.isProtected() ?
                encrypt(value.getValueAsBytes()) :
                value.getValueAsString();

        if (value.isProtected()) {
            xmlGenerator.setNextIsAttribute(true);
            xmlGenerator.writeStringField("Protected", "True");
        }
        xmlGenerator.setNextIsAttribute(false);
        xmlGenerator.setNextIsUnwrapped(true);
        xmlGenerator.writeStringField("text", stringToWrite);
        xmlGenerator.writeEndObject();
    }
}