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

package org.linguafranca.pwdb.example;

import org.junit.Ignore;
import org.junit.Test;
import org.linguafranca.pwdb.kdbx.QuickStart;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleEntry;
import org.linguafranca.pwdb.kdbx.simple.SimpleGroup;
import org.linguafranca.pwdb.kdbx.simple.SimpleIcon;
import org.linguafranca.pwdb.Credentials;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple illustration of hooking a SAX parser up to process a KDBX file
 * 
 * @author jo
 */
public class SimpleQuickStartTest extends QuickStart<SimpleDatabase, SimpleGroup, SimpleEntry, SimpleIcon> {


    @Override
    public SimpleDatabase getDatabase() {
        return new SimpleDatabase();
    }

    @Override
    public SimpleDatabase loadDatabase(Credentials credentials, InputStream inputStream){
        try {
            return SimpleDatabase.load(credentials, inputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void saveTest() throws IOException {
        super.saveKdbx();
    }

    @Test
    public void loadTest() throws IOException {
        super.loadKdbx();
    }

    @Test
    public void loadKdbSaveVernacular() throws IOException {
        super.loadKdb();
    }


}
