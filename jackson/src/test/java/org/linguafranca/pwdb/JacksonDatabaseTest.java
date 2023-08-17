package org.linguafranca.pwdb;

import java.io.IOException;

import org.linguafranca.pwdb.checks.BasicDatabaseChecks;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;

public class JacksonDatabaseTest extends BasicDatabaseChecks {

    public JacksonDatabaseTest() throws IOException {

    }


    @Override
    public Database createDatabase() throws IOException {
        return new JacksonDatabase();
    }
    
}
