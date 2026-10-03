/*
 * Copyright (c) 2026. Jo Rabin
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

package org.linguafranca.pwdb.io;

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.StreamConfiguration;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.security.StreamEncryptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Issue 109: the default write and read methods leave the stream open for implementations
 * that only have save and load, which close it
 */
@SuppressWarnings("deprecation")
class DefaultWriteReadTest {

    static class ClosingOutputStream extends ByteArrayOutputStream {
        boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }

    static class ClosingInputStream extends ByteArrayInputStream {
        boolean closed;

        ClosingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    /** An implementation written before 3.1.0: save and load close the stream */
    static class OldSerializableDatabase implements SerializableDatabase {
        String content = "content";

        @Override
        public SerializableDatabase load(InputStream inputStream) throws IOException {
            content = new String(inputStream.readAllBytes());
            inputStream.close();
            return this;
        }

        @Override
        public void save(OutputStream outputStream) throws IOException {
            outputStream.write(content.getBytes());
            outputStream.close();
        }

        @Override public StreamEncryptor getEncryption() { return null; }
        @Override public void setEncryption(StreamEncryptor encryption) {}
        @Override public byte[] getHeaderHash() { return null; }
        @Override public void setHeaderHash(byte[] hash) {}
        @Override public void addBinary(int index, byte[] payload) {}
        @Override public byte[] getBinary(int index) { return null; }
        @Override public int getBinaryCount() { return 0; }
    }

    /** An implementation written before 3.1.0: save and load close the stream */
    static class OldStreamFormat implements StreamFormat<StreamConfiguration.None> {
        @Override
        public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream inputStream) throws IOException {
            serializableDatabase.load(inputStream);
        }

        @Override
        public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream outputStream) throws IOException {
            serializableDatabase.save(outputStream);
        }

        @Override public StreamConfiguration.None getStreamConfiguration() { return new StreamConfiguration.None(); }
        @Override public void setStreamConfiguration(StreamConfiguration.None configuration) {}
    }

    @Test
    void serializableDatabaseDefaults() throws IOException {
        ClosingOutputStream outputStream = new ClosingOutputStream();
        new OldSerializableDatabase().write(outputStream);
        assertFalse(outputStream.closed);
        assertEquals("content", outputStream.toString());

        ClosingInputStream inputStream = new ClosingInputStream("other".getBytes());
        OldSerializableDatabase database = new OldSerializableDatabase();
        database.read(inputStream);
        assertFalse(inputStream.closed);
        assertEquals("other", database.content);
    }

    @Test
    void streamFormatDefaults() throws IOException {
        ClosingOutputStream outputStream = new ClosingOutputStream();
        new OldStreamFormat().write(new OldSerializableDatabase(), new Credentials.None(), outputStream);
        assertFalse(outputStream.closed);
        assertEquals("content", outputStream.toString());

        ClosingInputStream inputStream = new ClosingInputStream("other".getBytes());
        OldSerializableDatabase database = new OldSerializableDatabase();
        new OldStreamFormat().read(database, new Credentials.None(), inputStream);
        assertFalse(inputStream.closed);
        assertEquals("other", database.content);
    }

    @Test
    void streamFormatNone() throws IOException {
        ClosingOutputStream outputStream = new ClosingOutputStream();
        new StreamFormat.None().write(new OldSerializableDatabase(), new Credentials.None(), outputStream);
        assertFalse(outputStream.closed);

        outputStream = new ClosingOutputStream();
        new StreamFormat.None().save(new OldSerializableDatabase(), new Credentials.None(), outputStream);
        assertTrue(outputStream.closed);

        ClosingInputStream inputStream = new ClosingInputStream("other".getBytes());
        new StreamFormat.None().read(new OldSerializableDatabase(), new Credentials.None(), inputStream);
        assertFalse(inputStream.closed);

        inputStream = new ClosingInputStream("other".getBytes());
        new StreamFormat.None().load(new OldSerializableDatabase(), new Credentials.None(), inputStream);
        assertTrue(inputStream.closed);
    }
}
