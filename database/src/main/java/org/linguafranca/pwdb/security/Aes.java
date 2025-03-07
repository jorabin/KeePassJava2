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

package org.linguafranca.pwdb.security;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;

import static org.linguafranca.pwdb.security.Aes.KdfKeys.ParamRounds;
import static org.linguafranca.pwdb.security.Aes.KdfKeys.ParamSeed;
import static org.linguafranca.pwdb.security.Encryption.getSha256MessageDigestInstance;

/**
 * AES may be used for Key Derivation and also as the underlying stream cipher
 * <p>
 * The class is a singleton
 */
public class Aes implements CipherAlgorithm, KeyDerivationFunction {

    /** UUID specifying that AES is to be used as the Key Derivation Function in KDBX */
    private static final UUID KDF = UUID.fromString("C9D9F39A-628A-4460-BF74-0D08C18A4FEA");
    private static final String name = "AES";
    private static final VariantDictionary kdfParameters = new VariantDictionary((short) 1);
    static {
        kdfParameters.putUuid("$UUID", KDF);
        kdfParameters.putULong(ParamRounds, 6000L);
        kdfParameters.putByteArray(ParamSeed, SecureRandom.getSeed(32));
    }

    /** v4 variant dictionary keys for use of AES as the KDF */
    public static class KdfKeys {
        public static final String ParamRounds = "R"; // UInt64
        public static final String ParamSeed = "S"; // Byte[32]
    }

    /** UUID specifying that AES is to be used as the Cipher in KDBX */
    private static final UUID CIPHER = UUID.fromString("31C1F2E6-BF71-4350-BE58-05216AFC5AFF");

    /** hide constructor */
    private Aes () { }
    private static final Aes instance = new Aes();

    public static Aes getInstance () {
        return instance;
    }

    /**
     * get a copy of the Aes Variant dictionary
     * @return a copy
     */
    public VariantDictionary createKdfParameters() {
        return kdfParameters.copy();
    }


    @Override
    public UUID getCipherUuid() {
        return CIPHER;
    }

    public String getName(){
        return name;
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream encryptedInputStream, byte[] key, byte[] iv) {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(key), iv);
        PaddedBufferedBlockCipher pbbc = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        pbbc.init(false, keyAndIV);
        return new CipherInputStream(encryptedInputStream, pbbc);
    }

    @Override
    public OutputStream getEncryptedOutputStream(OutputStream decryptedOutputStream, byte[] key, byte[] iv) {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(key), iv);
        PaddedBufferedBlockCipher pbbc = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        pbbc.init(true, keyAndIV);
        return new CipherOutputStream(decryptedOutputStream, pbbc);
    }

    @Override
    public UUID getKdfUuid() {
        return KDF;
    }

    @Override
    public byte[] getTransformedKey(byte[] key, VariantDictionary transformParams) {
        return getTransformedKey(key,
                transformParams.mustGet(ParamSeed).asByteArray(),
                transformParams.mustGet(ParamRounds).asLong());
    }

    /**
     * Simplified version for KDBX V3
     * @param key the composite key
     * @param transformSeed the seed
     * @param transformRounds number of rounds
     * @return a transformed key
     */
    public static byte[] getTransformedKey(byte[] key, byte [] transformSeed, long transformRounds) {

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
