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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that flushes, rather than closes, the stream it wraps when it is closed.
 * <p>
 * Use it to give a stream to code that closes the streams it is given, when the
 * caller wants to keep the stream open.
 */
public class NonClosingOutputStream extends FilterOutputStream {

    /**
     * @param outputStream the stream to write to, which this stream does not close
     */
    public NonClosingOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Writes straight to the wrapped stream; {@link FilterOutputStream} writes byte by byte
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    /**
     * Flushes the wrapped stream and leaves it open
     */
    @Override
    public void close() throws IOException {
        flush();
    }
}
