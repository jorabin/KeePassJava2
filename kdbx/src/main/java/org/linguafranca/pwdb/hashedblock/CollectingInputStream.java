package org.linguafranca.pwdb.hashedblock;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class allows the forwarding and collection of read bytes as a buffer - e.g. to provide for HMAC operations
 *
 * @author jo
 */
public class CollectingInputStream extends FilterInputStream {

    private ByteArrayOutputStream collectedBytes = new ByteArrayOutputStream();
    private boolean collecting = true;

    protected CollectingInputStream(InputStream in) {
        this(in, true);
    }

    public CollectingInputStream(InputStream in, boolean collecting) {
        super(in);
        this.collecting = collecting;
    }

    @Override
    public int read() throws IOException {
        int result =  super.read();
        if (collecting && result != -1) {
            collectedBytes.write(result);
        }
        return result;
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (collecting && result != -1) {
            collectedBytes.write(b, off, result);
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        return super.skip(n);
    }

    public byte [] getCollectedBytes() {
        return collectedBytes.toByteArray();
    }

    public boolean isCollecting() {
        return collecting;
    }

    public void setCollecting(boolean collecting) {
        this.collecting = collecting;
    }
}
