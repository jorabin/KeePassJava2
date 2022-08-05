package org.linguafranca.pwdb.hashedblock;

import static org.linguafranca.pwdb.kdbx.Helpers.toBytes;
import static org.linguafranca.pwdb.security.Encryption.getHMacSha256Instance;
import static org.linguafranca.pwdb.security.Encryption.transformHmacKey;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Mac;

import org.jetbrains.annotations.NotNull;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxSerializer;

import com.google.common.io.LittleEndianDataOutputStream;

/**
 * Takes a stream of data and formats it as HMAC Hashed Blocks to the underlying output stream.
 * <p>
 * An HMac block consists of
 * <ol>
 * <li>a 32 byte HMac checksum</li>
 * <li>a 4 byte block size</li>
 * <li>{blockSize} bytes of data</li>
 * </ol>
 * <p>
 * The Class is initialized with an initial key digest. For each block this key is transformed
 * using the (implied, starting from 0) block number and used as the key for the block verification process.
 * That process consists of digesting the block number, its length, and its content.
 * <p>
 * KeePass streams are Little Endian.
 */

public class HmacBlockOutputStream extends OutputStream {
	  private static final int BLOCK_SIZE = (int)Math.pow(2, 20); // blocks can technically be bigger but KeePass supposedly splits them into this size
    
    private final KdbxHeader kdbxHeader;
    private final Credentials credentials;
    private final byte[] key;
    private boolean littleEndian = false;
    
    private ByteArrayOutputStream tempBaos = new ByteArrayOutputStream();
    private ByteArrayOutputStream blockCipherTextBaos = new ByteArrayOutputStream();
    private OutputStream encryptedOutputStream;
    private OutputStream ledos;
    private OutputStream outputStream;
    
    private final ByteOrder byteOrder;
    private long nextSequenceNumber = 0;
    private boolean newBlock = true;
    private boolean isClosed = false;

    /**
     * Create a BigEndian HmacBlockOutputStream
     * @param key the key for the HMAC
     * @param outputStream the output stream to receive the hash blocks
     * @throws IOException 
     */
    public HmacBlockOutputStream(KdbxHeader kdbxHeader, Credentials credentials, OutputStream outputStream) throws IOException {
        this(kdbxHeader, credentials, outputStream, false);
    }

    /**
     * Create a HmacBlockOutputStream with choice of endian encoding
     * @param kdbxHeader a KDBX4.x formatted header for obtaining the HMAC key and correctly formatting the encrypted output stream
     * @param credentials the master key of the KDBX database, required for creating an encrypted output stream
     * @param outputStream the output stream to receive the hash blocks
     * @param littleEndian true to encode in a little endian way
     * @throws IOException 
     */
    public HmacBlockOutputStream(KdbxHeader kdbxHeader, Credentials credentials, OutputStream outputStream, boolean littleEndian) throws IOException {
    	  this.kdbxHeader = kdbxHeader;
    	  this.credentials = credentials;
    	  this.key = kdbxHeader.getHmacKey(credentials);
    	
    	  ledos = new LittleEndianDataOutputStream(tempBaos);

    	  this.outputStream = outputStream;
        this.littleEndian = littleEndian;
        
        if(littleEndian) {
        	  byteOrder = ByteOrder.LITTLE_ENDIAN;
        }
        else {
        	  byteOrder = ByteOrder.BIG_ENDIAN;
        }
        
    }

    @Override
    public void write(int i) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte) i;
        put(buf, 0, 1);
    }

    @Override
    public void write(@NotNull byte[] b, int offset, int count) throws IOException {
        put(b, offset, count);
    }
  
    @Override
    public void flush() throws IOException {
        save();
    }

    @Override
    public void close() throws IOException {
        if (isClosed) {
            throw new EOFException();
        }
      
        flush();

        isClosed = true;
        
        final byte[] transformedKey = transformHmacKey(this.key, toBytes(nextSequenceNumber, byteOrder));
        final Mac mac = getHMacSha256Instance(transformedKey);
        mac.update(toBytes(nextSequenceNumber, byteOrder));
        mac.update(toBytes(0, byteOrder));
        outputStream.write(mac.doFinal());
        
        // write the buffer's length
        writeInt(0);

        // push the contents to disk etc.
        outputStream.flush();
        
        outputStream.close();
    }

    /**
     * Writes to the internal buffer, and writes to the underlying output stream
     * as necessary as {@link #BLOCK_SIZE} blocks
     * @param b the byte array to write
     * @param offset offset in the byte array
     * @param length number of bytes to write
     * @throws IOException
     */
    protected void put(byte[] b, int offset, int length) throws IOException {
        if (isClosed) {
            throw new EOFException();
        }
        
        while (length > 0) {
        	  if(newBlock) {    		
        		    //reopen the encrypted and optionally compressed output stream
        		    encryptedOutputStream = kdbxHeader.createEncryptedStream(credentials.getKey(), blockCipherTextBaos);
            	  if(kdbxHeader.getCompressionFlags().equals(KdbxHeader.CompressionFlags.GZIP)) {
                    encryptedOutputStream = new GZIPOutputStream(encryptedOutputStream);
            	  }
        		    KdbxSerializer.writeInnerHeader(kdbxHeader, ledos);
        		    newBlock = false;
        	  }
        	
        	  int bytesToWrite = Math.min(BLOCK_SIZE - tempBaos.size(), length);
        	  ledos.write(b, offset, bytesToWrite);
        	
        	  if (tempBaos.size() >= BLOCK_SIZE) {
                save();
            }
          
            offset += bytesToWrite;
            length -= bytesToWrite;
          
         }
        
    }

    /**
     * Save the internal buffer to the underlying stream as a hash block
     * @throws IOException
     */
    protected void save() throws IOException {
    	  if (tempBaos.size() == 0) {
            return;
        }
      
    	  // calculate the hash of the bufferStream (has to be first because of how he did it in input stream)
        encryptedOutputStream.write(tempBaos.toByteArray());
        encryptedOutputStream.close();
        byte[] encryptedBuffer = blockCipherTextBaos.toByteArray();
        final byte[] transformedKey = transformHmacKey(this.key, toBytes(nextSequenceNumber, byteOrder));
        final Mac mac = getHMacSha256Instance(transformedKey);
        mac.update(toBytes(nextSequenceNumber, byteOrder));
        mac.update(toBytes(encryptedBuffer.length, byteOrder));
        outputStream.write(mac.doFinal(encryptedBuffer));
        
        // increment the block sequence number
        nextSequenceNumber++;

        // write the encrypted buffer's length
        writeInt(encryptedBuffer.length);

        // write the encrypted buffer
        outputStream.write(encryptedBuffer);

        // push the contents to disk etc.
        outputStream.flush();

        // reset the internal unencrypted, uncompressed output buffer for reuse
        tempBaos.reset();
        
        // reset the internal encrypted, optionally compressed output buffer for reuse
        blockCipherTextBaos.reset();
        
        // reset newBlock
        newBlock = true;
        
    }

    /**
     * Write a 4 byte int value to the underlying stream in appropriate endian format
     * @param value the value to write
     * @throws IOException
     */
    protected void writeInt(int value) throws IOException {
        int output = value;
        if (littleEndian) {
            output = Integer.reverseBytes(value);
        }
        outputStream.write(new byte[]{(byte) (output >> 24), (byte) (output >> 16), (byte) (output >> 8), (byte) output});
    }
  
}
