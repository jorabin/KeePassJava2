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

package org.linguafranca.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class TestUtil {

    public static class NullOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {}
    }
    /**
     * set system property to suppress output from tests
     * @return if "inhibitConsoleOutput" has been set, e.g. in a profile
     */
    public static PrintStream getTestPrintStream() {
        return Boolean.getBoolean("inhibitConsoleOutput") ?
                new PrintStream(new NullOutputStream()) :
                new PrintStream(System.out);
    }
}
