package org.linguafranca.pwdb.security;

import org.spongycastle.crypto.StreamCipher;
import org.spongycastle.crypto.engines.ChaCha7539Engine;

/**
 * ChaCha20 may be used for the inner stream as well as for the underlying database encryption scheme
 *
 * @author jo
 */
public class ChaCha {

    public static StreamCipher getCipher () {
        return new ChaCha7539Engine();
    }
}
