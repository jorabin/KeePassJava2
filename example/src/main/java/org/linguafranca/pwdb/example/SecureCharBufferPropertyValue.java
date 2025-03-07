/*
 * Copyright (c) 2025. Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
    static class Factory implements PropertyValue.Factory<SecureCharBufferPropertyValue> {

        @Override
        public SecureCharBufferPropertyValue of(CharSequence aString) {
            return new SecureCharBufferPropertyValue(aString);
        }

        @Override
        public SecureCharBufferPropertyValue of(char[] value) {
            return new SecureCharBufferPropertyValue(value);
        }

        @Override
        public SecureCharBufferPropertyValue of(byte[] value) {
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
        CharBuffer cb = CharBuffer.wrap(value);
        char[] result = new char[cb.limit()];
        cb.get(result);
        return result;
    }

    @Override
    public byte [] getValueAsBytes() {
        CharBuffer cb = CharBuffer.wrap(value);
        ByteBuffer bb = StandardCharsets.UTF_8.encode(cb);
        byte[] result = new byte[bb.limit()];
        bb.get(result);
        return result;
    }

    @Override
    public boolean isProtected() {
        return true;
    }
}

