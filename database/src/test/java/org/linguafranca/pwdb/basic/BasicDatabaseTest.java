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

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.test.CrudDatabaseTest;
import org.linguafranca.pwdb.test.RecycleBinTest;
import org.linguafranca.pwdb.test.ProtectedPropertyTest;
import org.linguafranca.pwdb.test.TrivialDatabaseTest;

public class BasicDatabaseTest implements
        TrivialDatabaseTest,
        CrudDatabaseTest,
        RecycleBinTest,
        ProtectedPropertyTest {

    Database database;

    /**
     * Create a new database
     */
    @Override
    public Database createDatabase() {
        return new BasicDatabase();
    }

    /**
     * Create a new database for default use in tests
     */
    @Override
    public void newDatabase() {
        database = createDatabase();
    }

    /**
     * Get the current default database
     */
    @Override
    public Database getDatabase() {
        return database;
    }


}
