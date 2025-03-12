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

package org.linguafranca.pwdb.kdbx;

import com.google.common.io.CharStreams;
import com.google.common.io.LittleEndianDataInputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.linguafranca.pwdb.hashedblock.HmacBlockInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * test decryption of various kinds
 */
public class KdbxHeaderTest {
    static PrintStream printStream = getTestPrintStream();

    @Disabled // can be used for detailed HMAC debugging
    public void getHmacStream() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        printStream.println("Version " + header.getVersion());
        KdbxCredentials creds = new KdbxCredentials("123".getBytes());
        assert inputStream != null;
        KdbxSerializer.readOuterHeaderVerification(header, creds, new LittleEndianDataInputStream(inputStream));
        HmacBlockInputStream hmacBlockInputStream = new HmacBlockInputStream(header.getHmacKey(creds), inputStream, true);
        printStream.println(CharStreams.toString(new InputStreamReader(hmacBlockInputStream, StandardCharsets.UTF_8)));
    }

    // check the correct version
    @Test
    public void loadAesAesHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-AES.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }

    // check correct version
    @Test
    public void loadAesArgonHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }

    @Test
    public void loadChaChaHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-ChaCha20-AES.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }
    @Test
    public void loadChaChaArgonHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-ChaCha20-Argon2-Attachment.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }

}