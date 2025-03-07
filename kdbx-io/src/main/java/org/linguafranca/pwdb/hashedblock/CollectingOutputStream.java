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

import java.io.*;

/**
 * Class allows the forwarding (as a filter) and collection of written bytes as a buffer
 * - e.g. to provide for HMAC operations
 *
 * @author jo
 */
public class CollectingOutputStream extends FilterOutputStream {

    private final ByteArrayOutputStream collectedBytes = new ByteArrayOutputStream();
    private boolean collecting = true;

    /**
     * Create a collecting stream which is set to collect from the get go
     *
     * @param out the output stream to forward/collect
     */
    public CollectingOutputStream(OutputStream out) {
        this(out, true);
    }

    /**
     * Create a collecting stream
     *
     * @param out        the output stream to forward/collect
     * @param collecting whether the initial state is collecting or not
     */
    public CollectingOutputStream(OutputStream out, boolean collecting) {
        super(out);
        this.collecting = collecting;
    }

    @Override
    public void write(int toWrite) throws IOException {
        super.write(toWrite);
        if (collecting) {
            collectedBytes.write(toWrite);
        }
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        super.write(b, off, len);
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
