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
import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxKeyFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.linguafranca.pwdb.format.KdbxSerializer.createUnencryptedInputStream;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * tests reading of kdbx with various combinations of key file and password
 */
public class KdbxKeyFileTest {

    static PrintStream printStream = getTestPrintStream();

    private static void toConsole(InputStream is) throws IOException {
        printStream.println(CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8)));
    }

    /**
     * Test that we can load a key file and get a 32 byte base64 encoded value back
     */
    @Test
    public void testLoad() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.key");
        byte[] key = KdbxKeyFile.load(inputStream);
        assertNotNull(key);
        assertEquals(32, key.length);
    }

    /**
     * Test that we can read a file with empty password
     */
    @Test
    public void testEmptyPasswordCreds() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("EmptyPassword.kdbx");
        Credentials credentials = new KdbxCredentials(new byte[0]);
        InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        toConsole(decryptedInputStream);
    }

    /**
     * Test for empty password with key file
     */
    @Test
    public void testEmptyPasswordKeyCreds() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("EmptyPasswordWithKey.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("EmptyPasswordWithKey.key");
        assert inputStreamKeyFile != null;
        Credentials credentials = new KdbxCredentials(new byte[0], inputStreamKeyFile);
        InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        toConsole(decryptedInputStream);
    }

    /**
     * Test for no master password with key
     */
    @Test
    public void testNoPasswordKeyCreds() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("NoPasswordWithKey.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("NoPasswordWithKey.key");
        assert inputStreamKeyFile != null;
        Credentials credentials = new KdbxCredentials(inputStreamKeyFile);
        InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        toConsole(decryptedInputStream);
    }

    /**
     * Test for empty password
     */
    @Test
    public void testEmptyPassword() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("EmptyPassword.kdbx");
        Credentials credentials = new KdbxCredentials(new byte[0]);
        InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        toConsole(decryptedInputStream);
    }

    /**
     * Test the hash in KeyFile (v2.0)
     */
    @Test
    public void testSignedKeyFile() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("kdbx_hash_test.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("kdbx_hash_test.keyx");
        Credentials credentials = new KdbxCredentials("123".getBytes(), inputStreamKeyFile);
        InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        toConsole(decryptedInputStream);
    }

    /**
     * Test hash fails in KeyFile (v2.0)
     */
    @Test
    public void testSignatureFails() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("kdbx_hash_test.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("kdbx_hash_test_wrong_hash" +
                ".keyx");
        Throwable throwable = assertThrows(RuntimeException.class, () -> {
            Credentials credentials = new KdbxCredentials("123".getBytes(), inputStreamKeyFile);
            InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
            toConsole(decryptedInputStream);
        });
    }

    /**
     * Test KDBX with random KeyFile and key
     */
    @Test
    public void testKeyFileRandom() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("kdb_with_random_file.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("random_file");
        Credentials credentials = new KdbxCredentials("123".getBytes(), inputStreamKeyFile);
        InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        toConsole(decryptedInputStream);
    }

    /**
     * Test KDBX with 64 bytes hex KeyFile and key
     */
    @Test
    public void testKeyFileHex64() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("kdbx_keyfile64.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("keyfile64");
        Credentials credentials = new KdbxCredentials("123".getBytes(), inputStreamKeyFile);
        InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        toConsole(decryptedInputStream);
    }

    /**
     * Test KDBX with 32 bytes KeyFile and key
     */
    @Test
    public void testKeyFile32() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("kdbx_keyfile32.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("keyfile32");
        Credentials credentials = new KdbxCredentials("123".getBytes(), inputStreamKeyFile);
        InputStream decryptedInputStream = createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        toConsole(decryptedInputStream);
    }
}
