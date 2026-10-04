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

import org.linguafranca.pwdb.io.NonClosingInputStream;
import org.linguafranca.pwdb.io.NonClosingOutputStream;
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
        public void read(SerializableDatabase serializableDatabase, Credentials credentials, InputStream inputStream) throws IOException {
            serializableDatabase.setEncryption(new StreamEncryptor.None());
            serializableDatabase.read(inputStream);
        }

        @Override
        public void write(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream outputStream) throws IOException {
            serializableDatabase.setEncryption(new StreamEncryptor.None());
            serializableDatabase.write(outputStream);
            outputStream.flush();
        }

        @Override
        @Deprecated
        public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream inputStream) throws IOException {
            try (inputStream) {
                read(serializableDatabase, credentials, inputStream);
            }
        }

        @Override
        @Deprecated
        public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream outputStream) throws IOException {
            try (outputStream) {
                write(serializableDatabase, credentials, outputStream);
            }
        }

        @Override
        public StreamConfiguration.None getStreamConfiguration() {
            return new StreamConfiguration.None();
        }

        @Override
        public void setStreamConfiguration(StreamConfiguration.None configuration) {

        }
    }

    /**
     * Read a database from a stream in this format, leaving the stream open
     *
     * @param serializableDatabase the database to read into
     * @param credentials credentials to use
     * @param encryptedInputStream where to read from - the caller closes it
     * @since 3.1.0
     */
    default void read(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException {
        load(serializableDatabase, credentials, new NonClosingInputStream(encryptedInputStream));
    }

    /**
     * Write a database to a stream in this format, leaving the stream open
     *
     * @param serializableDatabase the database to write
     * @param credentials credentials to use
     * @param encryptedOutputStream where to write to - the caller closes it
     * @since 3.1.0
     */
    default void write(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) throws IOException {
        save(serializableDatabase, credentials, new NonClosingOutputStream(encryptedOutputStream));
    }

    /**
     * Load a database from a stream in this format and close the stream
     *
     * @deprecated closes a stream the caller opened; use {@link #read(SerializableDatabase, Credentials, InputStream)} (issue #109)
     */
    @Deprecated
    void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException;

    /**
     * Save a database to a stream in this format and close the stream
     *
     * @deprecated closes a stream the caller opened; use {@link #write(SerializableDatabase, Credentials, OutputStream)} (issue #109)
     */
    @Deprecated
    void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) throws IOException;

    C getStreamConfiguration();

    void setStreamConfiguration(C configuration);
}
