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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.codec.binary.Base64;
import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.format.Helpers;
import org.linguafranca.pwdb.security.StreamEncryptor;

import java.io.IOException;

public class ValueDeserializer extends StdDeserializer<PropertyValue> {

    private final StreamEncryptor encryptor;
    private final PropertyValue.Strategy strategy;

    public ValueDeserializer(StreamEncryptor encryptor, PropertyValue.Strategy strategy) {
        super(ValueDeserializer.class);
        this.encryptor = encryptor;
        this.strategy = strategy;
    }

    @Override
    public PropertyValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        JsonNode node = p.getCodec().readTree(p);

        if (node.isTextual()) {
            return strategy.newUnprotected().of(node.textValue());
        }

        if (node.isObject()) {
            // TODO not clear what is happening here, looks like it's not exactly correct
            //We need to decrypt all Protected values
            String cipherText = "";
            if (node.has("")) {
                cipherText = node.get("").asText();
            }

            if (node.has("Protected") && Boolean.TRUE.equals(Helpers.toBoolean(node.get("Protected").asText()))) {
                    //Decode to byte the Base64 text
                    byte[] encrypted = Base64.decodeBase64(cipherText.getBytes());
                    byte[] decrypted = encryptor.decrypt(encrypted);
                    return strategy.newProtected().of(decrypted);
            }
            return strategy.newUnprotected().of(cipherText);
        }

        throw new IllegalStateException("Error parsing XML node type is " + node.getClass());
    }
}