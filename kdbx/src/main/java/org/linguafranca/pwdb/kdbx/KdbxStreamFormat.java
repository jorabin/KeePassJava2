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

package org.linguafranca.pwdb.kdbx;

import org.linguafranca.pwdb.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * This class implements KDBX formatted saving and loading of databases
 *
 */
public class KdbxStreamFormat implements StreamFormat {

    private final Version version;

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

    /**
     * Create a StreamFormat for reading or for writing v3
     */
    public KdbxStreamFormat() {
        this.version = Version.KDBX31;
    }

    /**
     * Specify a version for writing
     * @param version the version
     */
    public KdbxStreamFormat(Version version) {
        this.version = version;
    }

    @Override
    public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException {
        KdbxHeader kdbxHeader = new KdbxHeader();
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, kdbxHeader, encryptedInputStream);
        serializableDatabase.setEncryption(kdbxHeader.getStreamEncryptor());
        serializableDatabase.load(decryptedInputStream);
        if (kdbxHeader.getVersion() == 3 && !Arrays.equals(serializableDatabase.getHeaderHash(), kdbxHeader.getHeaderHash())) {
            throw new IllegalStateException("Header hash does not match");
        }
        if (kdbxHeader.getVersion() == 4) {
            int count = 0;
            for (byte[] binary: kdbxHeader.getBinaries()) {
                serializableDatabase.addBinary(count, Arrays.copyOfRange(binary,1, binary.length));
                count++;
            }
        }
        decryptedInputStream.close();
    }

    @Override
    public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) throws IOException {
        // fresh kdbx header
        KdbxHeader kdbxHeader = new KdbxHeader(version.getVersionNum());
        OutputStream unencrytedOutputStream = KdbxSerializer.createEncryptedOutputStream(credentials, kdbxHeader, encryptedOutputStream);
        if (version == Version.KDBX31) {
            serializableDatabase.setHeaderHash(kdbxHeader.getHeaderHash());
        }
        serializableDatabase.setEncryption(kdbxHeader.getStreamEncryptor());
        serializableDatabase.save(unencrytedOutputStream);
        unencrytedOutputStream.flush();
        unencrytedOutputStream.close();
    }
}
