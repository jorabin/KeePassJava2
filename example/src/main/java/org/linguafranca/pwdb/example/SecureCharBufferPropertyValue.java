package org.linguafranca.pwdb.example;

import io.github.novacrypto.SecureCharBuffer;
import org.linguafranca.pwdb.PropertyValue;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

/**
 * SecureCharBufferPropertyValue uses {@link SecureCharBuffer}
 */
public class SecureCharBufferPropertyValue implements PropertyValue {
    private final SecureCharBuffer value;

    /**
     * Builder for {@link SecureCharBufferPropertyValue}
     */
    static class Factory implements PropertyValue.Factory {

        @Override
        public PropertyValue of(CharSequence aString) {
            return new SecureCharBufferPropertyValue(aString);
        }

        @Override
        public PropertyValue of(char[] value) {
            return new SecureCharBufferPropertyValue(value);
        }

        @Override
        public PropertyValue of(byte[] value) {
            return new SecureCharBufferPropertyValue(value);
        }
    }

    public SecureCharBufferPropertyValue(CharSequence aString) {
        this.value = SecureCharBuffer.withCapacity(aString.length());
        this.value.append(aString);
    }

    public SecureCharBufferPropertyValue(char [] value) {
        this.value = SecureCharBuffer.withCapacity(value.length);
        this.value.append(CharBuffer.wrap(value));
    }

    public SecureCharBufferPropertyValue(byte [] value) {
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

