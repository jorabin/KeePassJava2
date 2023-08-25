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
package org.linguafranca.pwdb.kdbx.jackson.converter;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.linguafranca.pwdb.kdbx.jackson.model.EntryClasses;
import org.linguafranca.pwdb.kdbx.jackson.model.EntryClasses.StringProperty.Value;
import org.linguafranca.pwdb.security.StreamEncryptor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;


public class ValueSerializer extends StdSerializer<EntryClasses.StringProperty.Value>{

    private final StreamEncryptor encryptor;

    public ValueSerializer(StreamEncryptor encryptor) {
        super(ValueSerializer.class, false);
        this.encryptor = encryptor;
    }


    @Override
    public void serialize(Value value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        
        final ToXmlGenerator xmlGenerator = (ToXmlGenerator) gen;
        xmlGenerator.writeStartObject();

        String stringToWrite = value.getText();
        //We need to encrypt and convert to base64 every protected element
        if (value.getProtectOnOutput()) {
            xmlGenerator.setNextIsAttribute(true);
            xmlGenerator.writeStringField("Protected", "True");
            String plain = value.getText();
            if(plain == null) {
                plain = "";
            }
            //Cipher
            byte[] encrypted = encryptor.encrypt(plain.getBytes());
            //Convert to base64
            stringToWrite = new String(Base64.encodeBase64(encrypted));
        }

        xmlGenerator.setNextIsAttribute(false);
        xmlGenerator.setNextIsUnwrapped(true);
        xmlGenerator.writeStringField("text", stringToWrite);
        xmlGenerator.writeEndObject();
    }
}
