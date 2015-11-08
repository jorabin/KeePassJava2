package org.linguafranca.keepass;

/**
 * @author jo
 */
public interface Credentials {

    class NoOp implements Credentials {
        @Override
        public byte[] getKey() {
            return new byte[0];
        }
    }

    byte [] getKey();
}
