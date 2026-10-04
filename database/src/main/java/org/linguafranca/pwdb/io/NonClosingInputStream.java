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

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * An input stream that leaves the stream it wraps open when it is closed.
 * <p>
 * Use it to give a stream to code that closes the streams it is given, when the
 * caller wants to keep the stream open.
 */
public class NonClosingInputStream extends FilterInputStream {

    /**
     * @param inputStream the stream to read from, which this stream does not close
     */
    public NonClosingInputStream(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Does nothing: the wrapped stream stays open
     */
    @Override
    public void close() {
    }
}
