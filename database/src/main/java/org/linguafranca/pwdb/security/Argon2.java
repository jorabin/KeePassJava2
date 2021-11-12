package org.linguafranca.pwdb.security;

import java.util.UUID;

import static com.kosprov.jargon2.api.Jargon2.*;
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
    public byte[] getTransformedKey(byte[] digest, VariantDictionary argonParameterKeys) {
        byte bVersion = argonParameterKeys.mustGet(paramVersion).asByteArray()[0];
        Version version = bVersion == 0x13 ? Version.V13 : Version.V10;
        byte[] salt = argonParameterKeys.mustGet(paramSalt).asByteArray();
        int parallelism = argonParameterKeys.mustGet(paramParallelism).asInteger();
        int memoryCost = (int) argonParameterKeys.mustGet(paramMemory).asLong();
        int timeCost = (int) argonParameterKeys.mustGet(paramIterations).asLong();

        // Configure the hasher
        Hasher hasher = jargon2Hasher()
                .type(Type.ARGON2d)
                .version(version)
                .salt(salt)
                .parallelism(parallelism)
                .memoryCost(memoryCost / 1024) // block size 1024
                .timeCost(timeCost)
                .hashLength(32);

        // do the hash
        return hasher.password(digest).rawHash();
    }
}
