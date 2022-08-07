package org.linguafranca.pwdb.security;

import java.security.SecureRandom;
import java.util.UUID;

import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.kosprov.jargon2.api.Jargon2.Type;
import com.kosprov.jargon2.api.Jargon2.Version;
import static org.linguafranca.pwdb.security.Argon2id.KdfKeys.*;
import static com.kosprov.jargon2.api.Jargon2.*;

/**
 * KDBX V4 files may use Argon2id for key derivation.
 * <p>
 * A singleton
 */

// Difference between this and Argon2d is minor but the lack of an ability to modify kdfParameters directly
// makes it hard to account for changes; once kdfParameters is initialized, user can only modify a copy of it.
// Encryption.java does not check copies, so it can only account for what was in kdfParameters when initialized.
// TODO: find a way to use Argon2d for both d/id versions
public class Argon2id implements KeyDerivationFunction{

	/**
     * UUID indicating that Argon2id is being used as the KDF
     */
    private static final UUID argon2id_kdf = UUID.fromString("9E298B19-56DB-4773-B23D-FC3EC6F0A1E6");

    /**
     * hide constructor
     */
    private Argon2id() {
    }

    private static final Argon2id instance = new Argon2id();

    public static Argon2id getInstance() {
        return instance;
    }

    private static VariantDictionary kdfParameters = new VariantDictionary((short) 1);
    static {
        kdfParameters.putUuid("$UUID", argon2id_kdf);
        kdfParameters.putInt(paramVersion, 19);
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
        return argon2id_kdf;
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
                .type(Type.ARGON2id)
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
