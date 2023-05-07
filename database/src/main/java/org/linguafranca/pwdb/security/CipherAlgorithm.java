package org.linguafranca.pwdb.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Interface defining an algorithm for encrypting and decrypting database contents
 */
public interface CipherAlgorithm {
    /**
     * Returns the UUID of this algorithm
     */
    UUID getCipherUuid();

    /**
     * Return the name of this algorithm
     */
    String getName();

    /**
     * Create a decrypted stream from the supplied encrypted one
     *
     * @param encryptedInputStream an encrypted stream
     * @param key                  the decryption key
     * @param iv                   the iv
     * @return an unencrypted stream
     */
    InputStream getDecryptedInputStream(InputStream encryptedInputStream, byte[] key, byte[] iv);

    /**
     * Create an encrypted stream  from the supplied unencrypted one
     *
     * @param decryptedOutputStream an unencrypted stream
     * @param key                   a key
     * @param iv                    an iv
     * @return an encrypted stream
     */
    OutputStream getEncryptedOutputStream(OutputStream decryptedOutputStream, byte[] key, byte[] iv);

}
