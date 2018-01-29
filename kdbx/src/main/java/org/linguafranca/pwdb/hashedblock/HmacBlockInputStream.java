package org.linguafranca.pwdb.hashedblock;

import com.google.common.io.LittleEndianDataInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Takes an underlying stream formatted as HMAC Hashed Blocks and provides
 * the content of the blocks as a stream.
 *
 * @author jo
 */
public class HmacBlockInputStream extends FilterInputStream {

    private final boolean littleEndian;
    private volatile ByteArrayInputStream bufferStream;
    private final DataInput input;
    private boolean finished;
    private int blockCount = 0;

    public HmacBlockInputStream(InputStream inputStream) throws IOException {
        this(inputStream, false);
    }

    public HmacBlockInputStream(InputStream inputStream, boolean littleEndian) throws IOException {
        super(inputStream);
        this.littleEndian = littleEndian;
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

        verifyHmac(buffer);

        // create a new internal stream for the block
        bufferStream = new ByteArrayInputStream(buffer);
        blockCount ++;
    }

    private void verifyHmac(byte[] buffer) {
        // TODO actually verify the HMAC
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
