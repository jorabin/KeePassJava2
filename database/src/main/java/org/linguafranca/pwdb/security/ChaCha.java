package org.linguafranca.pwdb.security;

import org.spongycastle.crypto.StreamCipher;
import org.spongycastle.crypto.engines.ChaCha7539Engine;

/**
 * @author jo
 */
public class ChaCha {

    public static StreamCipher getCipher () {
        return new ChaCha7539Engine();
    }
}
