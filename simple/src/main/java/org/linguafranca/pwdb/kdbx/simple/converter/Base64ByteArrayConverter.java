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

package org.linguafranca.pwdb.kdbx.simple.converter;

import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.simple.model.KeePassFile;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * @author jo
 */
public class Base64ByteArrayConverter implements Converter<KeePassFile.ByteArray>{
    @Override
    public KeePassFile.ByteArray read(InputNode inputNode) throws Exception {
        String input = inputNode.getValue();
        byte[] value = input == null? new byte[0] : input.getBytes();
        return new KeePassFile.ByteArray(Helpers.decodeBase64Content(value, false));
    }

    @Override
    public void write(OutputNode outputNode, KeePassFile.ByteArray bytes) throws Exception {
        outputNode.setValue(Helpers.encodeBase64Content(bytes.getContent(), false));
    }
}
