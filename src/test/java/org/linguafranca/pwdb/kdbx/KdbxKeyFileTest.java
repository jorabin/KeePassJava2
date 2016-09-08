/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.pwdb.kdbx;

import org.junit.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.security.Credentials;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author jo
 */
public class KdbxKeyFileTest {

    @Test
    public void testLoad() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.key");
        byte[] key = KdbxKeyFile.load(inputStream);
        assertNotNull(key);
        assertEquals(32, key.length);
    }

    @Test
    public void testNoPasswordLoad() throws Exception {
        InputStream inputStreamDB = getClass().getClassLoader().getResourceAsStream("EmptyKey.kdbx");
        InputStream inputStreamKeyFile = getClass().getClassLoader().getResourceAsStream("EmptyKey.key");
        Credentials credentials = new KdbxCredentials.KeyFile(new byte[0], inputStreamKeyFile);
        Database database = DomDatabaseWrapper.load(credentials, inputStreamDB);
    }
}