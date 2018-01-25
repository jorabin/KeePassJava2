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
import org.linguafranca.pwdb.checks.BasicDatabaseChecks;
import org.linguafranca.pwdb.Database;

import java.io.IOException;

import static org.junit.Assert.assertFalse;

/**
 * @author Jo
 */
public class KdbDatabaseTest  extends BasicDatabaseChecks {
    @Override
    public Database createDatabase() throws IOException {
        return new KdbDatabase();
    }

    public KdbDatabaseTest() throws IOException {
        super();
    }

    @Test
    public void supportedFunctionalityTest(){
        assertFalse(database.supportsBinaryProperties());
        assertFalse(database.supportsNonStandardPropertyNames());
        assertFalse(database.supportsRecycleBin());
    }
}
