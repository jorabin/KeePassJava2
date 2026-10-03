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

package org.linguafranca.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * In-memory streams that record whether they have been closed, for testing
 * that write and read leave the caller's stream open, and that save and load close it (issue #109)
 */
public class CloseTracking {

    public static class OutputStream extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }

    public static class InputStream extends ByteArrayInputStream {
        private boolean closed;

        public InputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }
}
