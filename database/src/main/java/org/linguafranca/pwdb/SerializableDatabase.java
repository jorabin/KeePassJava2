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
 * This interface allows for serialization and deserialization of databases, which is
 * theoretically decoupled from the actual format they use for serialization.
 * <p>
 * Databases instantiate themselves from a stream and serialize to a stream,
 * and need to be able to encrypt and decrypt data (e.g. Protected fields in KDBX format).
 * <p>
 * KDBX V3 databases contain a header hash (i.e. a hash of the contents of
 * some portion of the {@link StreamFormat} they have been loaded from or saved to).
 * Which means that databases must support the setting of this value after the header
 * has been written on save, and reading the value after load to allow for integrity checking.
 * <p>
 * KDBX V4 databases have their attachments in the header so databases need to support setting
 * and getting of attachments for serialization
 */
public interface SerializableDatabase {

    /**
     * Read the database from a stream, leaving the stream open
     *
     * @param inputStream where to read from - the caller closes it
     * @since 3.1.0
     */
    default SerializableDatabase read(InputStream inputStream) throws IOException {
        return load(new NonClosingInputStream(inputStream));
    }

    /**
     * Write the database to a stream, leaving the stream open
     *
     * @param outputStream where to write to - the caller closes it
     * @since 3.1.0
     */
    default void write(OutputStream outputStream) throws IOException {
        save(new NonClosingOutputStream(outputStream));
    }

    /**
     * Load the database from a stream and close the stream
     *
     * @deprecated closes a stream the caller opened; use {@link #read(InputStream)} (issue #109)
     */
    @Deprecated
    SerializableDatabase load(InputStream inputStream) throws IOException;

    /**
     * Save the database to a stream and close the stream
     *
     * @deprecated closes a stream the caller opened; use {@link #write(OutputStream)} (issue #109)
     */
    @Deprecated
    void save(OutputStream outputStream) throws IOException;

    /**
     * The lowest minor version of the format's major version given that can hold all of this database's
     * content, for formats such as KDBX 4 whose minor versions add content
     *
     * @param majorVersion the major version
     * @return the minor version, 0 by default
     * @since 3.1.0
     */
    default int getMinimumMinorVersion(int majorVersion) {
        return 0;
    }

    /**
     * Tells the database the version of the format it is about to be written in, so that it can leave out
     * content that version can't hold. By default this is ignored.
     *
     * @param majorVersion the major version
     * @param minorVersion the minor version
     * @since 3.1.0
     */
    default void setFormatVersion(int majorVersion, int minorVersion) {
    }

    StreamEncryptor getEncryption();

    void setEncryption(StreamEncryptor encryption);

    byte[] getHeaderHash();

    void setHeaderHash(byte[] hash);

    void addBinary(int index, byte[] payload);

    byte[] getBinary(int index);

    int getBinaryCount();
}
