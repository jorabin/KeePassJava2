package org.linguafranca.keepass.encryption;

import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.linguafranca.keepass.db.DatabaseProvider;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

/**
 * @author jo
 */
public class Salsa20Encryption implements DatabaseProvider.Encryption {
    private final Salsa20Engine decrypt;
    private final Salsa20Engine encrypt;
    private final byte[] key;

    private static final byte[] SALSA20_IV = DatatypeConverter.parseHexBinary("E830094B97205D2A");

    public static Salsa20Engine createSalsa20(boolean forEncryption, byte[] key) {
        MessageDigest md = Encryption.getMessageDigestInstance();
        KeyParameter keyParameter = new KeyParameter(md.digest(key));
        ParametersWithIV ivParameter = new ParametersWithIV(keyParameter, SALSA20_IV);
        Salsa20Engine engine = new Salsa20Engine();
        engine.init(forEncryption, ivParameter);
        return engine;
    }

    public Salsa20Encryption(byte[] key) {
        this.key = key;
        decrypt = createSalsa20(false, key);
        encrypt = createSalsa20(true, key);
    }

    @Override
    public byte[] getKey() {
        return key;
    }

    @Override
    public byte[] decrypt(byte[] encryptedText) {
        byte[] output = new byte[encryptedText.length];
        decrypt.processBytes(encryptedText, 0, encryptedText.length, output, 0);
        return output;
    }

    @Override
    public byte[] encrypt(byte[] decryptedText) {
        byte[] output = new byte[decryptedText.length];
        encrypt.processBytes(decryptedText, 0, decryptedText.length, output, 0);
        return output;
    }
}
