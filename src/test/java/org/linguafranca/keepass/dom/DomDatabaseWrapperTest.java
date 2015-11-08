package org.linguafranca.keepass.dom;

import org.junit.Test;
import org.linguafranca.keepass.BasicDatabaseChecks;
import org.linguafranca.keepass.Credentials;
import org.linguafranca.keepass.Formatter;
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
    public void inspectExistingDatabase2() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        DomDatabaseWrapper database = new DomDatabaseWrapper(new KdbxFormatter(), new Credentials.Password("123"), inputStream);

        database.save(new Formatter.NoOp(), new Credentials.NoOp(), System.out);
    }

}