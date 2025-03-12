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

package org.linguafranca.pwdb.hashedblock;

import com.google.common.io.LittleEndianDataInputStream;
import org.linguafranca.pwdb.format.Helpers;

import javax.crypto.Mac;
import java.io.*;
import java.nio.ByteOrder;
import java.util.Arrays;

import static org.linguafranca.pwdb.security.Encryption.getHMacSha256Instance;
import static org.linguafranca.pwdb.security.Encryption.transformHmacKey;

/**
 * Takes an underlying stream formatted as HMAC Hashed Blocks and provides
 * the content of the blocks as a stream.
 * <p>
 * An HMac block consists of
 * <ol>
 * <li>a 32 byte HMac checksum</li>
 * <li>a 4 byte block size</li>
 * <li>{blockSize} bytes of data</li>
 * </ol>
 * <p>
 * The Class is initialised with an initial key digest. For each block this key is transformed
 * using the (implied, starting from 0) block number and used as key for the block verification process.
 * That process consists of digesting the block number, its length and its content.
 * <p>
 * KeePass streams are Little Endian.
 */
public class HmacBlockInputStream extends FilterInputStream {

    private final ByteOrder byteOrder;
    private final byte[] key;
    private volatile ByteArrayInputStream bufferStream;
    private final DataInput input;
    private boolean finished;
    private int blockCount = 0;

    /**
     * Create a (big endian) HMac Block input stream
     *
     * @param key         the key digest
     * @param inputStream the stream to process
     * @throws IOException when something horrid happens
     */
    public HmacBlockInputStream(byte[] key, InputStream inputStream) throws IOException {
        this(key, inputStream, false);
    }

    /**
     * Create an HMac Block input stream
     *
     * @param key          the key digest
     * @param inputStream  the stream to process
     * @param littleEndian true if the stream is little endian
     * @throws IOException when something horrid happens
     */
    public HmacBlockInputStream(byte[] key, InputStream inputStream, boolean littleEndian) throws IOException {
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
        byte[] hmacSha256 = new byte[32];
        input.readFully(hmacSha256);

        // get the block size
        int blockSize = input.readInt();
        if (blockSize == 0) {
            finished = true;
        }

        // read the new block
        byte[] buffer = new byte[blockSize];
        input.readFully(buffer);

        verifyHmac(buffer, blockCount, hmacSha256);

        // create a new internal stream for the block
        bufferStream = new ByteArrayInputStream(buffer);
        blockCount++;
    }

    /**
     * HmacBlockStream.cs ReadSafeBlock
     *
     * @param buffer      the buffer to check
     * @param blockNumber the block number of this buffer
     * @param hmacSha256  the hmac to verify
     */
    private void verifyHmac(byte[] buffer, long blockNumber, byte[] hmacSha256) {
        final byte[] transformedKey = transformHmacKey(this.key, Helpers.toBytes(blockNumber, byteOrder));
        final Mac mac = getHMacSha256Instance(transformedKey);
        mac.update(Helpers.toBytes(blockNumber, byteOrder));
        mac.update(Helpers.toBytes(buffer.length, byteOrder));
        if (!Arrays.equals(mac.doFinal(buffer), hmacSha256)) {
            throw new IllegalStateException("Block HMAC does not match");
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
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
