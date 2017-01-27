package org.linguafranca.pwdb.keepasshttp;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Interface for an injectable password generator
 */
public interface PwGenerator {
    String generate();
    /**
     * Generator for simple hex password
     */
    class HexPwGenerator implements PwGenerator {
        private final int length;

        HexPwGenerator (int length) {
            this.length = length;
        }

        @Override
        public String generate() {
            String[] symbols = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
            Random random = new SecureRandom();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int indexRandom = random.nextInt( symbols.length );
                sb.append( symbols[indexRandom] );
            }
            return sb.toString();
        }
    }
}
