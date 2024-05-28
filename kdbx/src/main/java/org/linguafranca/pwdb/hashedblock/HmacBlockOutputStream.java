package org.linguafranca.pwdb.hashedblock;

import static org.linguafranca.pwdb.kdbx.Helpers.toBytes;
import static org.linguafranca.pwdb.security.Encryption.getHMacSha256Instance;
import static org.linguafranca.pwdb.security.Encryption.transformHmacKey;

import java.io.*;
import java.nio.ByteOrder;
import javax.crypto.Mac;
import org.jetbrains.annotations.NotNull;
import org.linguafranca.pwdb.util.SwappedDataOutputStream;

/**
 * Takes a stream of (encrypted GZIPped) data and formats it as HMAC Hashed Blocks to the
 * underlying output stream.
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
 * Streams in Keepass are Little Endian.
 */

public class HmacBlockOutputStream extends FilterOutputStream {
    private static final int BLOCK_SIZE = (int) Math.pow(2, 20); // blocks can technically be bigger but KeePass supposedly splits them into this size

    private final byte[] key;

    private final ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
    /** the encrypted, compressed output stream */
    private final DataOutput output;
    private final ByteOrder byteOrder;
    private long nextSequenceNumber = 0;
    private boolean isClosed = false;

    /**
     * Create a BigEndian HmacBlockOutputStream
     * @param key credentials for this database
     * @param outputStream the output stream to receive the hash blocks
     */
    public HmacBlockOutputStream(byte [] key, OutputStream outputStream) throws IOException {
        this(key, outputStream, false);
    }

    /**
     * Create a HmacBlockOutputStream with choice of endian encoding
     * @param key credentials for this database
     * @param outputStream the output stream to receive the hash blocks
     * @param littleEndian true to encode in a little endian way
     */
    public HmacBlockOutputStream(byte [] key, OutputStream outputStream, boolean littleEndian) throws IOException {
        super(outputStream);
        this.key = key;
        this.byteOrder = littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        if (this.byteOrder.equals(ByteOrder.LITTLE_ENDIAN)) {
            this.output = new SwappedDataOutputStream(out);
        } else {
            this.output = new DataOutputStream(out);
        }
    }

    @Override
    public void close() throws IOException {
        if (isClosed) {
            throw new EOFException();
        }
        // clear unwritten data
        flush();
        // write final block
        writeBlock(new byte [0]);
        // push the contents to disk etc.
        out.flush();
        out.close();
        isClosed = true;
    }

    private void writeBlock(byte [] buffer) throws IOException {
        final byte[] transformedKey = transformHmacKey(this.key, toBytes(nextSequenceNumber, byteOrder));
        final Mac mac = getHMacSha256Instance(transformedKey);
        mac.update(toBytes(nextSequenceNumber++, byteOrder));
        mac.update(toBytes(buffer.length, byteOrder));
        mac.update(buffer);
        byte[] finalMac =  mac.doFinal();
        output.write(finalMac);

        // write the buffer's length
        output.writeInt(buffer.length);
        output.write(buffer);
        out.flush();
    }

    /**
     * Writes to the internal buffer, and writes to the underlying output stream
     * as necessary as {@link #BLOCK_SIZE} blocks
     * @param b the byte array to write
     * @param offset offset in the byte array
     * @param length number of bytes to write
     */
    protected void put(byte[] b, int offset, int length) throws IOException {
        if (isClosed) {
            throw new EOFException();
        }

        while (length > 0) {
            int bytesToWrite = Math.min(BLOCK_SIZE - outputBuffer.size(), length);
            outputBuffer.write(b, offset, bytesToWrite);

            if (outputBuffer.size() >= BLOCK_SIZE) {
                save();
            }

            offset += bytesToWrite;
            length -= bytesToWrite;
        }
    }

    /**
     * Save the internal buffer to the underlying stream as a Hmac block
     */
    protected void save() throws IOException {
        if (outputBuffer.size() == 0) {
            return;
        }
        writeBlock(outputBuffer.toByteArray());
        outputBuffer.reset();
    }

    @Override
    public void write(int i) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte) i;
        put(buf, 0, 1);
    }

    @Override
    public void write(byte @NotNull [] b, int offset, int count) throws IOException {
        put(b, offset, count);
    }

    @Override
    public void flush() throws IOException {
        save();
    }
}