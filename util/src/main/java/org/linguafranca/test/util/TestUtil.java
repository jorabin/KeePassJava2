package org.linguafranca.test.util;

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
     * Do nothing output stream
     */
    public static class NullOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {

        }
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
