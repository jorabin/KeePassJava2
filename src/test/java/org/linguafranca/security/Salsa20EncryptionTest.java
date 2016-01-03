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

package org.linguafranca.security;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.Salsa20Encryption;

import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;

/**
 * @author jo
 */
public class Salsa20EncryptionTest {

    @Test
    public void encrypt() {
        byte[] key = SecureRandom.getSeed(32);

        Salsa20Encryption ss = new Salsa20Encryption(key);
        byte[] e = ss.encrypt("new secret".getBytes());
        byte[] f = ss.encrypt("secret 2".getBytes());


        Salsa20Encryption tt = new Salsa20Encryption(key);
        String s1 = new String(tt.encrypt(e));
        String t1 = new String(tt.encrypt(f));

        assertEquals("new secret", s1);
        assertEquals("secret 2", t1);

        System.out.println(s1);
        System.out.println(t1);


    }

}