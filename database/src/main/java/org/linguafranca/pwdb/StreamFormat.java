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

package org.linguafranca.pwdb;

import org.linguafranca.pwdb.security.StreamEncryptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface provides for wrapping a database serialization in a stream format, e.g. KDBX or none.
 *
 * @author jo
 */
public interface StreamFormat <C extends StreamConfiguration>{
    /**
     * Class allows for serializing a database directly to or from a stream with no encryption etc
     */
    class None implements StreamFormat<StreamConfiguration.None> {

        @Override
        public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream inputStream) throws IOException {
            serializableDatabase.setEncryption(new StreamEncryptor.None());
            serializableDatabase.load(inputStream);
            inputStream.close();
        }

        @Override
        public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream outputStream) throws IOException {
            serializableDatabase.setEncryption(new StreamEncryptor.None());
            serializableDatabase.save(outputStream);
            outputStream.flush();
            outputStream.close();
        }

        @Override
        public StreamConfiguration.None getStreamConfiguration() {
            return new StreamConfiguration.None();
        }

        @Override
        public void setStreamConfiguration(StreamConfiguration.None configuration) {

        }
    }

    void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException;

    void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) throws IOException;

    C getStreamConfiguration();

    void setStreamConfiguration(C configuration);
}
