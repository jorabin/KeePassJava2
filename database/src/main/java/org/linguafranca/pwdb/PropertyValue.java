package org.linguafranca.pwdb;

import com.google.common.base.Charsets;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An interface through which (textual) property values can be stored in memory as something other than String
 * and using various techniques for obfuscating the value and to make it
 * harder to access the values via a heap dump etc.
 */
public interface PropertyValue {
    CharSequence getValue();

    char [] getValueAsChars();

    byte [] getValueAsBytes();

    boolean isProtected();

    String getValueAsString();

    static byte[] charsToBytes(char[] value) {
        CharBuffer cb = CharBuffer.wrap(value);
        ByteBuffer bb = StandardCharsets.UTF_8.encode(cb);
        byte[] result = new byte[bb.limit()];
        bb.get(result);
        return result;
    }

    static char[] bytesToChars(byte[] value) {
        ByteBuffer bb = ByteBuffer.wrap(value);
        CharBuffer cb = Charsets.UTF_8.decode(bb);
        char[] chars = new char[cb.limit()];
        cb.get(chars);
        return chars;
    }

    static byte[] charSequenceToBytes(CharSequence charSequence){
        CharBuffer cb = CharBuffer.wrap(charSequence);
        ByteBuffer bb = StandardCharsets.UTF_8.encode(cb);
        byte[] result = new byte[bb.limit()];
        bb.get(result);
        return result;
    }

    static CharSequence bytesToCharSequence(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return StandardCharsets.UTF_8.decode(bb);
    }

    /**
     * A factory interface for PropertyValue.
     */
    interface  Factory<P extends PropertyValue> {
        P of (CharSequence aCharSequence);

        P of (char [] value);

        P of (byte [] value);
    }

    /**
     * A specification of which factories are to be used for unprotected values as opposed to protected values.
     */
    interface Strategy {
        /**
         * A list of the properties that should be protected
         */
        List<String> getProtectedProperties();

        /**
         * A factory for protected properties
         */
        PropertyValue.Factory<? extends PropertyValue> newProtected();

        /**
         * A factory for unprotected property values
         */
        PropertyValue.Factory<? extends PropertyValue> newUnprotected();

        /**
         * Return a factory given a property name and the properties that should be protected
         */
        default PropertyValue.Factory<? extends PropertyValue> getFactoryFor(String propertyName) {
            return getProtectedProperties().contains(propertyName) ?
                    newProtected() :
                    newUnprotected();
        }

        class Default implements Strategy {

            @Override
            public List<String> getProtectedProperties() {
                //noinspection ArraysAsListWithZeroOrOneArgument
                return new ArrayList<>(Arrays.asList(Entry.STANDARD_PROPERTY_NAME_PASSWORD));
            }

            @Override
            public Factory<? extends PropertyValue> newProtected() {
                return SealedStore.getFactory();
            }

            @Override
            public Factory<? extends PropertyValue> newUnprotected() {
                return BytesStore.getFactory();
            }
        }
    }

    /**
     * Values are stored as strings.
     */
    class StringStore implements PropertyValue {
        private final String value;
        private final static PropertyValue.Factory<StringStore> factory =
                new PropertyValue.Factory<StringStore>(){

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
                        return new StringStore((value));
                    }
                };
        public static PropertyValue.Factory<StringStore> getFactory() {
            return factory;
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
     * Property values are stored as byte arrays.
     */
    class BytesStore implements PropertyValue, Serializable {

        private final byte[] value;

        private final static PropertyValue.Factory<BytesStore> factory =
                new PropertyValue.Factory<BytesStore>(){

                    @Override
                    public BytesStore of(CharSequence aCharSequence) {
                        return new BytesStore(aCharSequence);
                    }

                    @Override
                    public BytesStore of(char[] value) {
                        return new BytesStore(value);
                    }

                    @Override
                    public BytesStore of(byte[] value) {
                        return new BytesStore((value));
                    }
                };

        public static PropertyValue.Factory<BytesStore> getFactory() {
            return factory;
        }
        public BytesStore(CharSequence aString) {
            this.value = charSequenceToBytes(aString);
        }

        public BytesStore(char [] value) {
            this.value = charsToBytes(value);
        }

        public BytesStore(byte [] value) {
            this.value = Arrays.copyOf(value, value.length);
        }

        @Override
        public String getValueAsString() {
            return new String(this.value);
        }

        @Override
        public CharSequence getValue() {
            ByteBuffer bb = ByteBuffer.wrap(this.value);
            return Charsets.UTF_8.decode(bb);
        }

        @Override
        public char [] getValueAsChars() {
            return bytesToChars(this.value);
        }

        @Override
        public byte [] getValueAsBytes() {
            return Arrays.copyOf(this.value, this.value.length);
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
     * The Key for the encrypted value is stored off-heap.
     * <p>
     * The overhead of using this class for encryption of the value then serialization of the key
     * for storage off-heap may be significant.
     */
    class SealedStore implements PropertyValue {
        static SecureRandom secureRandom = new SecureRandom();
        private final SealedObject sealedObject;
        private final ByteBuffer buffer;

        private ByteBuffer storeKey(SecretKey key) throws IOException {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(key);
                ByteBuffer b = ByteBuffer.allocateDirect(baos.size());
                b.put(baos.toByteArray());
                return b;
            }
        }

        private SecretKey retrieveKey(ByteBuffer buffer) {
            byte [] bytes = new byte[buffer.position()];
            buffer.rewind();
            buffer.get(bytes);
            try {
                try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {
                    return (SecretKey) ois.readObject();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private byte[] getBytes() {
            try {
                SecretKey key = retrieveKey(buffer);
                byte[] cs = ((byte[]) sealedObject.getObject(key));
                key.destroy();
                return cs;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private final static PropertyValue.Factory<SealedStore> factory =
                new PropertyValue.Factory<SealedStore>(){

                    @Override
                    public SealedStore of(CharSequence aCharSequence) {
                        return new SealedStore(aCharSequence);
                    }

                    @Override
                    public SealedStore of(char[] value) {
                        return new SealedStore(value);
                    }

                    @Override
                    public SealedStore of(byte[] value) {
                        return new SealedStore(value);
                    }
                };

        public static PropertyValue.Factory<SealedStore> getFactory() {
            return factory;
        }

        /**
         * Believe it or not ... a SecretKey generated by the standard KeyGenerator throws an
         * exception when the destroy() method is called, so we roll our own
         */
        private static class AESKey implements SecretKey, Serializable {
            private boolean destroyed = false;
            private final byte[] key = new byte[16];
            final String algorithm = "AES";

            {
                secureRandom.nextBytes(key);
            }

            @Override
            public void destroy() {
                Arrays.fill(this.key, (byte) 0);
                this.destroyed = true;
            }

            @Override
            public boolean isDestroyed() {
                return this.destroyed;
            }

            @Override
            public String getAlgorithm() {
                return this.algorithm;
            }

            @Override
            public String getFormat() {
                return "RAW";
            }

            @Override
            public byte[] getEncoded() {
                return this.key.clone();
            }
        }

        public SealedStore(byte [] bytes){
            try {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                SecretKey key = new AESKey();
                cipher.init(Cipher.ENCRYPT_MODE, key);
                buffer = storeKey(key);
                key.destroy();
                sealedObject = new SealedObject(bytes, cipher);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public SealedStore(char [] chars) {
            this(charsToBytes(chars));
        }

        public SealedStore(CharSequence charSequence) {
            this(charSequenceToBytes(charSequence));
        }

         @Override
        public String getValueAsString() {
            return new String(getBytes());
        }

        @Override
        public char[] getValueAsChars() {
            return bytesToChars(getBytes());
        }

        @Override
        public byte[] getValueAsBytes() {
            byte [] result = getBytes();
            return Arrays.copyOf(result, result.length);
        }

        @Override
        public CharSequence getValue() {
            return bytesToCharSequence(getBytes());
        }

        @Override
        public boolean isProtected() {
            return true;
        }
    }
}
