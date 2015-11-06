package org.linguafranca.keepass.kdbx;

import org.linguafranca.keepass.db.Credentials;
import org.linguafranca.keepass.db.DatabaseProvider;
import org.linguafranca.keepass.db.Formatter;
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
        String pass = new String(credentials.getPassword(), "UTF-8");
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(pass, kdbxHeader, encryptedInputStream);
        databaseProvider.setEncryption(new Salsa20Encryption(kdbxHeader.getProtectedStreamKey()));
        databaseProvider.load(decryptedInputStream);
        decryptedInputStream.close();
    }

    @Override
    public void save(DatabaseProvider databaseProvider, Credentials credentials, OutputStream encryptedOutputStream) throws IOException {
        String pass = new String(credentials.getPassword(), "UTF-8");
        // fresh kdbx header
        KdbxHeader kdbxHeader = new KdbxHeader();
        // to avoid re-encrypting all memory protected fields we need to keep the same key
        kdbxHeader.setProtectedStreamKey(databaseProvider.getEncryption().getKey());
        OutputStream unencrytedOutputStream = KdbxSerializer.createEncryptedOutputStream(pass, kdbxHeader, encryptedOutputStream);
        databaseProvider.setHeaderHash(kdbxHeader.getMessageDigest());
        databaseProvider.save(unencrytedOutputStream);
        unencrytedOutputStream.flush();
        unencrytedOutputStream.close();
    }
}
