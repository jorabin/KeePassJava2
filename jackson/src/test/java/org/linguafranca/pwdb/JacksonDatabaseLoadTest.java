package org.linguafranca.pwdb;

import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class JacksonDatabaseLoadTest {
    
    static PrintStream printStream = getTestPrintStream();


    @Test
    public void loadXml() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ExampleDatabase.xml");
        JacksonDatabase database = JacksonDatabase.loadXml(inputStream);
        database.visit(new Visitor.Print(printStream));
    }
    @Test
    public void loadKdbx() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test123.kdbx");
        JacksonDatabase database = JacksonDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print(printStream));
    }

    @Test
    public void loadKdbxV4() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        JacksonDatabase database = JacksonDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
        database.visit(new Visitor.Print(printStream));
        // test what happens to dates in V4
        database.visit(new Visitor.Default(){
            @Override
            public void visit(Entry entry) {
                printStream.println(entry.getCreationTime());
            }
        });
    }

    @Test
    public void emptyDb() throws Exception {
        JacksonDatabase database = new JacksonDatabase();
        printStream.println(database.getDescription());
    }

    @Test
    public void dbWithDeleted() throws Exception {
         InputStream inputStream = getClass().getClassLoader().getResourceAsStream("testDeleted.kdbx");
         JacksonDatabase database = JacksonDatabase.load(new KdbxCreds("123".getBytes()), inputStream);
         database.visit(new Visitor.Print(printStream));
     }

}
