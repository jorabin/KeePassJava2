package org.linguafranca.db.kdb;

import org.linguafranca.security.Credentials;
import org.linguafranca.security.Encryption;

import java.security.MessageDigest;

/**
 * Has inner classes representing credentials appropriate to KDB files
 *
 * @author jo
 */
public class KdbCredentials {

    /**
     * Password only credentials
     */
    public static class Password implements Credentials {
        private byte [] key;

        public Password(byte[] password) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            this.key = md.digest(password);
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }
}
