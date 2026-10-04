/*
 * Copyright (c) 2026. Jo Rabin
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

package org.linguafranca.pwdb.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class NonClosingStreamTest {

    /** Records whether it was flushed or closed, and how many writes it received */
    static class TrackingOutputStream extends FilterOutputStream {
        boolean flushed;
        boolean closed;
        int writes;

        TrackingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            writes++;
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            writes++;
            out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            flushed = true;
            super.flush();
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    static class TrackingInputStream extends FilterInputStream {
        boolean closed;

        TrackingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    @Test
    void closeFlushesAndLeavesOutputOpen() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        TrackingOutputStream tracking = new TrackingOutputStream(bytes);
        try (OutputStream outputStream = new NonClosingOutputStream(tracking)) {
            outputStream.write("Hello".getBytes(StandardCharsets.UTF_8));
        }
        assertTrue(tracking.flushed);
        assertFalse(tracking.closed);
        // still usable
        tracking.write(", World".getBytes(StandardCharsets.UTF_8));
        assertEquals("Hello, World", bytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void writesArraysWhole() throws IOException {
        TrackingOutputStream tracking = new TrackingOutputStream(new ByteArrayOutputStream());
        try (OutputStream outputStream = new NonClosingOutputStream(tracking)) {
            outputStream.write(new byte[100], 10, 50);
        }
        assertEquals(1, tracking.writes);
    }

    @Test
    void closeLeavesInputOpen() throws IOException {
        TrackingInputStream tracking = new TrackingInputStream(
                new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8)));
        try (InputStream inputStream = new NonClosingInputStream(tracking)) {
            assertEquals('H', inputStream.read());
        }
        assertFalse(tracking.closed);
        assertEquals('e', tracking.read());
    }
}
