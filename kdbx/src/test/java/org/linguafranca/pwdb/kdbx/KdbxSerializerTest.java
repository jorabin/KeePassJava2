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

import org.junit.Test;
import org.linguafranca.pwdb.Credentials;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/**
 * @author jo
 */
public class KdbxSerializerTest {

    @Test
    public void testGetPlainTextInputStream() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Attachment.kdbx");
        Credentials credentials = new KdbxCreds("123".getBytes());
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
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
        Credentials credentials = new KdbxCreds("123".getBytes());
        OutputStream outputStream = KdbxSerializer.createEncryptedOutputStream(credentials, new KdbxHeader(), testStream);

        outputStream.write("Hello World\n".getBytes());
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), new FileInputStream(tempFile));
        Scanner scanner = new Scanner(inputStream);
        assertEquals("Hello World", scanner.nextLine());
    }
}