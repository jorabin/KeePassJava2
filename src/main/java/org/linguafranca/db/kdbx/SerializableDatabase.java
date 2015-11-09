package org.linguafranca.db.kdbx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * This interface allows for serialization and deserialization of databases.
 *
 * <p>Databases instantiate themselves from a stream and serialize to a stream,
 * and need to be able to encrypt and decrypt data (e.g. Protected fields in KDBX format).
 *
 * <p>KDBX databases contain a header hash (i.e. a hash of the contents of
 * some portion of the {@link StreamFormat} they have been loaded from or saved to.
 * Which means that databases must support the setting of this value after the header
 * has been written on save, and reading after to load to allow for integrity checking.
 *
 * @author jo
 */
public interface SerializableDatabase {

    interface Encryption {
        byte[] getKey();

        byte[] decrypt(byte[] encryptedText);

        byte[] encrypt(byte[] decryptedText);
    }

    SerializableDatabase load(InputStream inputStream) throws IOException;

    void save(OutputStream outputStream) throws IOException;

    Encryption getEncryption();

    void setEncryption(Encryption encryption);

    byte[] getHeaderHash();

    void setHeaderHash(byte[] hash);
}
