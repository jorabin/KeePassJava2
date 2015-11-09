package org.linguafranca.db.kdbx.dom;

import org.junit.Test;
import org.linguafranca.db.BasicDatabaseChecks;
import org.linguafranca.security.Credentials;
import org.linguafranca.db.kdbx.StreamFormat;
import org.linguafranca.db.kdbx.KdbxCredentials;
import org.linguafranca.db.kdbx.KdbxStreamFormat;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jo
 */
public class DomDatabaseWrapperTest extends BasicDatabaseChecks {

    public DomDatabaseWrapperTest() throws IOException {
        super(new DomDatabaseWrapper());
    }

    @Test
    public void inspectPasswordDatabase() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxStreamFormat(), new KdbxCredentials.Password("123".getBytes()), inputStream);

        database.save(new StreamFormat.None(), new Credentials.None(), System.out);
    }

    @Test
    public void inspectKeyfileDatabase() throws IOException {
        InputStream keyFileInputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.key");
        Credentials credentials = new KdbxCredentials.KeyFile("123".getBytes(), keyFileInputStream);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxStreamFormat(), credentials, inputStream);

        database.save(new StreamFormat.None(), new Credentials.None(), System.out);
    }
}