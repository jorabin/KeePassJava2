package org.linguafranca.security;

/**
 * Supports a contract that yields a key for decryption of databases
 *
 * @author jo
 */
public interface Credentials {

    /**
     * Implementation of no credentials
     */
    class None implements Credentials {
        @Override
        public byte[] getKey() {
            return new byte[0];
        }
    }

    byte [] getKey();
}
