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

package org.linguafranca.pwdb.kdbx.stream_3_1;

import org.linguafranca.pwdb.kdbx.SerializableDatabase;
import org.linguafranca.pwdb.kdbx.StreamFormat;
import org.linguafranca.pwdb.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class implements KDBX formatted saving and loading of databases
 *
 * @author jo
 */
public class KdbxStreamFormat implements StreamFormat {

    @Override
    public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException {
        KdbxHeader kdbxHeader = new KdbxHeader();
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, kdbxHeader, encryptedInputStream);
        serializableDatabase.setEncryption(new Salsa20StreamEncryptor(kdbxHeader.getProtectedStreamKey()));
        serializableDatabase.load(decryptedInputStream);
        decryptedInputStream.close();
    }

    @Override
    public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) throws IOException {
        // fresh kdbx header
        KdbxHeader kdbxHeader = new KdbxHeader();
        OutputStream unencrytedOutputStream = KdbxSerializer.createEncryptedOutputStream(credentials, kdbxHeader, encryptedOutputStream);
        serializableDatabase.setHeaderHash(kdbxHeader.getHeaderHash());
        serializableDatabase.setEncryption(new Salsa20StreamEncryptor(kdbxHeader.getProtectedStreamKey()));
        serializableDatabase.save(unencrytedOutputStream);
        unencrytedOutputStream.flush();
        unencrytedOutputStream.close();
    }
}
