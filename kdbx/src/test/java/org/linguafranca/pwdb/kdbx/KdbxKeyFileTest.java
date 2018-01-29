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
import org.linguafranca.pwdb.kdbx.KdbxSerializer;
import org.linguafranca.pwdb.Credentials;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author jo
 */
public class KdbxKeyFileTest {

    @Test
    public void testLoad() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.key");
        byte[] key = KdbxKeyFile.load(inputStream);
        assertNotNull(key);
        assertEquals(32, key.length);
    }

    /*
    Test for empty password
     */
    @Test
    public void testEmptyPasswordCreds() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("EmptyPassword.kdbx");
        Credentials credentials = new KdbxCreds(new byte[0]);
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        byte[] buffer = new byte[1024];
        while ( decryptedInputStream.available() > 0) {
            int read = decryptedInputStream.read(buffer);
            if (read == -1) break;
            System.out.write(buffer, 0, read);
        }
    }


    /**
     Test for empty password with key
     */
    @Test
    public void testEmptyPasswordKeyCreds() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("EmptyPasswordWithKey.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("EmptyPasswordWithKey.key");
        Credentials credentials = new KdbxCreds(new byte[0], inputStreamKeyFile);
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        byte[] buffer = new byte[1024];
        while ( decryptedInputStream.available() > 0) {
            int read = decryptedInputStream.read(buffer);
            if (read == -1) break;
            System.out.write(buffer, 0, read);
        }
    }

    /**
     Test for no master password
     */
    @Test
    public void testNoPasswordKeyCreds() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("NoPasswordWithKey.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("NoPasswordWithKey.key");
        Credentials credentials = new KdbxCreds(inputStreamKeyFile);
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        byte[] buffer = new byte[1024];
        while ( decryptedInputStream.available() > 0) {
            int read = decryptedInputStream.read(buffer);
            if (read == -1) break;
            System.out.write(buffer, 0, read);
        }
    }

    /*
    Test for empty password
     */
    @Test
    public void testEmptyPassword() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("EmptyPassword.kdbx");
        Credentials credentials = new KdbxCreds(new byte[0]);
        InputStream decryptedInputStream = KdbxSerializer.createUnencryptedInputStream(credentials, new KdbxHeader(), inputStream);
        byte[] buffer = new byte[1024];
        while ( decryptedInputStream.available() > 0) {
            int read = decryptedInputStream.read(buffer);
            if (read == -1) break;
            System.out.write(buffer, 0, read);
        }
    }
}