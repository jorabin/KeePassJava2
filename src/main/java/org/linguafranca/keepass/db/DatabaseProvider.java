package org.linguafranca.keepass.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * @author jo
 */
public interface DatabaseProvider {

    Encryption getEncryption();

    interface Encryption {
        byte[] getKey();
        byte[] decrypt(byte[] encryptedText);
        byte[] encrypt(byte[] decryptedText);
    }

    DatabaseProvider load(InputStream inputStream) throws IOException;

    void save(OutputStream outputStream) throws IOException;

    void setHeaderHash(MessageDigest messageDigest);

    void setEncryption(Encryption encryption);
}
