package org.linguafranca.util;

import org.apache.commons.io.output.NullOutputStream;

import java.io.PrintStream;

public class TestUtil {
    /**
     * set system property to suppress output from tests
     * @return if "nullOutput has been set, e.g. in a profile
     */
    public static PrintStream getTestPrintStream() {
        return Boolean.getBoolean("inhibitConsoleOutput") ?
                new PrintStream(NullOutputStream.INSTANCE) :
                new PrintStream(System.out);
    }
}
