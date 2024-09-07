package org.linguafranca.pwdb;

import io.github.novacrypto.SecureCharBuffer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * An interface through which property values can be stored in memory to make it
 * harder to access their values via a heap dump etc.
 */
public interface PropertyValue {
    String getValueAsString();

    char [] getValueAsChars();

    byte [] getValueAsBytes();

    boolean isProtected();

    /**
     * A builder interface for PropertyValue
     */
    interface Factory {
        PropertyValue of (CharSequence aCharSequence);

        PropertyValue of (char [] value);

        PropertyValue of (byte [] value);
    }

    CharSequence getValue();

    /**
     * Unprotected value does not use String
     */

    class Default implements PropertyValue {

        private final CharBuffer value;

        static class Factory implements PropertyValue.Factory {

            @Override
            public PropertyValue of(CharSequence aCharSequence) {
                return new Default(aCharSequence);
            }

            @Override
            public PropertyValue of(char[] value) {
                return new Default(value);
            }

            @Override
            public PropertyValue of(byte[] value) {
                return new Default(value);
            }
        }

        public Default(CharSequence aString) {
            this.value = CharBuffer.wrap(aString);
        }

        public Default(char [] value) {
            this.value = CharBuffer.wrap(value);
        }

        public Default(byte [] value) {
            this.value = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(value));
        }

        @Override
        public String getValueAsString() {
            return this.value.toString();
        }

        @Override
        public CharSequence getValue() {
            return this.value;
        }

        @Override
        public char [] getValueAsChars() {
            return this.value.array();
        }

        @Override
        public byte [] getValueAsBytes() {
            return StandardCharsets.UTF_8.encode(value).array();
        }

        @Override
        public boolean isProtected() {
            return false;
        }
    }


    /**
     * Protected Value uses {@link SecureCharBuffer}
     */
    class Protected implements PropertyValue {
        private final SecureCharBuffer value;

        /**
         * Builder for {@link PropertyValue.Protected}
         */
        static class Factory implements PropertyValue.Factory {

            @Override
            public PropertyValue of(CharSequence aString) {
                return new Protected(aString);
            }

            @Override
            public PropertyValue of(char[] value) {
                return new Protected(value);
            }

            @Override
            public PropertyValue of(byte[] value) {
                return new Protected(value);
            }
        }

        public Protected(CharSequence aString) {
            this.value = SecureCharBuffer.withCapacity(aString.length());
            this.value.append(aString);
        }

        public Protected(char [] value) {
            this.value = SecureCharBuffer.withCapacity(value.length);
            this.value.append(CharBuffer.wrap(value));
        }

        public Protected(byte [] value) {
            this.value = SecureCharBuffer.withCapacity(value.length);
            this.value.append(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(value)));
        }
        @Override
        public String getValueAsString() {
            return this.value.toStringAble().toString();
        }

        @Override
        public CharSequence getValue() {
            return this.value;
        }

        @Override
        public char [] getValueAsChars() {
            return CharBuffer.wrap(value).array();
        }

        @Override
        public byte [] getValueAsBytes() {
            return StandardCharsets.UTF_8.encode(CharBuffer.wrap(value)).array();
        }

        @Override
        public boolean isProtected() {
            return true;
        }
    }
}
