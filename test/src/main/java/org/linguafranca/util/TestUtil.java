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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtil {
    /**
     * The directory tests write their output files to
     */
    public static final String TEST_OUTPUT_DIR = "testOutput";

    /**
     * Returns the path of a file in the test output directory, creating the directory if need be,
     * so that a test doesn't depend on another test having created it
     * @param name the file name
     * @return the path
     * @throws IOException if the directory can't be created
     */
    public static Path testOutputPath(String name) throws IOException {
        Path dir = Paths.get(TEST_OUTPUT_DIR);
        Files.createDirectories(dir);
        return dir.resolve(name);
    }


    /**
     * Database save closes output stream. If you want to output to console using KdbxFormat.None and <code>System.out</code>
     * then use this class to wrap <code>System.out</code>, so that it doesn't get closed.
     */
    public static class UncloseableOutputStream extends OutputStream {

        private final OutputStream outputStream;

        public UncloseableOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    /**
     * Null output stream, i.e. throw away everything that is written to it. Useful during builds
     * when you don't want to see the output - e.g. CI builds.
     */
    public static class NullOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {}
    }

    /**
     * Get a print stream that will not close <code>System.out</code>, but will throw away all output
     * if "inhibitConsoleOutput" system property has been set - e.g. as a result of using
     * <code>mvn clean install -P inhibitConsoleOutput</code>.
     */
    public static PrintStream getTestPrintStream() {
        return Boolean.getBoolean("inhibitConsoleOutput") ?
                // throw away all output
                new PrintStream(new NullOutputStream()) :
                // never close System.out
                new PrintStream(new UncloseableOutputStream(System.out));
    }
}