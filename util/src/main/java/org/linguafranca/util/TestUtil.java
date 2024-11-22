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
