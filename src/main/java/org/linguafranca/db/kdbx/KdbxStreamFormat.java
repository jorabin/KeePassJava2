package org.linguafranca.db.kdbx;

import org.linguafranca.security.Credentials;

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
        serializableDatabase.setEncryption(new Salsa20Encryption(kdbxHeader.getProtectedStreamKey()));
        serializableDatabase.load(decryptedInputStream);
        decryptedInputStream.close();
    }

    @Override
    public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) throws IOException {
        // fresh kdbx header
        KdbxHeader kdbxHeader = new KdbxHeader();
        OutputStream unencrytedOutputStream = KdbxSerializer.createEncryptedOutputStream(credentials, kdbxHeader, encryptedOutputStream);
        serializableDatabase.setHeaderHash(kdbxHeader.getHeaderHash());
        serializableDatabase.setEncryption(new Salsa20Encryption(kdbxHeader.getProtectedStreamKey()));
        serializableDatabase.save(unencrytedOutputStream);
        unencrytedOutputStream.flush();
        unencrytedOutputStream.close();
    }
}
