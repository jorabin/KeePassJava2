package org.linguafranca.pwdb.security;

import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.ChaCha7539Engine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * ChaCha20 may be used for the inner stream as well as for the underlying database encryption scheme
 * <p>
 * A singleton
 */
public class ChaCha implements CipherAlgorithm {

    private static final UUID CHACHA_CIPHER = UUID.fromString("d6038a2b-8b6f-4cb5-a524-339a31dbb59a");

    // hide constructor to enforce singleton
    private ChaCha(){}
    private static final ChaCha instance = new ChaCha();

    public static ChaCha getInstance() {
        return instance;
    }

    @Override
    public UUID getCipherUuid() {
        return CHACHA_CIPHER;
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream encryptedInputStream, byte[] key, byte[] iv) {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(key), iv);
        StreamCipher cipher = new ChaCha7539Engine();
        cipher.init(false, keyAndIV);
        return new CipherInputStream(encryptedInputStream, cipher);
    }

    @Override
    public OutputStream getEncryptedOutputStream(OutputStream decryptedOutputStream, byte[] key, byte[] iv) {
        final ParametersWithIV keyAndIV = new ParametersWithIV(new KeyParameter(key), iv);
        StreamCipher cipher = new ChaCha7539Engine();
        cipher.init(true, keyAndIV);
        return new CipherOutputStream(decryptedOutputStream, cipher);
    }
}
