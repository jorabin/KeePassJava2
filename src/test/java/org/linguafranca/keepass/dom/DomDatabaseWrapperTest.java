package org.linguafranca.keepass.dom;

import org.junit.Test;
import org.linguafranca.keepass.BasicDatabaseChecks;
import org.linguafranca.keepass.Credentials;
import org.linguafranca.keepass.Formatter;
import org.linguafranca.keepass.kdbx.KdbxCredentials;
import org.linguafranca.keepass.kdbx.KdbxFormatter;

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
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxFormatter(), new KdbxCredentials.Password("123".getBytes()), inputStream);

        database.save(new Formatter.NoOp(), new Credentials.NoOp(), System.out);
    }

    @Test
    public void inspectKeyfileDatabase() throws IOException {
        InputStream keyFileInputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.key");
        Credentials credentials = new KdbxCredentials.KeyFile("123".getBytes(), keyFileInputStream);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("KeyFileDatabase.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxFormatter(), credentials, inputStream);

        database.save(new Formatter.NoOp(), new Credentials.NoOp(), System.out);
    }
}