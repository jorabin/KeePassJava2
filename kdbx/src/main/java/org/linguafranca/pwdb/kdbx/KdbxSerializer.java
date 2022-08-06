/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.pwdb.kdbx;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.hashedblock.CollectingInputStream;
import org.linguafranca.pwdb.hashedblock.HashedBlockInputStream;
import org.linguafranca.pwdb.hashedblock.HashedBlockOutputStream;
import org.linguafranca.pwdb.hashedblock.HmacBlockInputStream;
import org.linguafranca.pwdb.hashedblock.HmacBlockOutputStream;
import org.linguafranca.pwdb.kdbx.KdbxHeader.ProtectedStreamAlgorithm;
import org.linguafranca.pwdb.security.Encryption;
import org.linguafranca.pwdb.security.VariantDictionary;
import org.linguafranca.pwdb.security.VariantDictionary.Entry;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Mac;

/**
 * This class provides static methods for the encryption and decryption of Keepass KDBX V3 and V4 files.
 * <p/>
 * A KDBX files are little-endian and consist of the following:
 * <ol>
 *      <li>An unencrypted portion</li>
 *      <ol>
 *          <li>8 bytes Magic number</li>
 *          <li>4 bytes version</li>
 *          <li>A header containing details of the encryption of the remainder of the file</li>
 *          <p>The header fields are encoded using a TLV style. The Type is an enumeration encoded in 1 byte.
 *          The length is encoded in 4 bytes (V3: 2 bytes) and the value according to the length denoted. The sequence is
 *          terminated by a zero type with 0 length. {@link #readOuterHeader}</p>
 *          <p>In V4 there follows a 32 byte SHA-256 hash of the file so far</p>
 *          <p>In V4 there follows a 32 byte HMAC-256 hash of the file so far</p>
 *          <p>{@link KdbxHeader} details the fields of the header.</p>
 *      </ol>
 *      <li>In V3 the remainder of the file is encrypted as follows:</li>
 *      <ol>
 *          <li>A sequence of bytes contained in the header. If they don't match, decryption has not worked.</li>
 *          <li>A payload serialized in Hashed Block format, see e.g. {@link HashedBlockInputStream} for details of this.</li>
 *          <li>The content of this payload may be GZIP compressed.</li>
 *          <li>The content is now a character stream, which is expected to be
 *          XML representing a KeePass Database. Assumed UTF-8 encoding.</li>
 *      </ol>
 *      <li>In V4 the remainder of the file is encoded as HMacHashedBlocks:</li>
 *      <ol>
 *          <li>A sequence of blocks encoded using Hmac Blocks see {@link HmacBlockInputStream}</li>
 *          <li>Those blocks contain an encrypted input stream.</li>
 *          <li>The encrypted input stream optionally contains a Gzipped input stream.</li>
 *          <li>The content is now a character stream, which is an inner header {@link #readInnerHeader}
 *          followed by XML representing a KeePass Database. Assumed UTF-8 encoding.</li>
 *      </ol>
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

@SuppressWarnings("WeakerAccess")
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

        readOuterHeader(inputStream, kdbxHeader); 

        InputStream plainTextStream;

        if (kdbxHeader.getVersion() >= 4) {
        	
            verifyOuterHeader(kdbxHeader, credentials, new DataInputStream(inputStream)); 

            HmacBlockInputStream hmacBlockInputStream = new HmacBlockInputStream(kdbxHeader.getHmacKey(credentials), inputStream, true); 

            plainTextStream = kdbxHeader.createDecryptedStream(credentials.getKey(), hmacBlockInputStream);
        } 
        
        else {

            InputStream decryptedInputStream = kdbxHeader.createDecryptedStream(credentials.getKey(), inputStream);

            checkStartBytes(kdbxHeader, decryptedInputStream);

            plainTextStream = new HashedBlockInputStream(decryptedInputStream, true);
        }

        if (kdbxHeader.getCompressionFlags().equals(KdbxHeader.CompressionFlags.GZIP)) {
            plainTextStream = new GZIPInputStream(plainTextStream);
        }

        if (kdbxHeader.getVersion() >= 4) {
            readInnerHeader(kdbxHeader, plainTextStream);
        }
        
        return plainTextStream;
    }

    /**
     * Provides an {@link OutputStream} to be encoded and encrypted in either KDBX3.1 or KDBX4.x formats
     * @param credentials credentials for encryption of the stream
     * @param kdbxHeader a KDBX header to control the formatting and encryption operations
     * @param outputStream output stream to contain the KDBX3.1/KDBX4.x formatted output
     * @return an encrypted output stream to be written to, flushed, and closed by the caller
     * @throws IOException on error
     * @throws NoSuchAlgorithmException 
     */
    public static OutputStream createKdbxFormattedOutputStream(Credentials credentials, KdbxHeader kdbxHeader, OutputStream outputStream) throws IOException, NoSuchAlgorithmException {
    	writeKdbxHeader(kdbxHeader, outputStream);
    	
    	if(kdbxHeader.getVersion() == 3) {
    		OutputStream encryptedOutputStream = kdbxHeader.createEncryptedStream(credentials.getKey(), outputStream);
    		
            writeStartBytes(kdbxHeader, encryptedOutputStream);

            HashedBlockOutputStream blockOutputStream = new HashedBlockOutputStream(encryptedOutputStream, true);
            
            if(kdbxHeader.getCompressionFlags().equals(KdbxHeader.CompressionFlags.NONE)) {
                return blockOutputStream;
            }
            
            return new GZIPOutputStream(blockOutputStream);
    	}
    	
    	else {
    		writeHeaderHash(kdbxHeader, credentials, outputStream, true);
    		writeHeaderHash(kdbxHeader, credentials, outputStream, false);

    		HmacBlockOutputStream hmacBlockOutputStream = new HmacBlockOutputStream(kdbxHeader, credentials, outputStream, true);
    		return hmacBlockOutputStream;
    	}
    }
     
    /*
    This method has been replaced by createdKdbxFormattedOutputStream
    Only keeping to not completely break the simple xml code
    Eventually this should be entirely removed, even if simple xml has no future with this project
    */
    public static OutputStream createEncryptedOutputStream(Credentials credentials, KdbxHeader kdbxHeader, OutputStream outputStream) throws IOException {
    	if(kdbxHeader.getVersion() == 3) {
    		writeKdbxHeader(kdbxHeader, outputStream);
    		
    		OutputStream encryptedOutputStream = kdbxHeader.createEncryptedStream(credentials.getKey(), outputStream);

            writeStartBytes(kdbxHeader, encryptedOutputStream);

            HashedBlockOutputStream blockOutputStream = new HashedBlockOutputStream(encryptedOutputStream, true);

            if(kdbxHeader.getCompressionFlags().equals(KdbxHeader.CompressionFlags.NONE)) {
                return blockOutputStream;
            }
            return new GZIPOutputStream(blockOutputStream);
    	}
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
    private static final int FILE_VERSION_CRITICAL_MASK = 0xFFFF0000;
    private static final int FILE_VERSION_32 = 0x00030001;
    private static final int FILE_VERSION_4 = 0x00040000;
    private static final int END = 0x0A0D0A0D;

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
     * Read two lots of 4 bytes and verify that they satisfy the signature of a KDBX file;
     * @param ledis a LittleEndianDataInputStream
     * @return true if it looks like this is a KDBX file
     * @throws IOException on error
     */
    private static boolean verifyMagicNumber(LittleEndianDataInputStream ledis) throws IOException {
        int sig1 = ledis.readInt();
        int sig2 = ledis.readInt();
        return sig1 == SIG1 && sig2 == SIG2;
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
        // collect the bytes of the header, we'll need them later
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
        getOuterHeaderFields(kdbxHeader, digest, ledis);

        // stop collecting the bytes of the header
        collectingInputStream.setCollecting(false);
        kdbxHeader.setHeaderBytes(collectingInputStream.getCollectedBytes());

        return kdbxHeader;
    }

    /**
     * V4 header is followed by an SHA256 and then contains an HMACSHA256 after that.
     * @param kdbxHeader the header containing the relevant parameters
     * @param credentials the credentials - used to verify the HMAC
     * @param input an input source
     * @throws IOException on error
     */
    public static void verifyOuterHeader(KdbxHeader kdbxHeader, Credentials credentials, DataInput input) throws IOException {
        // check the SHA
        byte [] sha256 = getBytes(32, input);
        if (!Arrays.equals(kdbxHeader.getHeaderHash(), sha256)) {
            throw new IllegalStateException("Header hash does not match");
        }

        byte[] hmacKey = kdbxHeader.getHmacKey(credentials);

        // get the key for the header Hmac (using sequence number -1)
        // KdbxFile.cs ComputeHeaderHmac
        byte [] hmacKey64 = Encryption.transformHmacKey(hmacKey, Helpers.toBytes(-1L, ByteOrder.LITTLE_ENDIAN));

        kdbxHeader.verifyHeaderHmac(hmacKey64, getBytes(32, input));
    }

    private static void getOuterHeaderFields(KdbxHeader kdbxHeader, MessageDigest digest, DataInput input) throws IOException {
        byte headerType;
        do {
            headerType = input.readByte();
            int length = (kdbxHeader.getVersion() == 3 ? input.readShort() : input.readInt()); //short=2 bytes for KDBX3.1, int=4 bytes for KDBX4

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
                    kdbxHeader.setKdfParameters(makeVariantDictionary(length, input));
                    break;

                case HeaderType.CUSTOM_DATA:
                    kdbxHeader.setCustomData(makeVariantDictionary(length, input));
                    break;

                default: throw new IllegalStateException("Unknown File Header");
            }
        } while (headerType != HeaderType.END);

        kdbxHeader.setHeaderHash(digest.digest());
    }

    /**
     * Type fieds for inner headers
     * @see KdbxSerializer#readInnerHeader
     */
    private static class InnerHeaderType {
        private static final byte END = 0;
        private static final byte INNER_RANDOM_STREAM_ID = 1; // Supersedes KdbxHeaderFieldID.InnerRandomStreamID
        private static final byte INNER_RANDOM_STREAM_KEY = 2; // Supersedes KdbxHeaderFieldID.InnerRandomStreamKey
        private static final byte BINARY = 3;
    }

    /**
     * For KDBX4.x, the inner stream encryption parameters are contained in
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
                    kdbxHeader.setInnerRandomStreamKey(getBytes(length, input)); //why is this read as 64 sometimes?
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
    
    
    /**
     * For KDBX4.x, writes the inner headers preceding the XML payload of an HmacBlock
     * TODO: add ability to handle binary attachments
     * @param kdbxHeader the header whose values are to be read
     * @param encryptedOutputStream an encrypted (and optionally compressed) output stream to write the inner header to: Must also be the same stream the XML payload will be written to
     * @throws IOException 
     */
    public static void writeInnerHeader(KdbxHeader kdbxHeader, OutputStream encryptedOutputStream) throws IOException {
    	LittleEndianDataOutputStream ledosWrapper = new LittleEndianDataOutputStream(encryptedOutputStream);
    	
    	// write part of inner header for Inner Random Stream ID
    	ledosWrapper.write(1);
    	ledosWrapper.writeInt(4);
    	ledosWrapper.writeInt(ProtectedStreamAlgorithm.getAlgorithmValue(kdbxHeader.getProtectedStreamAlgorithm()));

    	// write part of inner header for Inner Random Stream Key
    	ledosWrapper.write(2);
    	ledosWrapper.writeInt(kdbxHeader.getInnerRandomStreamKey().length);
    	ledosWrapper.write(kdbxHeader.getInnerRandomStreamKey());
    	
    	// write part of inner header indicating its end
    	ledosWrapper.write(0);
    	ledosWrapper.writeInt(0);

    }

    /**
     * Read a VariantDictionary from the supplied input
     * @param input source of data
     * @return a VariantDictionary
     * @throws IOException on error
     */
    private static VariantDictionary makeVariantDictionary(int length, DataInput input) throws IOException {
        // read the buffer containing the dictionary, which starts with a 4 byte length
        ByteBuffer buf = ByteBuffer.wrap(getBytes(length, input));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        // version number must be 0x01??

        VariantDictionary vd = new VariantDictionary((short) (buf.getShort() >> 8));

        // sequence of entries followed by a byte 0
        byte type = buf.get();
        while (type != 0) {
            // get key
            int keylength = buf.getInt();
            byte [] key = new byte[keylength];
            buf.get(key);

            // get value
            int valueLength = buf.getInt();
            byte [] value = new byte[valueLength];
            buf.get(value);

            // add entry
            vd.put(new String(key), VariantDictionary.EntryType.get(type), value);

            type = buf.get();
        }
        return vd;
    }

    /**
     * Write a KdbxHeader to the output stream supplied. The header is updated with the
     * message digest of the written stream.
     * @param kdbxHeader the header to write and update
     * @param outputStream the output stream
     * @throws IOException on error
     */
    public static void writeKdbxHeader(KdbxHeader kdbxHeader, OutputStream outputStream) throws IOException {
    	ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();
    	MessageDigest messageDigest = Encryption.getSha256MessageDigestInstance();
        DigestOutputStream digestOutputStream = new DigestOutputStream(tempBaos, messageDigest);
    	LittleEndianDataOutputStream ledos = new LittleEndianDataOutputStream(digestOutputStream);
    	int databaseVersion = kdbxHeader.getVersion();
        
        // write the magic number
        ledos.writeInt(SIG1);
        ledos.writeInt(SIG2);
        
        // write appropriate file version
        if(databaseVersion == 3) {
        	ledos.writeInt(FILE_VERSION_32);
        }
        else {
        	ledos.writeInt(FILE_VERSION_4); 
        }
        
        // write cipher ID of encryption algo
        ledos.writeByte(HeaderType.CIPHER_ID);
        if(databaseVersion == 3) {
        	ledos.writeShort(16);
        }
        else {
        	ledos.writeInt(16); // in KDBX4, the length in the TLV is stored in 4 bytes, not 2
        }
        byte[] b = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.putLong(kdbxHeader.getCipherUuid().getMostSignificantBits());
        bb.putLong(8, kdbxHeader.getCipherUuid().getLeastSignificantBits());
        ledos.write(b);

        // write compression flag (whether we GZip or not)
        ledos.writeByte(HeaderType.COMPRESSION_FLAGS);
        if(databaseVersion == 3) {
        	ledos.writeShort(4);
        }
        else {
        	ledos.writeInt(4);
        }
        ledos.writeInt(kdbxHeader.getCompressionFlags().ordinal());

        // write master seed
        ledos.writeByte(HeaderType.MASTER_SEED);
        if(databaseVersion == 3) {
        	ledos.writeShort(kdbxHeader.getMasterSeed().length);
        }
        else {
        	ledos.writeInt(kdbxHeader.getMasterSeed().length);
        }
        ledos.write(kdbxHeader.getMasterSeed());

        // write KDF algo transform seed and rounds
        // in KDBX4, these are stored in the VariantDictionary as their own entries
        if(databaseVersion == 3) {
        	ledos.writeByte(HeaderType.TRANSFORM_SEED);
            ledos.writeShort(kdbxHeader.getTransformSeed().length);
            ledos.write(kdbxHeader.getTransformSeed());

            ledos.writeByte(HeaderType.TRANSFORM_ROUNDS);
            ledos.writeShort(8);
            ledos.writeLong(kdbxHeader.getTransformRounds());
        }
        else {
        	VariantDictionary kdfParameters = kdbxHeader.getKdfParameters();
        	ledos.writeByte(HeaderType.KDF_PARAMETERS);
        	ledos.writeInt(kdfParameters.getTotalBytes());
        	ledos.writeByte(0);
        	ledos.writeByte(kdfParameters.getVersion());
        	
        	for(String key : kdfParameters.getKeys()) {
        		Entry entry = kdfParameters.get(key);
        		
        		// write the Entry's type (byte array, String, etc.)
        		ledos.writeByte(entry.getType());
        		
        		// write the Entry's key length
        		ledos.writeInt(key.length());
        		
        		// write the String name of the Entry's key
        		ledos.write(key.getBytes());
        		
        		// write the Entry's value length
        		ledos.writeInt(entry.getValueLength());
        		
        		// write the bytes of Entry's value
                // TODO: this needs reworked, not good
        		if(entry.getType() == 0x42) { // byte array
        			ledos.write(entry.asByteArray());
        		}
        		else if(entry.getType() == 0xc) { // signed 32-bit integer
        			ledos.writeInt(entry.asInteger());
        		}
        		else if(entry.getType() == 0xD) {// signed 64-bit integer
        			ledos.writeLong(entry.asLong());
        		}
        		else if(entry.getType() == 0x4) {// unsigned 32-bit integer
        			if(entry.getValueLength() == 4) { // if value is an integer
        				ledos.writeLong(Integer.toUnsignedLong(entry.asInteger()));
        			}
        			else { //entry.getValueLength() == 8 (if value is a long)
        				ledos.writeLong(entry.asLong() & 0xffffffffL);
        			}
        		}
        		else if(entry.getType() == 0x5) {
        			if(entry.getValueLength() == 4) { // if value is an integer
        				ledos.writeLong(Integer.toUnsignedLong(entry.asInteger()));
        			}
        			else { //entry.getValueLength() == 8 (if value is a long)
        				ledos.writeLong(entry.asLong());
        			}
        		}
        	}
        	// write null terminator byte to signal end of VariantDictionary
        	ledos.writeByte(0);
        }
        
        // write initialization vector for encryption algo
        ledos.writeByte(HeaderType.ENCRYPTION_IV);
        if(databaseVersion == 3) {
        	ledos.writeShort(kdbxHeader.getEncryptionIv().length);
        }
        else {
        	ledos.writeInt(kdbxHeader.getEncryptionIv().length);
        }
        ledos.write(kdbxHeader.getEncryptionIv());

        // write some stream stuff that, in KDBX4, is stored in the inner header
        if(databaseVersion == 3) {
        	ledos.writeByte(HeaderType.INNER_RANDOM_STREAM_KEY);
            ledos.writeShort(kdbxHeader.getInnerRandomStreamKey().length);
            ledos.write(kdbxHeader.getInnerRandomStreamKey());

            ledos.writeByte(HeaderType.STREAM_START_BYTES);
            ledos.writeShort(kdbxHeader.getStreamStartBytes().length);
            ledos.write(kdbxHeader.getStreamStartBytes());

            ledos.writeByte(HeaderType.INNER_RANDOM_STREAM_ID);
            ledos.writeShort(4);
            ledos.writeInt(kdbxHeader.getProtectedStreamAlgorithm().ordinal());
        }
        
        // write bytes to signify end of header
        ledos.writeByte(HeaderType.END);
        if(databaseVersion == 3) {
        	ledos.writeShort(4);
        }
        else {
        	ledos.writeInt(4);
        }
        ledos.writeInt(END);

        MessageDigest digest = digestOutputStream.getMessageDigest();
        kdbxHeader.setHeaderHash(digest.digest());
        
        byte[] leba = tempBaos.toByteArray();
        kdbxHeader.setHeaderBytes(leba);
        outputStream.write(leba);
    }
    
    /**
     * In KDBX4, following the outer header data are 2 hashes.
     * 1st is a SHA256 hash of all the outer header data,
     * 2nd is an HMACSHA256 hash of all the outer header data.
     * 
     * @param kdbxHeader the header of the KDBX database
     * @param credentials the master key of the KDBX database, required for computing HMAC
     * @param outputStream the outputStream to write the bytes of the resulting hash to
     * @param isSha256 signifies whether or not to create the hash with SHA256 algorithm
     */
    public static void writeHeaderHash(KdbxHeader kdbxHeader, Credentials credentials, OutputStream outputStream, boolean isSha256) throws NoSuchAlgorithmException, IOException {
    	byte[] headerBytes = kdbxHeader.getHeaderBytes();
    	byte[] hashedHeaderBytes;
    	
    	if(isSha256) {
    		MessageDigest hashDigest = MessageDigest.getInstance("SHA-256");
			hashedHeaderBytes = hashDigest.digest(headerBytes);
			outputStream.write(hashedHeaderBytes);
    	}
    	
    	else {
    		byte[] hmacKey = kdbxHeader.getHmacKey(credentials);
            byte [] hmacKey64 = Encryption.transformHmacKey(hmacKey, Helpers.toBytes(-1L, ByteOrder.LITTLE_ENDIAN));
    		Mac mac = Encryption.getHMacSha256Instance(hmacKey64);
    		hashedHeaderBytes = mac.doFinal(headerBytes);
    		outputStream.write(hashedHeaderBytes);
    	}
    	
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
