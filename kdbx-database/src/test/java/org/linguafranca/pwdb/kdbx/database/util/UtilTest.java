/*
 * Copyright (c) 2025. Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.linguafranca.pwdb.kdbx.database.util;


import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.format.KdbxCredentials;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import static org.linguafranca.pwdb.kdbx.jackson.util.Util.*;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class UtilTest {

    OutputStream outputStream = getTestPrintStream();

    @Test
    public void listDatabaseTest() throws IOException {
        listDatabase("V3-CustomIcon.kdbx", new KdbxCredentials("123".getBytes()), outputStream);
    }

    @Test
    public void listXmlTest() throws IOException {
        PrintWriter writer = new PrintWriter(outputStream);
        listXml("Database-4.1-123.kdbx", new KdbxCredentials("123".getBytes()), writer);
        writer.flush();
    }

    @Test
    public void listXmlTest2() throws IOException {
        PrintWriter writer = new PrintWriter(outputStream);
        listXml("issue-70/test2.kdbx", new KdbxCredentials("KeePassJava2".getBytes()), writer);
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
        listXml("V4-AES-Argon2-CustomIcon.kdbx", new KdbxCredentials("123".getBytes()), writer);
        writer.flush();
    }
}
