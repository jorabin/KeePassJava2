package org.linguafranca.db.kdbx;

import org.linguafranca.security.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface provides for wrapping a database serialization in a stream format, e.g. KDBX or none.
 *
 * @author jo
 */
public interface StreamFormat {
    /**
     * Class allows for serializing a database directly to or from a stream with no encryption etc
     */
    class None implements StreamFormat {

        @Override
        public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream inputStream) throws IOException {
            serializableDatabase.load(inputStream);
        }

        @Override
        public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream outputStream) throws IOException {
            serializableDatabase.save(outputStream);
        }
    }

    void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) throws IOException;

    void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) throws IOException;
}
