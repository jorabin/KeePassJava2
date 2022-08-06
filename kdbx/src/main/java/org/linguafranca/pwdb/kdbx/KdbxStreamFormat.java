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
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * This class implements KDBX formatted saving and loading of databases
 *
 */
public class KdbxStreamFormat implements StreamFormat {

    private final Version version;

    public enum Version {
    	//maybe change these to more accurately reflect version number
    	//31=3.1, 40=4, 41=4.1, etc.
        KDBX31(3),
        KDBX4(4);

        private final int version;

        Version(int num) {
            ersion = num;
        }

        int getVersionNum() {
            return version;
        }
    }

    /**
     * Create a StreamFormat for reading or for writing KDBX4
     */
    public KdbxStreamFormat() {
        version = Version.KDBX4;
    }

    /**
     * Create a StreamFormat for reading or writing a KDBX database of version
     * @param version the version of the KDBX database
     */
    public KdbxStreamFormat(Version version) {
        this.version = version;
    }

    @Override
    public int getStreamFormatVersion() {
    	return version.getVersionNum();
    }
    
    @Override
    public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException {
        KdbxHeader kdbxHeader = new KdbxHeader(version.getVersionNum());
        
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
    /**
     * Creates a default KDBX header based on StreamFormat version
     * and saves to the corresponding formatted output stream
     */
    public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream unencryptedOutputStream) throws IOException {
        // fresh default KDBX3.1/4.x header
        KdbxHeader kdbxHeader = new KdbxHeader(version.getVersionNum());
        OutputStream kdbxOutputStream = null;
        try {
			kdbxOutputStream = KdbxSerializer.createKdbxFormattedOutputStream(credentials, kdbxHeader, unencryptedOutputStream);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}

        if (version == Version.KDBX31) {
            serializableDatabase.setHeaderHash(kdbxHeader.getHeaderHash());
        }
        
        serializableDatabase.setEncryption(kdbxHeader.getStreamEncryptor());
        serializableDatabase.save(kdbxOutputStream);
        
        kdbxOutputStream.flush();
        kdbxOutputStream.close();
    }
    
    /**
     * Uses a user-defined KDBX header to appropriately format an output stream
     * for writing the database to.
     * @param customKdbxHeader
     * @param serializableDatabase
     * @param credentials
     * @param unencryptedOutputStream
     * @throws IOException
     */
    public void save(KdbxHeader customKdbxHeader, SerializableDatabase serializableDatabase, Credentials credentials, OutputStream unencryptedOutputStream) throws IOException {
        OutputStream kdbxOutputStream = null;
        try {
			kdbxOutputStream = KdbxSerializer.createKdbxFormattedOutputStream(credentials, customKdbxHeader, unencryptedOutputStream);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}

        if (version == Version.KDBX31) {
            serializableDatabase.setHeaderHash(customKdbxHeader.getHeaderHash());
        }
        
        serializableDatabase.setEncryption(customKdbxHeader.getStreamEncryptor());
        serializableDatabase.save(kdbxOutputStream);
        
        kdbxOutputStream.flush();
        kdbxOutputStream.close();
    	
    }
}
