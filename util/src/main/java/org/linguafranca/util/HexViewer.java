package org.linguafranca.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * Useful for e.g. viewing the raw file contents
 */
public class HexViewer {

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


