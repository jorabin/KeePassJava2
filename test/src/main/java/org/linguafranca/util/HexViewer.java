package org.linguafranca.util;

import java.io.IOException;
import java.io.InputStream;

public class HexViewer {

    public static void main(String[] args) throws IOException {


        try (InputStream is = HexViewer.class.getClassLoader().getResourceAsStream("V4-AES-Argon2-CustomIcon.kdbx");) {

            //HexFormat format = HexFormat.ofDelimiter(" ");

            for (int i = 0; i < 32; i++) {
                byte[] buf = new byte [16];
                is.read(buf);
                StringBuilder sb = new StringBuilder();
                for (byte b: buf) {
                    sb.append(String.format("%02X", b));
                }
                sb.append("  ");
                for (byte b : buf) {
                    sb.append(b < 0x20 || b > 0x7e ? (char) 0x00B7 : (char) b);
                }
                System.out.println(sb);
            }
        }
    }
}


