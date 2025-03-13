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

package org.linguafranca.pwdb.kdbx.database.validation;



import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.format.Helpers;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.kdbx.jackson.util.Util;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Review  <a href="https://github.com/jorabin/KeePassJava2/issues/33">Issue-33</a>
 */
public class Issue33Test {

    public static final String TEST_RESOURCE = "V4-AES-Argon2-CustomIcon.kdbx";
    public static final String TEST_OUTPUT_DIR = "testOutput";
    public static final File TEST_XML_FILE = Paths.get(TEST_OUTPUT_DIR,"Issue33Source.xml").toFile();
    public static final KdbxCredentials CREDENTIALS = new KdbxCredentials("123".getBytes());


    InputStream inputStream;

    @BeforeAll
    public static void listXml() throws IOException {
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
        Util.listXml(TEST_RESOURCE, CREDENTIALS, new PrintWriter(TEST_XML_FILE));
        Helpers.isV4.set(true);
    }

    @BeforeEach
    public void refreshInputStream() {
        inputStream = this.getClass().getClassLoader().getResourceAsStream(TEST_RESOURCE);
    }

    @Test
    public void testJacksonDatabase() throws IOException {
        KdbxDatabase database = KdbxDatabase.load(CREDENTIALS, inputStream);
        database.save(new StreamFormat.None(), new Credentials.None(), Files.newOutputStream(Paths.get(TEST_OUTPUT_DIR, "Issue33Jackson.xml")));
    }
}
