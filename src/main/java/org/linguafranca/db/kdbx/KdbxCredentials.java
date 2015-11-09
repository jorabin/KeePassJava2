package org.linguafranca.db.kdbx;

import org.linguafranca.security.Encryption;
import org.linguafranca.security.Credentials;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * The class holds subclasses of {@link Credentials} appropriate to KDBX files.
 *
 * @author jo
 */
public class KdbxCredentials {

    /**
     * Class for KDBX key file with password credentials
     */
    public static class KeyFile implements Credentials {

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

    /**
     * Class for KDBX password only credentials
     */
    public static class Password implements Credentials {

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
