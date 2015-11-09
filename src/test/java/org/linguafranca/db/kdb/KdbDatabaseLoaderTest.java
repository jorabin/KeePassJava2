package org.linguafranca.db.kdb;

import org.linguafranca.security.Credentials;
import org.linguafranca.db.DatabaseLoaderChecks;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jo
 */
public class KdbDatabaseLoaderTest extends DatabaseLoaderChecks {

    public KdbDatabaseLoaderTest() throws IOException {
        // get an input stream from kdbx file
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdb");
        // file has password credentials
        Credentials credentials = new KdbCredentials.Password("123".getBytes());
        // open database. DomDatabaseWrapper is so-called, since it wraps
        // a W3C DOM, populated from the KeePass XML, and presents it
        // through a org.linguafranca.keepass.Database interface.
        super.database = KdbDatabase.load(credentials, inputStream);
    }
}
