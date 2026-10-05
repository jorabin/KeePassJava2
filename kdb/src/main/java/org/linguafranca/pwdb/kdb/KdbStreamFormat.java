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

package org.linguafranca.pwdb.kdb;

import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.SerializableDatabase;
import org.linguafranca.pwdb.StreamFormat;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * The KDB format, as the stream format of a {@link KdbDatabase}, holding the {@link KdbHeader} it was
 * read with.
 * <p>
 * KDB databases are read with {@link KdbDatabase#read(Credentials, InputStream)} and can't be written,
 * so the reading and writing methods of this class throw {@link UnsupportedOperationException}.
 *
 * @since 3.1.0
 */
public class KdbStreamFormat implements StreamFormat<KdbHeader> {

    private KdbHeader kdbHeader;

    public KdbStreamFormat() {
        this(new KdbHeader());
    }

    public KdbStreamFormat(KdbHeader kdbHeader) {
        this.kdbHeader = kdbHeader;
    }

    @Override
    public void read(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) {
        throw new UnsupportedOperationException("Read KDB files with KdbDatabase.read");
    }

    @Override
    public void write(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) {
        throw new UnsupportedOperationException("Cannot write KDB files");
    }

    @Override
    @Deprecated
    public void load(SerializableDatabase serializableDatabase, Credentials credentials, InputStream encryptedInputStream) {
        throw new UnsupportedOperationException("Read KDB files with KdbDatabase.read");
    }

    @Override
    @Deprecated
    public void save(SerializableDatabase serializableDatabase, Credentials credentials, OutputStream encryptedOutputStream) {
        throw new UnsupportedOperationException("Cannot write KDB files");
    }

    @Override
    public KdbHeader getStreamConfiguration() {
        return kdbHeader;
    }

    @Override
    public void setStreamConfiguration(KdbHeader configuration) {
        this.kdbHeader = configuration;
    }
}
