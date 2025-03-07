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

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.security.SecureRandom;
import java.util.UUID;

import static org.linguafranca.pwdb.security.Argon2.VariantDictionaryKeys.*;


/**
 * KDBX V4 files may use Argon2 for key derivation.
 * <p>
 * A singleton
 */
public class Argon2 implements KeyDerivationFunction {

    /**
     * UUID indicating that Argon is being used as the KDF
     */
    private static final UUID argon2_kdf = UUID.fromString("EF636DDF-8C29-444B-91F7-A9A403E30A0C");

    private static final String name = "Argon2";

    /**
     * hide constructor
     */
    private Argon2() {
    }

    private static final Argon2 instance = new Argon2();

    public static Argon2 getInstance() {
        return instance;
    }


    /**
     * keys into the variant dictionary supplied as a KDBX header
     */
    @SuppressWarnings("WeakerAccess")
    public static class VariantDictionaryKeys {
        static final String paramSalt = "S"; // Byte[]
        static final String paramParallelism = "P"; // UInt32
        static final String paramMemory = "M"; // UInt64
        static final String paramIterations = "I"; // UInt64
        static final String paramVersion = "V"; // UInt32
        static final String paramSecretKey = "K"; // Byte[]
        static final String paramAssocData = "A"; // Byte[]
    }


    @Override
    public UUID getKdfUuid() {
        return argon2_kdf;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getTransformedKey(byte[] digest, VariantDictionary argonParameterKeys) {
        int version = argonParameterKeys.mustGet(paramVersion).asInteger();
        byte[] salt = argonParameterKeys.mustGet(paramSalt).asByteArray();
        int parallelism = argonParameterKeys.mustGet(paramParallelism).asInteger();
        int memoryCost = (int) argonParameterKeys.mustGet(paramMemory).asLong();
        int timeCost = (int) argonParameterKeys.mustGet(paramIterations).asLong();

        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_d)
                .withVersion(version)
                .withIterations(timeCost)
                .withMemoryAsKB(memoryCost/1024)
                .withParallelism(parallelism)
                .withSalt(salt);

        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(builder.build());
        byte[] result = new byte[32];
        gen.generateBytes(digest, result, 0, result.length);
        return result;
    }

    static final SecureRandom random = new SecureRandom();

    @Override
    public VariantDictionary createKdfParameters() {
        VariantDictionary vd = new VariantDictionary((short) 1);
        vd.putUInt("P", 2);
        vd.putUInt("V", 19);
        vd.putULong("I", 2);
        vd.putULong("M", 64 * 1024 * 1024);
        vd.putUuid("$UUID", Argon2.argon2_kdf);
        vd.put("S", VariantDictionary.EntryType.ARRAY, random.generateSeed(32));
        return vd;
    }
}
