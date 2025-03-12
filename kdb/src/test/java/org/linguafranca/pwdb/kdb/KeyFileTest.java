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

package org.linguafranca.pwdb.kdb;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jo
 */
public class KeyFileTest {

    @Test
    public void openKdbWithKeyFile() throws IOException {
        InputStream key = getClass().getClassLoader().getResourceAsStream("kdb.key");
        KdbCredentials creds = new KdbCredentials.KeyFile("123".getBytes(), key);
        InputStream is = getClass().getClassLoader().getResourceAsStream("kdbwithkey.kdb");
        KdbDatabase db = KdbDatabase.load(creds, is);
        assertEquals(1, db.getRootGroup().getGroupsCount());
        assertEquals("General", db.getRootGroup().getGroups().get(0).getName());
    }
}