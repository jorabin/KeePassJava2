package org.linguafranca.pwdb.keepasshttp.util;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jo
 */
public class LogginOutputStream extends OutputStream {

    Logger logger;
    OutputStream outputStream;

    public LogginOutputStream(OutputStream outputStream, Logger logger) {
        this.logger = logger;
        this.outputStream = outputStream;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        logger.info("<-- " + new String(b, off, len));
        outputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
