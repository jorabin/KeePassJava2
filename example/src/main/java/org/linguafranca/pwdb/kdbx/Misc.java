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

package org.linguafranca.pwdb.kdbx;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class Misc {

    /**
     * Useful for e.g. viewing the raw file contents
     */
    public static class HexViewer {

        static PrintStream printStream = getTestPrintStream();

        public static void list(InputStream is) throws IOException {
            for (int i = 0; i < 32; i++) {
                byte[] buf = new byte [16];
                is.read(buf);
                StringBuilder sb = new StringBuilder();
                for (byte b: buf) {
                    sb.append(String.format("%02X ", b));
                }
                sb.append("  ");
                for (byte b : buf) {
                    sb.append(b < 0x20 || b > 0x7e ? (char) 0x00B7 : (char) b);
                }
                printStream.println(sb);
            }
        }
    }



}
