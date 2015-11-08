package org.linguafranca.keepass.kdbx;

import org.linguafranca.keepass.Credentials;
import org.linguafranca.keepass.DatabaseProvider;
import org.linguafranca.keepass.Formatter;
import org.linguafranca.keepass.encryption.Salsa20Encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jo
 */
public class KdbxFormatter implements Formatter {

    @Override
    public void load(DatabaseProvider databaseProvider, Credentials credentials, InputStream encryptedInputStream) throws IOException {
        KdbxHeader kdbxHeader = new KdbxHeader();
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, kdbxHeader, encryptedInputStream);
        databaseProvider.setEncryption(new Salsa20Encryption(kdbxHeader.getProtectedStreamKey()));
        databaseProvider.load(decryptedInputStream);
        decryptedInputStream.close();
    }

    @Override
    public void save(DatabaseProvider databaseProvider, Credentials credentials, OutputStream encryptedOutputStream) throws IOException {
        // fresh kdbx header
        KdbxHeader kdbxHeader = new KdbxHeader();
        OutputStream unencrytedOutputStream = KdbxSerializer.createEncryptedOutputStream(credentials, kdbxHeader, encryptedOutputStream);
        databaseProvider.setHeaderHash(kdbxHeader.getMessageDigest());
        databaseProvider.setEncryption(new Salsa20Encryption(kdbxHeader.getProtectedStreamKey()));
        databaseProvider.save(unencrytedOutputStream);
        unencrytedOutputStream.flush();
        unencrytedOutputStream.close();
    }
}
