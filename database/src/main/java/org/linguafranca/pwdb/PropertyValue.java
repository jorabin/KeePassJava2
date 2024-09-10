package org.linguafranca.pwdb;

import org.apache.commons.lang3.CharSequenceUtils;

import javax.crypto.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
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
                return CharsStore.getFactory();
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
     * Property values are stored as char arrays.
     */
    class CharsStore implements PropertyValue, Serializable {

        private final char[] value;

        private final static PropertyValue.Factory<CharsStore> factory =
                new PropertyValue.Factory<CharsStore>(){

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
                        return new CharsStore((value));
                    }
                };

        public static PropertyValue.Factory<CharsStore> getFactory() {
            return factory;
        }
        public CharsStore(CharSequence aString) {
            this.value = CharSequenceUtils.toCharArray(aString);
        }

        public CharsStore(char [] value) {
            this.value = value;
        }

        public CharsStore(byte [] value) {
            ByteBuffer bb = ByteBuffer.wrap(value);
            CharBuffer cb = StandardCharsets.UTF_8.decode(bb);
            char[] chars = new char[cb.limit()];
            cb.get(chars);
            this.value = chars;
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
            CharBuffer cb = CharBuffer.wrap(this.value);
            ByteBuffer bb = StandardCharsets.UTF_8.encode(cb);
            byte[] result = new byte[bb.limit()];
            bb.get(result);
            return result;
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
        static KeyGenerator keyGenerator;
        private final SealedObject sealedObject;
        private final ByteBuffer buffer;

        static {
            try {
                keyGenerator= KeyGenerator.getInstance("AES");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private final static PropertyValue.Factory<SealedStore> factory =
                new PropertyValue.Factory<SealedStore>(){

                    @Override
                    public SealedStore of(CharSequence aCharSequence) {
                        return new SealedStore(new CharsStore(aCharSequence));
                    }

                    @Override
                    public SealedStore of(char[] value) {
                        return new SealedStore(new CharsStore(value));
                    }

                    @Override
                    public SealedStore of(byte[] value) {
                        return new SealedStore(new CharsStore(value));
                    }
                };

        public static PropertyValue.Factory<SealedStore> getFactory() {
            return factory;
        }

        public SealedStore(CharsStore object){
            try {
                Key key = keyGenerator.generateKey();
                buffer = storeKey(key);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                sealedObject = new SealedObject(object, cipher);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private ByteBuffer storeKey(Key key) throws IOException {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(key);
                ByteBuffer b = ByteBuffer.allocateDirect(baos.size());
                b.put(baos.toByteArray());
                return b;
            }
        }

        private Key retrieveKey(ByteBuffer buffer) {
            byte [] bytes = new byte[buffer.position()];
            buffer.rewind();
            buffer.get(bytes);
            try {
                try (
                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bais)) {
                    return (Key) ois.readObject();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public CharsStore getAsCharsStore() {
            try {
                Key key = retrieveKey(buffer);
                return ((CharsStore) sealedObject.getObject(key));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public String getValueAsString() {
            return getAsCharsStore().getValueAsString();
        }

        @Override
        public char[] getValueAsChars() {
            return getAsCharsStore().getValueAsChars();
        }

        @Override
        public byte[] getValueAsBytes() {
            return getAsCharsStore().getValueAsBytes();
        }

        @Override
        public CharSequence getValue() {
            return getAsCharsStore().getValue();
        }

        @Override
        public boolean isProtected() {
            return true;
        }
    }
}
