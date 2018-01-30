package org.linguafranca.pwdb.security;

import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;

import java.security.MessageDigest;
import java.util.UUID;

import static org.linguafranca.pwdb.security.Encryption.getSha256MessageDigestInstance;

/**
 * AES may be used for Key Derivation and aslo as the underlying stream cipher
 *
 * @author jo
 */
public class Aes {

    /** UUID specifying that AES is to be used as the Key Derivation Function */
    public static final UUID KDF = UUID.fromString("C9D9F39A-628A-4460-BF74-0D08C18A4FEA");

    /** v4 variant dictionary keys for use of AES as the KDF */
    public static class KdfKeys {
        public static final String ParamRounds = "R"; // UInt64
        public static final String ParamSeed = "S"; // Byte[32]
    }

    public static PaddedBufferedBlockCipher getCipher() {
        return new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
    }

    public  static byte [] getTransformedKey (byte [] key, byte [] transformSeed, long transformRounds) {

        AESEngine engine = new AESEngine();
        engine.init(true, new KeyParameter(transformSeed));

        // copy input key
        byte[] transformedKey = new byte[key.length];
        System.arraycopy(key, 0, transformedKey, 0, transformedKey.length);

        // transform rounds times
        for (long rounds = 0; rounds < transformRounds; rounds++) {
            engine.processBlock(transformedKey, 0, transformedKey, 0);
            engine.processBlock(transformedKey, 16, transformedKey, 16);
        }

        MessageDigest md = getSha256MessageDigestInstance();
        return md.digest(transformedKey);
    }
}
