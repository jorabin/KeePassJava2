/*
 * Copyright 2023 Giuseppe Valente
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
package org.linguafranca.pwdb;

import java.io.InputStream;

import org.linguafranca.pwdb.checks.DatabaseLoaderChecks;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;

public class JacksonDatabaseLoaderTest extends DatabaseLoaderChecks{
    
     
    public JacksonDatabaseLoaderTest() throws Exception{
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        // file has password credentials
        Credentials credentials = new KdbxCreds("123".getBytes());
        super.database = JacksonDatabase.load(credentials, inputStream);
    }

}
