package org.linguafranca.keepass.kdb;

import org.linguafranca.keepass.Credentials;
import org.linguafranca.keepass.encryption.Encryption;

import java.security.MessageDigest;

/**
 * @author jo
 */
public class KdbCredentials {

    public static class Password implements Credentials {
        private byte [] key;

        public Password(String password) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            this.key = md.digest(password.getBytes());
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }
}
