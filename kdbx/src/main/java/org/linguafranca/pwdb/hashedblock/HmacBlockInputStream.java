package org.linguafranca.pwdb.hashedblock;

import com.google.common.io.LittleEndianDataInputStream;
import org.jetbrains.annotations.NotNull;
import org.linguafranca.pwdb.security.Encryption;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Takes an underlying stream formatted as HMAC Hashed Blocks and provides
 * the content of the blocks as a stream.
 *
 * @author jo
 */
public class HmacBlockInputStream extends FilterInputStream {

    private final ByteOrder byteOrder;
    private final byte[] key;
    private volatile ByteArrayInputStream bufferStream;
    private final DataInput input;
    private boolean finished;
    private int blockCount = 0;

    public HmacBlockInputStream(byte [] key, InputStream inputStream) throws IOException {
        this(key, inputStream, false);
    }

    public HmacBlockInputStream(byte [] key, InputStream inputStream, boolean littleEndian) throws IOException {
        super(inputStream);
        this.byteOrder = littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        this.key = key;
        if (littleEndian) {
            input = new LittleEndianDataInputStream(in);
        } else {
            input = new DataInputStream(in);
        }
        getBlock();
    }

    private void getBlock() throws IOException {
        // get the HMAC
        byte [] hmacSha256 = new byte [32];
        input.readFully(hmacSha256);

        // get the block size
        int blockSize = input.readInt();
        if (blockSize == 0) {
            finished = true;
        }

        // read the new block
        byte [] buffer = new byte [blockSize];
        input.readFully(buffer);

        verifyHmac(buffer, hmacSha256, blockCount);

        // create a new internal stream for the block
        bufferStream = new ByteArrayInputStream(buffer);
        blockCount ++;
    }

    /**
     * HmacBlockStream.cs ReadSafeBlock
     * @param buffer
     * @param hmacSha256
     * @param blockNumber
     */
    private void verifyHmac(byte[] buffer, byte[] hmacSha256, long blockNumber) {
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(getHmacBlockKey(key, blockNumber, byteOrder)));
        hmac.update(toBytes(blockNumber, byteOrder), 0, 8);
        hmac.update(toBytes(buffer.length, byteOrder), 0, 4);
        hmac.update(buffer, 0, buffer.length);
        hmac.update(new byte[0], 0, 0);
        byte[] computedHmacSha256 = new byte[32];
        hmac.doFinal(computedHmacSha256, 0);
        if (!Arrays.equals(computedHmacSha256, hmacSha256)) {
            throw new IllegalStateException("Block HMAC does not match");
        }
    }

    /**
     * From HmacBlockStream.cs GetHmacKey64
     * Calculates the block key for the block number ...
     * @param key the HMAC key
     * @param blockIndex the block number
     * @param order Byte order to use
     * @return a key
     */
    public static byte [] getHmacBlockKey(byte [] key, long blockIndex, ByteOrder order) {
        MessageDigest md = Encryption.getSha512MessageDigestInstance();
        md.update(toBytes(blockIndex, order));
        return md.digest(key);
    }

    private static byte[] toBytes(long value, ByteOrder byteOrder) {
        byte[] longBuffer = new byte [8];
        ByteBuffer.wrap(longBuffer)
                .order(byteOrder)
                .putLong(value);
        return longBuffer;
    }

    private static byte[] toBytes(int value, ByteOrder byteOrder) {
        byte[] longBuffer = new byte [4];
        ByteBuffer.wrap(longBuffer)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value);
        return longBuffer;
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
        return read(b , 0 , b.length);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        if (finished) {
            return -1;
        }
        int bytesRead = bufferStream.read(b, off, len);
        if (bufferStream.available() == 0) {
            getBlock();
        }
        return bytesRead;
    }

    @Override
    public int read() throws IOException {
        if (finished) {
            return -1;
        }
        int result = bufferStream.read();
        if (result == -1) {
            getBlock();
            if (finished) {
                return -1;
            }
            result = bufferStream.read();
            if (result == -1) {
                throw new IOException("Can't replenish buffer");
            }
        }
        return result;
    }
}
