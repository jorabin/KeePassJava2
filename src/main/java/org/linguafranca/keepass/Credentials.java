package org.linguafranca.keepass;

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
        private byte [] password;

        public Password(String password) {
            this.password = password.getBytes();
        }
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
