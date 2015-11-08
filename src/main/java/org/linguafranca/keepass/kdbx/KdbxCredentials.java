package org.linguafranca.keepass.kdbx;

import org.linguafranca.keepass.encryption.Encryption;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * @author jo
 */
public class KdbxCredentials {

    public static class KeyFile implements org.linguafranca.keepass.Credentials {

        private final byte[] key;

        public KeyFile(byte[] password, InputStream inputStream) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            byte[] pwKey = md.digest(password);

            byte[] keyFileData = KdbxKeyFile.load(inputStream);
            if (keyFileData == null) {
                throw new IllegalStateException("Could not read key file");
            }
            md.update(pwKey);
            this.key = md.digest(keyFileData);
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }

    public static class Password implements org.linguafranca.keepass.Credentials {

        private final byte[] key;

        public Password(byte[] password) {
            MessageDigest md = Encryption.getMessageDigestInstance();
            byte[] digest = md.digest(password);
            key = md.digest(digest);
        }

        @Override
        public byte[] getKey() {
            return key;
        }
    }

}
