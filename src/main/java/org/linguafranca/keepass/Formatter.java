package org.linguafranca.keepass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jo
 */
public interface Formatter {
    class NoOp implements Formatter {

        @Override
        public void load(DatabaseProvider databaseProvider, Credentials credentials, InputStream encryptedInputStream) throws IOException {
            databaseProvider.load(encryptedInputStream);
        }

        @Override
        public void save(DatabaseProvider databaseProvider, Credentials credentials, OutputStream encryptedOutputStream) throws IOException {
            databaseProvider.save(encryptedOutputStream);
        }
    }

    void load(DatabaseProvider databaseProvider, Credentials credentials, InputStream encryptedInputStream) throws IOException;

    void save(DatabaseProvider databaseProvider, Credentials credentials, OutputStream encryptedOutputStream) throws IOException;
}
