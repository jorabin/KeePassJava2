/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.pwdb.kdbx;

import com.google.common.io.CharStreams;
import org.junit.Test;
import org.linguafranca.pwdb.Credentials;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/**
 * test operation of kdbx
 */
public class KdbxSerializerTest {

    /**
     * Test that we can read a kdbx v3 file and list the XML to console
     */
    @Test
    public void testGetPlainTextInputStream() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Attachment.kdbx");
        Credentials credentials = new KdbxCreds("123".getBytes());
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        System.out.println(CharStreams.toString(new InputStreamReader(decryptedInputStream, StandardCharsets.UTF_8)));
    }

    /**
     * Test that we can read a kdbx v4 file and list the XML to console
     */
    @Test
    public void testGetPlainTextInputStream2() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-ChaCha20-AES.kdbx");
        Credentials credentials = new KdbxCreds("123".getBytes());
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        System.out.println(CharStreams.toString(new InputStreamReader(decryptedInputStream, StandardCharsets.UTF_8)));
    }


    /**
     * Test that we can write a KDBX 3 file containing "Hello World"
     * and then read it back to get the same content
     */
    @Test
    public void testCypherTextOutputStream() throws Exception {
        File tempFile = File.createTempFile("test", "test");
        OutputStream testStream = new FileOutputStream(tempFile);
        Credentials credentials = new KdbxCreds("123".getBytes());
        OutputStream outputStream = KdbxSerializer.createEncryptedOutputStream(credentials, new KdbxHeader(), testStream);

        outputStream.write("Hello World\n".getBytes());
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), new FileInputStream(tempFile));
        Scanner scanner = new Scanner(inputStream);
        assertEquals("Hello World", scanner.nextLine());
    }

    /**
     * Test that we can write a KDBX 4 file containing "Hello World"
     * and then read it back to get the same content
     */
    @Test
    public void testCypherTextOutputStream2() throws Exception {
        File tempFile = File.createTempFile("test", "test");
        OutputStream testStream = new FileOutputStream(tempFile);
        Credentials credentials = new KdbxCreds("123".getBytes());
        KdbxHeader kdbxHeader = new KdbxHeader(4);
        OutputStream outputStream = KdbxSerializer.createEncryptedOutputStream(credentials, kdbxHeader, testStream);

        outputStream.write("Hello World\n".getBytes());
        outputStream.flush();
        outputStream.close();

        KdbxHeader kdbxHeader1 = new KdbxHeader();
        try (InputStream inputStream = KdbxSerializer.createUnencryptedInputStream(credentials, kdbxHeader1, new FileInputStream(tempFile))){
            assertEquals(4, kdbxHeader1.getVersion());
            Scanner scanner = new Scanner(inputStream);
            assertEquals("Hello World", scanner.nextLine());
        }
    }
}