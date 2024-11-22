package org.linguafranca.pwdb.kdbx;

import org.junit.Test;
import org.linguafranca.pwdb.format.KdbxCreds;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import static org.linguafranca.pwdb.kdbx.Util.*;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class UtilTest {

    OutputStream outputStream = getTestPrintStream();
    @Test public void listDatabaseTest() throws IOException {
        listDatabase("V3-CustomIcon.kdbx", new KdbxCreds("123".getBytes()), outputStream);
    }

    @Test
    public void listXmlTest() throws IOException {
        PrintWriter writer = new PrintWriter(outputStream);
        listXml("Database-4.1-123.kdbx", new KdbxCreds("123".getBytes()), writer);
        writer.flush();
    }

    @Test
    public void listXmlTest2() throws IOException {
        PrintWriter writer = new PrintWriter(outputStream);
        listXml("issue-70/test2.kdbx", new KdbxCreds("KeePassJava2".getBytes()), writer);
        writer.flush();
    }
    /**
     * List Database Encryption Characteristics
     */
    @Test
    public void listKdbxHeaderParams () throws IOException {
        PrintWriter writer = new PrintWriter(outputStream);
        listKdbxHeaderProperties("test123.kdbx", writer);
        listKdbxHeaderProperties("V4-AES-AES.kdbx", writer);
        listKdbxHeaderProperties("V4-AES-Argon2.kdbx", writer);
        listKdbxHeaderProperties("V4-ChaCha20-AES.kdbx", writer);
        listKdbxHeaderProperties("V4-ChaCha20-Argon2-Attachment.kdbx", writer);
        writer.flush();
    }

    @Test
    public void listHeaderPropertiesAndXml() throws IOException {
        PrintWriter writer = new PrintWriter(outputStream);
        listKdbxHeaderProperties("V4-AES-Argon2-CustomIcon.kdbx", writer);
        listXml("V4-AES-Argon2-CustomIcon.kdbx", new KdbxCreds("123".getBytes()), writer);
        writer.flush();
    }
}
