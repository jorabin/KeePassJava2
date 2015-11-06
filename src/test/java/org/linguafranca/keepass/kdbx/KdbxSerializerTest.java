package org.linguafranca.keepass.kdbx;

import org.junit.Test;
import org.linguafranca.keepass.kdbx.KdbxHeader;
import org.linguafranca.keepass.kdbx.KdbxSerializer;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/**
 * @author jo
 */
public class KdbxSerializerTest {

    @Test
    public void testGetPlainTextInputStream() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream("123", new KdbxHeader(), inputStream);
        byte[] buffer = new byte[1024];
        while ( decryptedInputStream.available() > 0) {
            int read = decryptedInputStream.read(buffer);
            if (read == -1) break;
            System.out.write(buffer, 0, read);
        }
    }

    @Test
    public void testCypherTextOutputStream() throws Exception {
        File tempFile = File.createTempFile("test", "test");
        OutputStream testStream = new FileOutputStream(tempFile);
        OutputStream outputStream = KdbxSerializer.createEncryptedOutputStream("123", new KdbxHeader(), testStream);

        outputStream.write("Hello World\n".getBytes());
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = KdbxSerializer.createUnencryptedInputStream("123", new KdbxHeader(), new FileInputStream(tempFile));
        Scanner scanner = new Scanner(inputStream);
        assertEquals("Hello World", scanner.nextLine());
    }
}