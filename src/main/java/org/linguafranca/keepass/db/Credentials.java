package org.linguafranca.keepass.db;

/**
 * @author jo
 */
public interface Credentials {
    class NoOp implements Credentials {

        @Override
        public byte[] getPassword() {
            return new byte[0];
        }

        @Override
        public byte[] getKey() {
            return new byte[0];
        }
    }

    class Password implements Credentials {

        public Password(String password) {
            this.password = password.getBytes();
        }
        byte [] password;
        @Override
        public byte[] getPassword() {
            return password;
        }

        @Override
        public byte[] getKey() {
            return new byte[0];
        }
    }
    byte [] getPassword();
    byte [] getKey();
}
