package org.linguafranca.pwdb;

import org.apache.commons.lang3.CharSequenceUtils;

import javax.crypto.*;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * An interface through which property values can be stored in memory to make it
 * harder to access their values via a heap dump etc.
 */
public interface PropertyValue {
    String getValueAsString();

    char [] getValueAsChars();

    byte [] getValueAsBytes();

    CharSequence getValue();

    boolean isProtected();

    /**
     * A builder interface for PropertyValue
     */
    interface Factory {
        PropertyValue of (CharSequence aCharSequence);

        PropertyValue of (char [] value);

        PropertyValue of (byte [] value);
    }

    interface Strategy {
        PropertyValue.Factory getUnprotectectedValueFactory ();
        PropertyValue.Factory getProtectectedValueFactory ();
    }

    class StringStore implements PropertyValue {
        private final String value;
        static class Factory implements PropertyValue.Factory {

            @Override
            public StringStore of(CharSequence aCharSequence) {
                return new StringStore(aCharSequence);
            }

            @Override
            public StringStore of(char[] value) {
                return new StringStore(value);
            }

            @Override
            public StringStore of(byte[] value) {
                return new StringStore(value);
            }
        }

        public StringStore(CharSequence aCharSequence){
            this.value = String.valueOf(aCharSequence);
        }
        public StringStore(char[] value){
            this.value = String.valueOf(value);
        }
        public StringStore(byte[] value){
            this.value = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(value)).toString();
        }

        @Override
        public String getValueAsString() {
            return value;
        }

        @Override
        public char[] getValueAsChars() {
            return value.toCharArray();
        }

        @Override
        public byte[] getValueAsBytes() {
            return value.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public CharSequence getValue() {
            return value;
        }

        @Override
        public boolean isProtected() {
            return false;
        }
    }
    /**
     * Unprotected value does not use String
     */
    class CharsStore implements PropertyValue, Serializable {

        private final char[] value;

        static class Factory implements PropertyValue.Factory {

            @Override
            public CharsStore of(CharSequence aCharSequence) {
                return new CharsStore(aCharSequence);
            }

            @Override
            public CharsStore of(char[] value) {
                return new CharsStore(value);
            }

            @Override
            public CharsStore of(byte[] value) {
                return new CharsStore(value);
            }
        }

        public CharsStore(CharSequence aString) {
            this.value = CharSequenceUtils.toCharArray(aString);
        }

        public CharsStore(char [] value) {
            this.value = value;
        }

        public CharsStore(byte [] value) {
            this.value = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(value)).array();
        }

        @Override
        public String getValueAsString() {
            return CharBuffer.wrap(this.value).toString();
        }

        @Override
        public CharSequence getValue() {
            return CharBuffer.wrap(this.value);
        }

        @Override
        public char [] getValueAsChars() {
            return this.value;
        }

        @Override
        public byte [] getValueAsBytes() {
            return StandardCharsets.UTF_8.encode(CharBuffer.wrap(this.value)).array();
        }

        @Override
        public boolean isProtected() {
            return false;
        }
    }

    /**
     * Encrypted property value storage intended for storing passwords in something other than
     * plaintext using {@link javax.crypto.SealedObject} class.
     * <p>
     * Since the key and the sealed object are stored together this is a bit of a vulnerability.
     */
    class SealedStore implements PropertyValue {
        static KeyGenerator keyGenerator;
        private final SealedObject sealedObject;

        ByteBuffer buffer;
        private final Key key;

        static {
            try {
                keyGenerator= KeyGenerator.getInstance("AES");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public SealedStore(CharsStore object){
            try {
                key = keyGenerator.generateKey();
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                sealedObject = new SealedObject(object, cipher);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        private CharsStore getAsDefault() {
            try {
                return ((CharsStore) sealedObject.getObject(key));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public String getValueAsString() {
            return getAsDefault().getValueAsString();
        }

        @Override
        public char[] getValueAsChars() {
            return getAsDefault().getValueAsChars();
        }

        @Override
        public byte[] getValueAsBytes() {
            return getAsDefault().getValueAsBytes();
        }

        @Override
        public CharSequence getValue() {
            return getAsDefault().getValue();
        }

        @Override
        public boolean isProtected() {
            return true;
        }
    }
}
