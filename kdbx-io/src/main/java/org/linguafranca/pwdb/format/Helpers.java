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

package org.linguafranca.pwdb.format;

import com.google.common.io.ByteStreams;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * The class provides helpers to marshal and unmarshal values of KDBX files
 */
public class Helpers {
    /**
     *  Oftentimes we have no way of communicating which version we are using, say in an adapter that
     *  is buried deep in the internals of JAXB marshalling
     */
    public static ThreadLocal<Boolean> isV4 = ThreadLocal.withInitial(() -> false);

    public static String base64FromUuid(UUID uuid) {
        byte[] buffer = new byte[16];
        ByteBuffer b = ByteBuffer.wrap(buffer);
        b.putLong(uuid.getMostSignificantBits());
        b.putLong(8, uuid.getLeastSignificantBits());
        // round the houses for Android
        return new String(Base64.encodeBase64(buffer));
    }

    public static String hexStringFromUuid(UUID uuid) {
        byte[] buffer = new byte[16];
        ByteBuffer b = ByteBuffer.wrap(buffer);
        b.putLong(uuid.getMostSignificantBits());
        b.putLong(8, uuid.getLeastSignificantBits());
        // round the houses for Android
        return new String(Hex.encodeHex(buffer));
    }

    public static String hexStringFromBase64(String base64) {
        // round the houses for Android
        byte[] buffer = Base64.decodeBase64(base64.getBytes());
        return new String(Hex.encodeHex(buffer));
    }

    public static UUID uuidFromBase64(String base64) {
        // round the houses for Android
        byte[] buffer = Base64.decodeBase64(base64.getBytes());
        ByteBuffer b = ByteBuffer.wrap(buffer);
        return new UUID(b.getLong(), b.getLong(8));
    }

    /* --- Boolean ---
        Booleans are deliberately tri-valued true, false and null
     */
    public static Boolean toBoolean(String value) {
        if (value.equalsIgnoreCase("null")) {
            return null;
        }
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equals("1");
    }

    public static String fromBoolean(Boolean value) {
        return value == null ? "null" : (value ? "True" : "False");
    }

    /* --- Dates
    V3 dates are serialised as ISO8601 using Zulu (Z) TZD - but will deserialize more leniently
    V4 dates are base64 encoded seconds since midnight 0001-01-01
    --- */

    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz");
    /** The base of V4 times, 0001-01-01T00:00:00Z */
    public static final Instant baseInstant = ZonedDateTime.parse("0001-01-01T00:00:00Z", dateTimeFormatter).toInstant();

    /**
     * Parses a time, in V3 an ISO 8601 date and time, in V4 a base64 encoded count of seconds after {@link #baseInstant}
     * @param value a formatted time
     * @return the time
     */
    public static Instant toInstant(String value) {
        try {
            return ZonedDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
               // let's see if it is a V4 date
        }
        // V4 dates are base 64 encoded seconds since baseInstant
        byte [] b = decodeBase64Content(value.getBytes());
        long secondsSinceBaseInstant = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getLong();
        return baseInstant.plusSeconds(secondsSinceBaseInstant);
    }

    /**
     * Formats the value according to the value of {@link Helpers#isV4}
     * @param value a time
     * @return a formatted time
     */
    public static String fromInstant(Instant value) {
        return isV4.get() ? fromInstantV4(value) : fromInstantV3(value);
    }

    /**
     * Formats the value as ISO 8601 in UTC, to the second
     */
    public static String fromInstantV3(Instant value) {
        return dateTimeFormatter.format(value.atZone(ZoneId.of("Z")));
    }

    /**
     * Formats the value as a base64 encoded count of seconds after {@link #baseInstant}
     */
    public static String fromInstantV4(Instant value) {
        long secondsSinceBaseInstant = value.getEpochSecond() - baseInstant.getEpochSecond();
        byte []  asBytes = toBytes(secondsSinceBaseInstant, ByteOrder.LITTLE_ENDIAN);
        return encodeBase64Content(asBytes);
    }



    /* --- Base64 --- */

    public static byte[] decodeBase64Content(byte[] content) {
        return decodeBase64Content(content, false);
    }

    public static byte[] decodeBase64Content (byte[] content, boolean isCompressed) {
        byte[] value = Base64.decodeBase64(content);
        if (isCompressed) {
            return unzipBinaryContent(value);
        }
        return value;
    }

    public static byte[] unzipBinaryContent(byte[] content) {
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        try {
            GZIPInputStream g = new GZIPInputStream(bais);
            return ByteStreams.toByteArray(g);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String encodeBase64Content(byte[] value) {
        return encodeBase64Content(value, false);
    }

    public static String encodeBase64Content(byte[] value, boolean isCompressed) {
        if (!isCompressed) {
            return Base64.encodeBase64String(value);
        }
        return Base64.encodeBase64String(zipBinaryContent(value));
    }

    public static byte[] zipBinaryContent(byte[] value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // zip up the content
        try {
            GZIPOutputStream g = new GZIPOutputStream(baos);
            g.write(value, 0, value.length);
            g.flush();
            g.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return baos.toByteArray();
    }

    public static byte[] toBytes(long value, ByteOrder byteOrder) {
        byte[] longBuffer = new byte [8];
        ByteBuffer.wrap(longBuffer)
                .order(byteOrder)
                .putLong(value);
        return longBuffer;
    }

    public static byte[] toBytes(int value, ByteOrder byteOrder) {
        byte[] longBuffer = new byte [4];
        ByteBuffer.wrap(longBuffer)
                .order(byteOrder)
                .putInt(value);
        return longBuffer;
    }
}
