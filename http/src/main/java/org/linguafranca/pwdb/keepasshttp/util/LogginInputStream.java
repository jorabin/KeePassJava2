package org.linguafranca.pwdb.keepasshttp.util;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jo
 */
public class LogginInputStream extends InputStream {

    InputStream is;
    Logger logger;

    public LogginInputStream(InputStream is, Logger logger) {
        this.is = is;
        this.logger = logger;
    }

    @Override
    public int read() throws IOException {
        int i = is.read();
        return is.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return is.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        int i = is.read(b, off, len);
        if (i>=0) {
            String s = new String(b, off, i);
            logger.info("--> " + s);
        }
        return i;
    }

    @Override
    public long skip(long n) throws IOException {
        return is.skip(n);
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public void mark(int readlimit) {
        is.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        is.reset();
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }
}
