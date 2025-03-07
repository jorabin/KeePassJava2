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

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class allows the forwarding (as a filter) and collection of read bytes as a buffer
 * - e.g. to provide for HMAC operations
 *
 * @author jo
 */
public class CollectingInputStream extends FilterInputStream {

    private ByteArrayOutputStream collectedBytes = new ByteArrayOutputStream();
    private boolean collecting = true;

    /**
     * Create a collecting stream which is set to collect from the get go
     *
     * @param in the input stream to forward/collect
     */
    public CollectingInputStream(InputStream in) {
        this(in, true);
    }

    /**
     * Create a collecting stream
     *
     * @param in         the input stream to forward/collect
     * @param collecting whether the initial state is collecting or not
     */
    public CollectingInputStream(InputStream in, boolean collecting) {
        super(in);
        this.collecting = collecting;
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        if (collecting && result != -1) {
            collectedBytes.write(result);
        }
        return result;
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (collecting && result != -1) {
            collectedBytes.write(b, off, result);
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        return super.skip(n);
    }

    /**
     * Get the collected bytes as a byte array
     */
    public byte[] getCollectedBytes() {
        return collectedBytes.toByteArray();
    }

    /**
     * True if we are currently collecting bytes
     */
    public boolean isCollecting() {
        return collecting;
    }

    /**
     * Cganeg the state of collecting bytes
     * @param collecting true to collect
     */
    public void setCollecting(boolean collecting) {
        this.collecting = collecting;
    }
}
