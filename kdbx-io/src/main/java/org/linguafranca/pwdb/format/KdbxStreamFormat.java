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

package org.linguafranca.pwdb.format;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.io.NonClosingInputStream;
import org.linguafranca.pwdb.io.NonClosingOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * This class implements KDBX formatted saving and loading of databases
 */
public class KdbxStreamFormat implements StreamFormat<KdbxHeader> {

    private KdbxHeader kdbxHeader;
    /**
     * Create a StreamFormat for reading or for writing v3 with default KdbxHeader
     */
    public KdbxStreamFormat() {
        this(new KdbxHeader(3));
    }

    /**
     * Specify a version for writing using default KdbxHeader
     *
     * @param version the version
     * @deprecated use {@link #KdbxStreamFormat(KdbxHeader)} with version set
     */
    @Deprecated
    public KdbxStreamFormat(Version version) {
        this(version.getVersionNum() == 3? new KdbxHeader(): new KdbxHeader(4));
    }

    /**
     * Provide a {@link KdbxHeader} which includes version info
     * @param kdbxHeader a {@link KdbxHeader} to specify (write) or (capture) read the file config
     */

    public KdbxStreamFormat(KdbxHeader kdbxHeader) {
        this.kdbxHeader = kdbxHeader;
    }

    /**
     * Read a database, leaving the stream open
     */
    @Override
    public void read(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException {
        // closing the decryption chain closes the stream under it, so shield the caller's stream
        try (InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, kdbxHeader,
                new NonClosingInputStream(encryptedInputStream))) {
            serializableDatabase.setEncryption(kdbxHeader.getInnerStreamEncryptor());
            serializableDatabase.read(decryptedInputStream);
            if (kdbxHeader.getVersion() == 3 && !Arrays.equals(serializableDatabase.getHeaderHash(), kdbxHeader.getHeaderHash())) {
                throw new IllegalStateException("Header hash does not match");
            }
            if (kdbxHeader.getVersion() == 4) {
                int count = 0;
                for (byte[] binary : kdbxHeader.getBinaries()) {
                    serializableDatabase.addBinary(count, Arrays.copyOfRange(binary, 1, binary.length));
                    count++;
                }
            }
        }
    }

    /**
     * Write a database, leaving the stream open
     */
    @Override
    public void write(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream outputStream) throws IOException {
        Helpers.isV4.set(kdbxHeader.getVersion() == 4);
        try {
            if (kdbxHeader.getVersion() == 4) {
                // the header may already hold binaries, from loading or an earlier save
                kdbxHeader.getBinaries().clear();
                for (int a = 0; a < serializableDatabase.getBinaryCount(); a++) {
                    int attachmentLength = serializableDatabase.getBinary(a).length;
                    byte[] binary = new byte[attachmentLength + 1];
                    binary[0] = 0;
                    System.arraycopy(serializableDatabase.getBinary(a),0, binary, 1, attachmentLength);
                    kdbxHeader.addBinary(binary);
                }
                // the serializer leaves the binaries out of the XML when Helpers.isV4 is set
            }

            // choose the version, and tell the database, so it can leave out what the version can't hold
            if (kdbxHeader.isMinorVersionAutomatic()) {
                kdbxHeader.chooseMinorVersion(serializableDatabase.getMinimumMinorVersion(kdbxHeader.getVersion()));
            }
            serializableDatabase.setFormatVersion(kdbxHeader.getVersion(), kdbxHeader.getMinorVersion());

            // the encryption chain must be closed to finish, and closing it closes the stream under it,
            // so shield the caller's stream
            try (OutputStream encryptedOutputStream = KdbxSerializer.createEncryptedOutputStream(credentials, kdbxHeader,
                    new NonClosingOutputStream(outputStream))) {
                if (kdbxHeader.getVersion() == 3) {
                    serializableDatabase.setHeaderHash(kdbxHeader.getHeaderHash());
                }
                serializableDatabase.setEncryption(kdbxHeader.getInnerStreamEncryptor());
                serializableDatabase.write(encryptedOutputStream);
                encryptedOutputStream.flush();
            }
        } finally {
            Helpers.isV4.set(false);
        }
    }

    /**
     * Load a database and close the stream
     *
     * @deprecated use {@link #read(SerializableDatabase, Credentials, InputStream)} (issue #109)
     */
    @Override
    @Deprecated
    public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException {
        try (encryptedInputStream) {
            read(serializableDatabase, credentials, encryptedInputStream);
        }
    }

    /**
     * Save a database and close the stream
     *
     * @deprecated use {@link #write(SerializableDatabase, Credentials, OutputStream)} (issue #109)
     */
    @Override
    @Deprecated
    public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream outputStream) throws IOException {
        try (outputStream) {
            write(serializableDatabase, credentials, outputStream);
        }
    }

    @Override
    public KdbxHeader getStreamConfiguration() {
        return kdbxHeader;
    }

    @Override
    public void setStreamConfiguration(KdbxHeader configuration) {
        this.kdbxHeader = configuration;
    }

    /**
     * KDBX file format specifier
     * @deprecated use constructor with {@link KdbxHeader#setVersion(int)}
     */
   @Deprecated
   public enum Version {
        KDBX31(3),
        KDBX4(4);

        private final int version;

        Version(int num) {
            this.version = num;
        }

        int getVersionNum() {
            return this.version;
        }
    }
}
