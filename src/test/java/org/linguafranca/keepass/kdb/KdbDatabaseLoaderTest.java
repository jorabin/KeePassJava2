package org.linguafranca.keepass.kdb;

import org.linguafranca.keepass.Credentials;
import org.linguafranca.keepass.DatabaseLoaderChecks;
import org.linguafranca.keepass.dom.DomDatabaseWrapper;

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
        Credentials credentials = new Credentials.Password("123");
        // open database. DomDatabaseWrapper is so-called, since it wraps
        // a W3C DOM, populated from the KeePass XML, and presents it
        // through a org.linguafranca.keepass.Database interface.
        super.database = KdbDatabase.load(credentials, inputStream);
    }
}
