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

package org.linguafranca.pwdb.security;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.StreamEncryptor;
import org.linguafranca.pwdb.kdbx.StreamEncryptor.*;

import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;

/**
 * @author jo
 */
public class StreamEncryptionTest {

    @Test
    public void salsa20() {
        byte[] key = SecureRandom.getSeed(32);
        StreamEncryptor ss = new Salsa20(key);
        StreamEncryptor tt = new Salsa20(key);

        verifyTwoWay(ss, tt);
    }

    @Test
    public void chacha20() {
        byte[] key = SecureRandom.getSeed(32);
        StreamEncryptor ss = new ChaCha20(key);
        StreamEncryptor tt = new ChaCha20(key);

        verifyTwoWay(ss, tt);
    }

    private void verifyTwoWay(StreamEncryptor ss, StreamEncryptor tt) {

        byte[] e = ss.encrypt("new secret".getBytes());
        byte[] f = ss.encrypt("secret 2".getBytes());


        String s1 = new String(tt.encrypt(e));
        String t1 = new String(tt.encrypt(f));

        assertEquals("new secret", s1);
        assertEquals("secret 2", t1);

        System.out.println(s1);
        System.out.println(t1);
    }

}