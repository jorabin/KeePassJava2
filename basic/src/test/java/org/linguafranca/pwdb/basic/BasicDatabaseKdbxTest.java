/*
 * Copyright (c) 2025. Jo Rabin
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

package org.linguafranca.pwdb.basic;

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.linguafranca.pwdb.format.KdbxStreamFormat;
import org.linguafranca.pwdb.test.DatabaseTestBase;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.linguafranca.pwdb.Entry.STANDARD_PROPERTY_NAME.*;

public class BasicDatabaseKdbxTest         extends
        DatabaseTestBase  {

    public BasicDatabaseKdbxTest(){
        super(BasicDatabase::new,
                (credentials, inputStream) ->
                        new BasicDatabaseSerializer.Xml().loadNx(inputStream),
                (database, credentials, outputStream) ->
                        new BasicDatabaseSerializer.Xml().saveNx((BasicDatabase) database, outputStream),
                (credentials) -> null);
    }

    @Test
    public void testKdbx() throws IOException {
        BasicDatabase db = new BasicDatabase();
        db.setName("test");
        db.getRootGroup().addEntry("test")
                .addProperty(PASSWORD,"password")
                .addProperty(URL,"http://example.com")
                .addProperty(USER_NAME,"user");
        db.save(new KdbxStreamFormat(), new KdbxCredentials("123".getBytes()), new FileOutputStream(Path.of(OUTPUT_DIRECTORY_PATH, "test.kdbx").toFile()));

        KdbxHeader header = new KdbxHeader();
        InputStream unencryptedInputStream = KdbxSerializer.createUnencryptedInputStream(new KdbxCredentials("123".getBytes()),
                header, new FileInputStream(Path.of(OUTPUT_DIRECTORY_PATH, "test.kdbx").toFile()));
        BasicDatabaseSerializer.Xml xml = new BasicDatabaseSerializer.Xml(header.getInnerStreamEncryptor());
        Database db2 = xml.load(unencryptedInputStream);
        assertEquals(db.getName(), db2.getName());
        assertEquals(db2.getRootGroup().findEntries(e -> true, true).size(), db.getRootGroup().findEntries(e -> true, true).size());
        assertEquals("password", db2.getRootGroup().findEntries(e -> e.getTitle().equals("test"), true).get(0).getPropertyValue(PASSWORD).getValueAsString());
    }
}
