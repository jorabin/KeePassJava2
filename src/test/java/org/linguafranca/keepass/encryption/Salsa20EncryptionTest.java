package org.linguafranca.keepass.encryption;

import org.junit.Test;

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