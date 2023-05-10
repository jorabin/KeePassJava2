package org.linguafranca.pwdb.kdbx;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static org.linguafranca.pwdb.kdbx.Util.listKdbxHeaderProperties;
import static org.linguafranca.pwdb.kdbx.Util.listXml;

public class UtilTest {
    /**
     * List Database Encryption Characteristics
     */
    @Test
    public void listKdbxHeaderParams () throws IOException {
        PrintWriter writer = new PrintWriter(System.out);
        listKdbxHeaderProperties("test123.kdbx", writer);
        listKdbxHeaderProperties("V4-AES-AES.kdbx", writer);
        listKdbxHeaderProperties("V4-AES-Argon2.kdbx", writer);
        listKdbxHeaderProperties("V4-ChaCha20-AES.kdbx", writer);
        listKdbxHeaderProperties("V4-ChaCha20-Argon2-Attachment.kdbx", writer);
        writer.flush();
    }

    @Test
    public void listHeaderPropertiesAndXml() throws IOException {
        PrintWriter writer = new PrintWriter(System.out);
        listKdbxHeaderProperties("V4-AES-Argon2-CustomIcon.kdbx", writer);
        listXml("V4-AES-Argon2-CustomIcon.kdbx", "123".getBytes(), writer);
        writer.flush();
    }
}
