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
     * Transform a key using this key derivation function
     *
     * @param key             the key to transform
     * @param transformParams the parameters defining the way the transformation is to be carried out
     * @return a transformed key
     */
    byte[] getTransformedKey(byte[] key, VariantDictionary transformParams);

    /**
     * Create default KDF parameters
     */
    VariantDictionary createKdfParameters();

    /**
     * Get a name
     */
    String getName();
}
