package org.linguafranca.pwdb.security;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of a storage for KDBX Header field parameters
 *
 * @author jo
 */

@SuppressWarnings("WeakerAccess")
public class VariantDictionary {

    private short version;
    private Map<String, Entry> entries = new HashMap<>();

    @SuppressWarnings("unused")
    public static class Types {
        public static final byte UINT32 = 0x4;
        public static final byte UINT64 = 0x5;
        public static final byte BOOL = 0x8;
        public static final byte INT32 = 0xC;
        public static final byte INT64 = 0xD;
        public static final byte STRING = 0x18; // UTF-8, without BOM, without null terminator
        public static final byte ARRRAY = 0x42;
    }

    @SuppressWarnings("WeakerAccess")
    public static class Entry {
        private byte type;
        private byte [] value;

        public Entry(byte type, byte [] value) {
            this.type = type;
            this.value = value;
        }

        public byte getType() {
            return type;
        }

        public UUID asUuid() {
            if (value.length == 16) {
                ByteBuffer b = ByteBuffer.wrap(value);
                return new UUID(b.getLong(), b.getLong(8));
            }
            throw new IllegalStateException("Cannot convert value to UUID");
        }

        public long asLong() {
            if (value.length != 8){
                throw new IllegalStateException("Cannot convert value to long");
            }
            return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getLong();
        }

        public int asInteger() {
            if (value.length != 4){
                throw new IllegalStateException("Cannot convert value to int");
            }
            return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getInt();
        }

        public byte [] asByteArray() {
            return value;
        }
    }

    public VariantDictionary(short version) {
        this.version = version;
    }
    public short getVersion() {
        return version;
    }

    public @Nullable Entry get(String key) {
        return entries.get(key);
    }

    public void put(String key, byte type, byte [] value) {
        entries.put(key, new Entry(type, value));
    }
}
