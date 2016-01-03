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

package org.linguafranca.pwdb.kdbx.dom;

import org.linguafranca.security.Credentials;
import org.linguafranca.pwdb.DatabaseLoaderChecks;
import org.linguafranca.pwdb.kdbx.KdbxCredentials;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jo
 */
public class DomDatabaseWrapperLoaderTest extends DatabaseLoaderChecks {

    public DomDatabaseWrapperLoaderTest() throws IOException {
        // get an input stream from kdbx file
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        // file has password credentials
        Credentials credentials = new KdbxCredentials.Password("123".getBytes());
        // open database. DomDatabaseWrapper is so-called, since it wraps
        // a W3C DOM, populated from the KeePass XML, and presents it
        // through a org.linguafranca.keepass.Database interface.
        super.database = DomDatabaseWrapper.load(credentials, inputStream);
    }
}
