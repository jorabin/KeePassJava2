package org.linguafranca.pwdb.security;

import java.security.SecureRandom;
import java.util.UUID;

import static com.kosprov.jargon2.api.Jargon2.*;
import static org.linguafranca.pwdb.security.Argon2.KdfKeys.*;

/**
 * KDBX V4 files may use Argon2d/id for key derivation.
 * <p>
 * A singleton
 */
public class Argon2 implements KeyDerivationFunction {

    /**
     * UUID indicating that Argon2d is being used as the KDF
     * TODO: add support for Argon2id
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

    private static VariantDictionary kdfParameters = new VariantDictionary((short) 1);
    static {
        kdfParameters.putUuid("$UUID", argon2_kdf);
        kdfParameters.putInt(paramVersion, 19); // hex representation of 13, which itself represents latest Argon version of 1.3
        kdfParameters.putLong(paramIterations, 2);
        kdfParameters.putLong(paramMemory, 67108864L); // this amount of bytes divided by 1024 is 65536 kibi bytes, which is equal to 64 MB
        kdfParameters.putInt(paramParallelism, 2);
        kdfParameters.putByteArray(paramSalt, SecureRandom.getSeed(32));
        
    }
    
    /**
     * keys into the variant dictionary supplied as a KDBX header
     */
    @SuppressWarnings("WeakerAccess")
    public static class KdfKeys {
        public static final String paramVersion = "V"; // UInt32
        public static final String paramIterations = "I"; // UInt64
        public static final String paramMemory = "M"; // UInt64
        public static final String paramParallelism = "P"; // UInt32
        public static final String paramSalt = "S"; // Byte[]
        
        // These don't seem to be used (?)
        //static final String paramSecretKey = "K"; // Byte[]
        //static final String paramAssocData = "A"; // Byte[]
    }

    @Override
    public UUID getKdfUuid() {
        return argon2_kdf;
    }
    
    /**
     * get a copy of the Argon2 VariantDictionary
     * @return a copy
     */
    public static VariantDictionary createKdfParameters() {
        return kdfParameters.copy();
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
                .memoryCost(memoryCost / 1024) // block size of 1024
                .timeCost(timeCost)
                .hashLength(32);

        // do the hash
        return hasher.password(digest).rawHash();
    }
}
