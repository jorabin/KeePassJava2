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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.codec.binary.Base64;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.jackson.model.EntryClasses;
import org.linguafranca.pwdb.security.StreamEncryptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ValueDeserializer extends StdDeserializer<EntryClasses.StringProperty.Value> {

    private final StreamEncryptor encryptor;

    public ValueDeserializer(StreamEncryptor encryptor) {
        super(ValueDeserializer.class);
        this.encryptor = encryptor;
    }

    @Override
    public EntryClasses.StringProperty.Value deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        JsonNode node = p.getCodec().readTree(p);

        EntryClasses.StringProperty.Value result = new EntryClasses.StringProperty.Value();

        if (node.isTextual()) {
            result.setText(node.textValue());
            return result;
        }
        if (node.isObject()) {
            // TODO not clear what is happening here, looks like it's not exactly correct
            //We need to decrypt all Protected values
            if (node.has("Protected") && Boolean.TRUE.equals(Helpers.toBoolean(node.get("Protected").asText()))) {
                if (node.has("")) {
                    String cipherText = node.get("").asText();
                    if (cipherText != null && !cipherText.isEmpty()) {

                        //Decode to byte the Base64 text
                        byte[] encrypted = Base64.decodeBase64(cipherText.getBytes());
                        String decrypted = new String(encryptor.decrypt(encrypted), StandardCharsets.UTF_8);
                        result.setText(decrypted);
                        result.setProtectOnOutput(true);
                    }
                }
            } else {
                //If an element is not marked us Protected we need to copy the value as is
                if (node.has("ProtectInMemory")) {
                    Boolean protectInMemory = Helpers.toBoolean(node.get("ProtectInMemory").asText());
                    result.setProtectInMemory(protectInMemory);

                    if (node.has("")) {
                        result.setText(node.get("").asText());
                    }
                }
            }
        }
        return result;
    }
}