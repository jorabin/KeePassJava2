package org.linguafranca.pwdb.security;

import java.util.UUID;

/**
 * Interface defining a Key Derivation Function
 */
public interface KeyDerivationFunction {
    /**
     * The UUID of this key derivation function
     */
    UUID getKdfUuid();

    /**
     * Transform a key using this key derivation faunction
     *
     * @param key             the key to transform
     * @param transformParams the parameters defining the way the tranformation is to be carried out
     * @return a transformed key
     */
    byte[] getTransformedKey(byte[] key, VariantDictionary transformParams);
}
