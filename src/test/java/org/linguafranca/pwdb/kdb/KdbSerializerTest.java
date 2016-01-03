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

package org.linguafranca.pwdb.kdb;

import org.junit.Test;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Visitor;

import java.io.InputStream;

/**
 * @author jo
 */
public class KdbSerializerTest {

    @Test
    public void testCreateKdbDatabase() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdb");
        Database database = KdbDatabase.load(new KdbCredentials.Password("123".getBytes()), inputStream);
        database.visit(new Visitor.Print());
    }
}