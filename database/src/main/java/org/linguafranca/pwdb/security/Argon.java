package org.linguafranca.pwdb.security;

import java.security.MessageDigest;
import java.util.UUID;

import static com.kosprov.jargon2.api.Jargon2.*;
import static org.linguafranca.pwdb.security.Argon.ArgonParameterKeys.ArgonKeys.*;
import static org.linguafranca.pwdb.security.Encryption.getSha256MessageDigestInstance;


/**
 * KDBX V4 files may use Argon2 for key derivation.
 *
 * @author jo
 */
public class Argon {

    /** UUID indicating that Argon is being used as the KDF */
    public static final UUID argon2_kdf = UUID.fromString("EF636DDF-8C29-444B-91F7-A9A403E30A0C");

    /** keys into the variant dictionary supplied as a KDBX header */
    public static class ArgonParameterKeys {
        @SuppressWarnings("unused")
        public static class ArgonKeys {
            static final String paramSalt = "S"; // Byte[]
            static final String paramParallelism = "P"; // UInt32
            static final String paramMemory = "M"; // UInt64
            static final String paramIterations = "I"; // UInt64
            static final String paramVersion = "V"; // UInt32
            static final String paramSecretKey = "K"; // Byte[]
            static final String paramAssocData = "A"; // Byte[]

            static final int minVersion = 0x10;
            static final int maxVersion = 0x13;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static byte [] getArgonFinalKeyDigest(byte [] digest, byte [] masterSeed, VariantDictionary argonParameterKeys) {

        byte bVersion = argonParameterKeys.get(paramVersion).asByteArray()[0];
        Version version = bVersion == 0x13 ? Version.V13 : Version.V10;
        byte [] salt = argonParameterKeys.get(paramSalt).asByteArray();
        int parallelism = argonParameterKeys.get(paramParallelism).asInteger();
        int memoryCost = (int) argonParameterKeys.get(paramMemory).asLong();
        int timeCost = (int) argonParameterKeys.get(paramIterations).asLong();

        // Configure the hasher
        Hasher hasher = jargon2Hasher()
                .type(Type.ARGON2d)
                .version(version)
                .salt(salt)
                .parallelism(parallelism)
                .memoryCost(memoryCost/1024) // block size 1024
                .timeCost(timeCost)
                .hashLength(32);

        // do the hash
        byte [] hash = hasher.password(digest).rawHash();

        // return digest of master seed and hash
        MessageDigest md = getSha256MessageDigestInstance();
        md.update(masterSeed);
        return md.digest(hash);
    }
}
