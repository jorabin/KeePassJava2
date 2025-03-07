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

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.hashedblock.*;
import org.linguafranca.pwdb.security.Encryption;
import org.linguafranca.pwdb.security.VariantDictionary;

import javax.crypto.Mac;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class provides static methods for the encryption and decryption of Keepass KDBX V3 and V4 files.
 * <p>
 * KDBX files are little-endian and consist of the following:
 * <ol>
 *      <li>An unencrypted portion</li>
 *      <li><ol>
 *          <li>8 bytes Magic number</li>
 *          <li>4 bytes version</li>
 *          <li>A header containing details of the encryption of the remainder of the file:
 *          <p>The header fields are encoded using a TLV style. The Type is an enumeration encoded in 1 byte.
 *          The length is encoded in 4 bytes (V3: 2 bytes) and the value according to the length denoted. The sequence is
 *          terminated by a zero type with 0 length. {@link #readOuterHeader}</p>
 *          <p>In V4 there follows a 32 byte SHA-256 hash of the file so far</p>
 *          <p>In V4 there follows a 32 byte HMAC-256 hash of the file so far</p>
 *          <p>{@link KdbxHeader} details the fields of the header.</p>
 *          </li>
 *      </ol></li>
 *      <li>In V3 the remainder of the file is encrypted as follows:</li>
 *      <li><ol>
 *          <li>A sequence of bytes contained in the header. If they don't match, decryption has not worked.</li>
 *          <li>A payload serialized in Hashed Block format, see e.g. {@link HashedBlockInputStream} for details of this.</li>
 *          <li>The content of this payload may be GZIP compressed.</li>
 *          <li>The content is now a character stream, which is expected to be
 *          XML representing a KeePass Database. Assumed UTF-8 encoding.</li>
 *      </ol></li>
 *      <li>In V4 the remainder of the file is encoded as HMacBlocks:</li>
 *      <li><ol>
 *          <li>A sequence of blocks encoded using Hmac Blocks see {@link HmacBlockInputStream}</li>
 *          <li>Those blocks contain an encrypted input stream.</li>
 *          <li>The encrypted input stream optionally contains a Gzipped input stream.</li>
 *          <li>The content is now a character stream, which is an inner header {@link #readInnerHeader}
 *          followed by XML representing a KeePass Database. Assumed UTF-8 encoding.</li>
 *      </ol></li>
 * </ol>
 * <p>The methods in this class provide support for serializing and deserializing plain text payload content
 *      to and from the above encrypted format.
 * <p>Various fields of the plain text XML (e.g. passwords) are additionally
 *      and optionally encrypted using a second encryption. They
 *      are stream encrypted, meaning they have to be decrypted in the
 *      same order as they were encrypted, namely actual XML document order. Or at least that
 *      is the way it seems. The methods of this class do not perform this aspect of encryption/decryption.
 * @see <a href="https://github.com/jorabin/KeePassJava2/blob/master/Format%20Diagram.svg">this diagram</a>
 * @author jo
 */
@SuppressWarnings({"WeakerAccess", "UnstableApiUsage"})
public class KdbxSerializer {

    // make entirely static
    private KdbxSerializer() {}

    /**
     * Provides the payload of a KDBX file as an unencrypted {@link InputStream}.
     * @param credentials credentials for decryption of the stream
     * @param kdbxHeader a KdbxHeader for the encryption parameters and so on
     * @param inputStream a KDBX formatted input stream
     * @return an unencrypted input stream, to be read and closed by the caller
     * @throws IOException on error
     */
    public static InputStream createUnencryptedInputStream(Credentials credentials, KdbxHeader kdbxHeader, InputStream inputStream) throws IOException {
        // read plaintext outer header
        readOuterHeader(inputStream, kdbxHeader);

        InputStream plainTextStream;

        if (kdbxHeader.getVersion() >= 4) {
            // verify the header hashes
            readOuterHeaderVerification(kdbxHeader, credentials, new DataInputStream(inputStream));
            // create an HMac Block input stream
            HmacBlockInputStream hmacBlockInputStream = new HmacBlockInputStream(kdbxHeader.getHmacKey(credentials), inputStream, true);
            // decrypt the contents of the blocks
            plainTextStream = kdbxHeader.createDecryptedStream(credentials.getKey(), hmacBlockInputStream);

        } else {// V3
            // decrypt the input stream
            InputStream decryptedInputStream = kdbxHeader.createDecryptedStream(credentials.getKey(), inputStream);
            // the unencrypted stream starts with a byte pattern
            checkStartBytes(kdbxHeader, decryptedInputStream);
            // unencrypted stream consists of hashed blocks
            plainTextStream = new HashedBlockInputStream(decryptedInputStream, true);
        }
        // need to decompress the stream
        if (kdbxHeader.getCompressionFlags().equals(KdbxHeader.CompressionFlags.GZIP)) {
            plainTextStream = new GZIPInputStream(plainTextStream);
        }
        // read inner header in V4
        if (kdbxHeader.getVersion() >= 4) {
            readInnerHeader(kdbxHeader, plainTextStream);
        }
        // stream is now positioned on payload (assumed to be Keepass XML)
        return plainTextStream;
    }

    /**
     * Provides an {@link OutputStream} to be encoded and encrypted in KDBX format
     *
     * @param credentials credentials for encryption of the stream
     * @param kdbxHeader a KDBX header to control the formatting and encryption operation
     * @param outputStream output stream to contain the KDBX formatted output
     * @return an unencrypted output stream, to be written to, flushed and closed by the caller
     * @throws IOException on error
     */
    public static OutputStream createEncryptedOutputStream(Credentials credentials, KdbxHeader kdbxHeader, OutputStream outputStream) throws IOException {
        // write plain text header
        writeKdbxHeader(kdbxHeader, outputStream);

        OutputStream result;

        if (kdbxHeader.getVersion() >= 4) {
            // write in plaintext
            writeOuterHeaderVerification(kdbxHeader, credentials, new DataOutputStream(outputStream));
            // write encrypted blocks
            OutputStream blockOutputStream = new HmacBlockOutputStream(kdbxHeader.getHmacKey(credentials), outputStream, true);
            // accept plaintext
            result = kdbxHeader.createEncryptedStream(credentials.getKey(), blockOutputStream);

        } else {
            OutputStream encryptedOutputStream = kdbxHeader.createEncryptedStream(credentials.getKey(), outputStream);
            // write encrypted
            writeStartBytes(kdbxHeader, encryptedOutputStream);
            // input is plaintext, output is encrypted after
            result = new HashedBlockOutputStream(encryptedOutputStream, true);
        }

        if(kdbxHeader.getCompressionFlags().equals(KdbxHeader.CompressionFlags.GZIP)) {
            result = new GZIPOutputStream(result);
        }

        if (kdbxHeader.getVersion() >= 4) {
            writeInnerHeader(kdbxHeader, result);
        }

        return result;
    }

    /**
     * Checks that the decrypted stream starts with the expected bytes in V3 format
     * @param kdbxHeader the header
     * @param decryptedInputStream the decrypted stream
     * @throws IOException if the stream cannot be read, etc.
     */
    private static void checkStartBytes(KdbxHeader kdbxHeader, InputStream decryptedInputStream) throws IOException {
        LittleEndianDataInputStream ledis = new LittleEndianDataInputStream(decryptedInputStream);

        byte [] startBytes = new byte[32];
        ledis.readFully(startBytes);
        if (!Arrays.equals(startBytes, kdbxHeader.getStreamStartBytes())) {
            throw new IllegalStateException("Inconsistent stream start bytes. This usually means the credentials were wrong.");
        }
    }

    /**
     * Writes the expected stream start bytes to the encrypted stream for V3 format
     * @param kdbxHeader the header
     * @param encryptedOutputStream the encrypted stream
     * @throws IOException if the stream cannot be written, etc.
     */
    private static void writeStartBytes(KdbxHeader kdbxHeader, OutputStream encryptedOutputStream) throws IOException {
        LittleEndianDataOutputStream ledos = new LittleEndianDataOutputStream(encryptedOutputStream);
        ledos.write(kdbxHeader.getStreamStartBytes());
    }

    private static final int SIG1 = 0x9AA2D903;
    private static final int SIG2 = 0xB54BFB67;
    private static final int FILE_VERSION_32 = 0x00030001;
    private static final int FILE_VERSION_4 = 0x00040000;

    private static class HeaderType {
        static final byte END = 0;
        static final byte COMMENT = 1;
        static final byte CIPHER_ID = 2;
        static final byte COMPRESSION_FLAGS = 3;
        static final byte MASTER_SEED = 4;
        static final byte TRANSFORM_SEED = 5;
        static final byte TRANSFORM_ROUNDS = 6;
        static final byte ENCRYPTION_IV = 7;
        static final byte INNER_RANDOM_STREAM_KEY = 8;
        static final byte STREAM_START_BYTES = 9;
        static final byte INNER_RANDOM_STREAM_ID = 10;
        static final byte KDF_PARAMETERS = 11;
        static final byte CUSTOM_DATA = 12;
    }

    /**
     * Create and populate a KdbxHeader from the input stream supplied
     * @param inputStream an input stream
     * @return the populated KdbxHeader
     * @throws IOException on error
     */
    public static KdbxHeader readOuterHeader(InputStream inputStream, KdbxHeader kdbxHeader) throws IOException {

        // header is digested to verify correctness
        MessageDigest digest = Encryption.getSha256MessageDigestInstance();
        DigestInputStream shaDigestInputStream = new DigestInputStream(inputStream, digest);
        // collect the bytes of the header, we'll need them later for the HMAC header calculation
        // we can't use a similar technique to the digest stream above, since the
        // HMac calculation depends on having collected a couple of the header fields
        CollectingInputStream collectingInputStream = new CollectingInputStream(shaDigestInputStream, true);
        // make values available from LittleEndian
        LittleEndianDataInputStream ledis = new LittleEndianDataInputStream(collectingInputStream);

        // file starts with magic number
        if (!verifyMagicNumber(ledis)) {
            throw new IllegalStateException("Magic number did not match");
        }
        // followed by a file version number
        int fullVersion = ledis.readInt();
        kdbxHeader.setVersion(fullVersion >> 16);

        // read header fields
        getOuterHeaderFields(kdbxHeader, ledis);

        // stop digesting
        shaDigestInputStream.on(false);
        // stop collecting the bytes of the header
        collectingInputStream.setCollecting(false);

        kdbxHeader.setHeaderHash(digest.digest());
        kdbxHeader.setHeaderBytes(collectingInputStream.getCollectedBytes());

        return kdbxHeader;
    }

    /**
     * V4 header is followed by an SHA256 and then contains an HMAC SHA256 after that.
     * @param kdbxHeader the header containing the relevant parameters
     * @param credentials the credentials - used to verify the HMAC
     * @param input an input source
     * @throws IOException on error
     */
    public static void readOuterHeaderVerification(KdbxHeader kdbxHeader, Credentials credentials, DataInput input) throws IOException {
        // check the SHA
        byte [] receivedSha256 = getBytes(32, input);
        if (!Arrays.equals(kdbxHeader.getHeaderHash(), receivedSha256)) {
            throw new IllegalStateException("Header hash does not match");
        }

        byte[] hmacKey = kdbxHeader.getHmacKey(credentials);
        // get the key for the header Hmac (using sequence number -1)
        // KdbxFile.cs ComputeHeaderHmac
        byte [] hmacKey64 = Encryption.transformHmacKey(hmacKey, Helpers.toBytes(-1L, ByteOrder.LITTLE_ENDIAN));
        Mac mac = Encryption.getHMacSha256Instance(hmacKey64);
        byte [] computedHmacSha256 = mac.doFinal(kdbxHeader.getHeaderBytes());
        byte [] receivedHmacSha256 = getBytes(32, input);
        if (!Arrays.equals(computedHmacSha256, receivedHmacSha256)) {
            throw new IllegalStateException("Header HMAC does not match");
        }
    }

    /**
     * Write the hashes required after the header for V4
     * @param kdbxHeader the header whose hashes need to be written
     * @param credentials database credentials
     * @param dataOutputStream somewhere to write to
     */

    private static void writeOuterHeaderVerification(KdbxHeader kdbxHeader, Credentials credentials, DataOutputStream dataOutputStream) throws IOException {
        // the SHA digest
        dataOutputStream.write(kdbxHeader.getHeaderHash());

        // the HMac
        byte[] hmacKey = kdbxHeader.getHmacKey(credentials);
        byte[] hmacKey64 = Encryption.transformHmacKey(hmacKey, Helpers.toBytes(-1L, ByteOrder.LITTLE_ENDIAN));
        Mac mac = Encryption.getHMacSha256Instance(hmacKey64);
        byte[] hashedHeaderBytes = mac.doFinal(kdbxHeader.getHeaderBytes());
        dataOutputStream.write(hashedHeaderBytes);
    }

    private static void getOuterHeaderFields(KdbxHeader kdbxHeader, DataInput input) throws IOException {
        byte headerType;
        do {
            headerType = input.readByte();
            int length = (kdbxHeader.getVersion() == 3 ? input.readShort() : input.readInt());

            switch (headerType) {

                case HeaderType.END: {
                    getBytes(length, input);
                    break;
                }

                case HeaderType.COMMENT:
                    getBytes(length, input);
                    break;

                case HeaderType.CIPHER_ID:
                    kdbxHeader.setCipherUuid(getBytes(length, input));
                    break;

                case HeaderType.COMPRESSION_FLAGS:
                    kdbxHeader.setCompressionFlags(getInt(length, input));
                    break;

                case HeaderType.MASTER_SEED:
                    kdbxHeader.setMasterSeed(getBytes(length, input));
                    break;

                case HeaderType.TRANSFORM_SEED:
                    kdbxHeader.setTransformSeed(getBytes(length, input));
                    break;

                case HeaderType.TRANSFORM_ROUNDS:
                    kdbxHeader.setTransformRounds(getLong(length, input));
                    break;

                case HeaderType.ENCRYPTION_IV:
                    kdbxHeader.setEncryptionIv(getBytes(length, input));
                    break;

                case HeaderType.INNER_RANDOM_STREAM_KEY:
                    kdbxHeader.setInnerRandomStreamKey(getBytes(length, input));
                    break;

                case HeaderType.STREAM_START_BYTES:
                    kdbxHeader.setStreamStartBytes(getBytes(length, input));
                    break;

                case HeaderType.INNER_RANDOM_STREAM_ID:
                    kdbxHeader.setInnerRandomStreamId(getInt(length, input));
                    break;

                case HeaderType.KDF_PARAMETERS:
                    kdbxHeader.setKdfParameters(readVariantDictionary(getBytes(length, input)));
                    break;

                case HeaderType.CUSTOM_DATA:
                    kdbxHeader.setCustomData(readVariantDictionary(getBytes(length, input)));
                    break;

                default: throw new IllegalStateException("Unknown File Header");
            }
        } while (headerType != HeaderType.END);
    }

    /**
     * Write a KdbxHeader to the output stream supplied. The header is updated with the
     * message digest of the written stream.
     * @param kdbxHeader the header to write and update
     * @throws IOException on error
     */
    @SuppressWarnings("UnstableApiUsage")
    public static void writeKdbxHeader(KdbxHeader kdbxHeader, OutputStream outputStream) throws IOException {
        MessageDigest messageDigest = Encryption.getSha256MessageDigestInstance();
        DigestOutputStream digestOutputStream = new DigestOutputStream(outputStream, messageDigest);
        CollectingOutputStream collectingOutputStream = new CollectingOutputStream(digestOutputStream);
        LittleEndianDataOutputStream ledos = new LittleEndianDataOutputStream(collectingOutputStream);

        // lengths are short in v3 int in v4
        IntConsumer lengthWriter = (i -> {
            try {
                if (kdbxHeader.getVersion() == 3) {
                    ledos.writeShort(i);
                } else {
                    ledos.writeInt(i);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // write the magic number
        ledos.writeInt(SIG1);
        ledos.writeInt(SIG2);
        // write a file version
        ledos.writeInt(kdbxHeader.getVersion() == 3?FILE_VERSION_32:FILE_VERSION_4);

        ledos.writeByte(HeaderType.CIPHER_ID);
        lengthWriter.accept(16);
        byte[] b = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.putLong(kdbxHeader.getCipherUuid().getMostSignificantBits());
        bb.putLong(8, kdbxHeader.getCipherUuid().getLeastSignificantBits());
        ledos.write(b);

        ledos.writeByte(HeaderType.COMPRESSION_FLAGS);
        lengthWriter.accept(4);
        ledos.writeInt(kdbxHeader.getCompressionFlags().ordinal());

        ledos.writeByte(HeaderType.MASTER_SEED);
        lengthWriter.accept(kdbxHeader.getMasterSeed().length);
        ledos.write(kdbxHeader.getMasterSeed());

        if (kdbxHeader.getVersion() < 4) {
            ledos.writeByte(HeaderType.TRANSFORM_SEED);
            lengthWriter.accept(kdbxHeader.getTransformSeed().length);
            ledos.write(kdbxHeader.getTransformSeed());

            ledos.writeByte(HeaderType.TRANSFORM_ROUNDS);
            lengthWriter.accept(8);
            ledos.writeLong(kdbxHeader.getTransformRounds());
        }

        ledos.writeByte(HeaderType.ENCRYPTION_IV);
        lengthWriter.accept(kdbxHeader.getEncryptionIv().length);
        ledos.write(kdbxHeader.getEncryptionIv());

        if (kdbxHeader.getVersion() < 4) {
            ledos.writeByte(HeaderType.INNER_RANDOM_STREAM_KEY);
            lengthWriter.accept(kdbxHeader.getInnerRandomStreamKey().length);
            ledos.write(kdbxHeader.getInnerRandomStreamKey());

            ledos.writeByte(HeaderType.STREAM_START_BYTES);
            lengthWriter.accept(kdbxHeader.getStreamStartBytes().length);
            ledos.write(kdbxHeader.getStreamStartBytes());

            ledos.writeByte(HeaderType.INNER_RANDOM_STREAM_ID);
            lengthWriter.accept(4);
            ledos.writeInt(kdbxHeader.getProtectedStreamAlgorithm().ordinal());
        }

        if (kdbxHeader.getVersion() > 3) {
            ledos.writeByte(HeaderType.KDF_PARAMETERS);
            byte [] vd = serializeVariantDictionary(kdbxHeader.getKdfParameters());
            lengthWriter.accept(vd.length);
            ledos.write(vd);
        }

        ledos.writeByte(HeaderType.END);
        lengthWriter.accept(0);

        MessageDigest digest = digestOutputStream.getMessageDigest();
        kdbxHeader.setHeaderHash(digest.digest());

        collectingOutputStream.setCollecting(false);
        kdbxHeader.setHeaderBytes(collectingOutputStream.getCollectedBytes());
    }

    /**
     * Type fields for inner headers
     * @see KdbxSerializer#readInnerHeader
     */
    private static class InnerHeaderType {
        private static final byte END = 0;
        private static final byte INNER_RANDOM_STREAM_ID = 1; // Supersedes KdbxHeaderFieldID.InnerRandomStreamID
        private static final byte INNER_RANDOM_STREAM_KEY = 2; // Supersedes KdbxHeaderFieldID.InnerRandomStreamKey
        private static final byte BINARY = 3;
    }

    /**
     * From V4 the inner stream encryption parameters are contained in
     * a set of headers immediately preceding the XML payload
     * @param kdbxHeader the header whose values are to be read
     * @param plainTextStream a stream to read them from
     * @throws IOException on error
     */
    private static void readInnerHeader(KdbxHeader kdbxHeader, InputStream plainTextStream) throws IOException {
        DataInput input = new LittleEndianDataInputStream(plainTextStream);

        byte headerType;
        do {
            headerType = input.readByte();
            int length = input.readInt();

            switch (headerType) {
                case InnerHeaderType.END: {
                    getBytes(length, input);
                    break;
                }

                case InnerHeaderType.INNER_RANDOM_STREAM_ID: {
                    kdbxHeader.setInnerRandomStreamId(getInt(length, input));
                    break;
                }

                case InnerHeaderType.INNER_RANDOM_STREAM_KEY: {
                    kdbxHeader.setInnerRandomStreamKey(getBytes(length, input));
                    break;
                }

                case InnerHeaderType.BINARY: {
                    kdbxHeader.addBinary(getBytes(length, input));
                    break;
                }

                default: throw new IllegalStateException("Invalid inner header field");
            }
        } while (headerType != HeaderType.END);
    }

    public static void writeInnerHeader(KdbxHeader kdbxHeader, OutputStream outputStream) throws IOException {
        DataOutput output = new LittleEndianDataOutputStream(outputStream);

        output.writeByte(InnerHeaderType.INNER_RANDOM_STREAM_ID);
        output.writeInt(4);
        output.writeInt(kdbxHeader.getProtectedStreamAlgorithm().ordinal());

        output.writeByte(InnerHeaderType.INNER_RANDOM_STREAM_KEY);
        output.writeInt(kdbxHeader.getInnerRandomStreamKey().length);
        output.write(kdbxHeader.getInnerRandomStreamKey());

        for (byte [] binary: kdbxHeader.getBinaries()) {
            output.writeByte(InnerHeaderType.BINARY);
            output.writeInt(binary.length);
            output.write(binary);
        }

        output.writeByte(InnerHeaderType.END);
        output.writeInt(0);
    }

    /**
     * Read a VariantDictionary from the supplied input according to KDBX rules
     * @param source source of data
     * @return a VariantDictionary
     */
    public static VariantDictionary readVariantDictionary(byte [] source) {
        // read the buffer containing the dictionary, which starts with a 4 byte length
        ByteBuffer buf = ByteBuffer.wrap(source);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        // version number must be 0x01??

        VariantDictionary vd = new VariantDictionary((short) (buf.getShort() >> 8));

        // sequence of entries followed by a byte 0
        byte type = buf.get();
        while (type != 0) {
            // get key
            int keyLength = buf.getInt();
            byte [] key = new byte[keyLength];
            buf.get(key);

            // get value
            int valueLength = buf.getInt();
            byte [] value = new byte[valueLength];
            buf.get(value);

            // add entry
            vd.put(new String(key, StandardCharsets.US_ASCII), VariantDictionary.EntryType.get(type), value);

            type = buf.get();
        }
        return vd;
    }

    /**
     * Serialize a variant dictionary according to KDBX rules
     * @param v the dictionary to serialize
     * @return a byte array
     */
    public static byte[] serializeVariantDictionary(VariantDictionary v) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[1024]);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.mark();
        // version number must be 0x01??
        buf.putShort((short) 0x100); //1
        for (Map.Entry<String, VariantDictionary.Entry> e : v.getEntries().entrySet()){
            buf.put(e.getValue().getType());
            buf.putInt(e.getKey().length());
            buf.put(e.getKey().getBytes(StandardCharsets.US_ASCII));
            buf.putInt(e.getValue().asByteArray().length);
            buf.put(e.getValue().asByteArray());
        }
        buf.put((byte) 0);
        byte[] result = new byte[buf.position()];
        buf.reset();
        buf.get(result);
        return result;
    }

    /**
     * Read two lots of 4 bytes and verify that they satisfy the signature of a kdbx file;
     * @param ledis an input stream
     * @return true if it looks like this is a kdbx file
     * @throws IOException on error
     */
    private static boolean verifyMagicNumber(LittleEndianDataInputStream ledis) throws IOException {
        int sig1 = ledis.readInt();
        int sig2 = ledis.readInt();
        return sig1 == SIG1 && sig2 == SIG2;
    }

    private static int getInt(int length, DataInput input) throws IOException {
        if (length != 4) {
            throw new IllegalStateException("Int required but length was " + length);
        }
        return input.readInt();
    }

     private static long getLong(int length, DataInput input) throws IOException {
        if (length != 8) {
            throw new IllegalStateException("Long required but length was " + length);
        }
        return input.readLong();
    }

    private static byte[] getBytes(int numBytes, DataInput input) throws IOException {
        byte [] result = new byte[numBytes];
        input.readFully(result);
        return result;
    }
}
