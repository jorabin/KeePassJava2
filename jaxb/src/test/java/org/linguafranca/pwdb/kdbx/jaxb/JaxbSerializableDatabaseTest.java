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

package org.linguafranca.pwdb.kdbx.jaxb;

import org.junit.Test;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.KdbxCreds;

import java.io.InputStream;

/**
 * @author jo
 */
public class JaxbSerializableDatabaseTest {
    @Test
    public void createEmptyDatabase() throws Exception {
        JaxbDatabase db = JaxbDatabase.createEmptyDatabase();
        db.save(new KdbxCreds.None(), System.out);
    }
    @Test
    public void loadXml() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        JaxbDatabase database = JaxbDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print());
    }

}