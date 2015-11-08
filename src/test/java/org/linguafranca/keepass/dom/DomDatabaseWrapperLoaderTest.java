package org.linguafranca.keepass.dom;

import org.linguafranca.keepass.Credentials;
import org.linguafranca.keepass.DatabaseLoaderChecks;

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
        Credentials credentials = new Credentials.Password("123");
        // open database. DomDatabaseWrapper is so-called, since it wraps
        // a W3C DOM, populated from the KeePass XML, and presents it
        // through a org.linguafranca.keepass.Database interface.
        super.database = DomDatabaseWrapper.load(credentials, inputStream);
    }
}
