package org.linguafranca.pwdb.kdbx;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.InputStream;
import java.io.PrintStream;

import static org.linguafranca.test.util.TestUtil.getTestPrintStream;

/**
 * Example and naive speed test for various implementations.
 *
 * @author jo
 */
public class OpenDbExample {

    static PrintStream printStream = getTestPrintStream();

    private interface DbLoader {
        Database load(KdbxCreds creds, InputStream inputStream) throws Exception;
    }

    private static class SimpleDbLoader implements DbLoader {
        @Override
        public Database load(KdbxCreds creds, InputStream inputStream) throws Exception {
            return SimpleDatabase.load(creds, inputStream);
        }
    }

    private static class DomDbLoader implements  DbLoader {
        @Override
        public Database load(KdbxCreds creds, InputStream inputStream) throws Exception {
            return DomDatabaseWrapper.load(creds, inputStream);
        }
    }

    private static class JaxbDbLoader implements  DbLoader {
        @Override
        public Database load(KdbxCreds creds, InputStream inputStream) throws Exception {
            return JaxbDatabase.load(creds, inputStream);
        }
    }

    private static class JacksonDbLoader implements DbLoader {
        @Override
        public Database load(KdbxCreds creds, InputStream inputStream) throws Exception {
            return JacksonDatabase.load(creds, inputStream);
        }
    }

    public static void testDb (DbLoader loader, String label, int loads, int iterations) throws Exception {
        KdbxCreds creds = new KdbxCreds("123".getBytes());
        long start = System.currentTimeMillis();
        for (int i=0; i < loads; i++) {
            try (InputStream inputStream = OpenDbExample.class.getClassLoader().getResourceAsStream("test1.kdbx")) {
                Database database = loader.load(creds, inputStream);
                for (int j = 0; j < iterations; j++) {
                    database.visit(new Visitor.Default() {
                    });
                }
            }
        }
        printStream.printf("%s %d loads %d iterations %d millis%n", label, loads, iterations, System.currentTimeMillis()-start);
    }


    public static void main(String[] args) throws Exception {
        printStream.println("Warming up JVM");
        testDb(new SimpleDbLoader(), "Simple", 5, 20);
        testDb(new JaxbDbLoader(), "Jaxb", 5, 20);
        testDb(new DomDbLoader(), "Dom", 5, 20);
        testDb(new JacksonDbLoader(), "Jackson", 5, 20);

        printStream.println("Sleeping");
        System.gc();
        Thread.sleep(2000);

        testDb(new SimpleDbLoader(), "Simple", 5, 20);
        testDb(new JaxbDbLoader(), "Jaxb", 5, 20);
        testDb(new DomDbLoader(), "Dom", 5, 20);
        testDb(new JacksonDbLoader(), "Jackson", 5, 20);

        printStream.println("Sleeping");
        System.gc();
        Thread.sleep(2000);

        testDb(new SimpleDbLoader(), "Simple", 10, 1);
        testDb(new JaxbDbLoader(), "Jaxb", 10, 1);
        testDb(new DomDbLoader(), "Dom", 10, 1);
        testDb(new JacksonDbLoader(), "Jackson", 10, 1);

        printStream.println("Sleeping");
        System.gc();
        Thread.sleep(2000);

        testDb(new SimpleDbLoader(), "Simple", 1, 50);
        testDb(new JaxbDbLoader(), "Jaxb", 1, 50);
        testDb(new DomDbLoader(), "Dom", 1, 50);
        testDb(new JacksonDbLoader(), "Jackson", 1, 50);
    }
}
