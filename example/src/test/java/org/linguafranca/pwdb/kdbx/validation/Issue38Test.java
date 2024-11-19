package org.linguafranca.pwdb.kdbx.validation;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import static org.linguafranca.test.util.TestUtil.getTestPrintStream;

/**
 * Bug report on GitHub, the Keyfile is Version 2 (Hex)
 */
public class Issue38Test {

    static PrintStream printStream = getTestPrintStream();
    @Test
    public void testV2Keyfile() throws IOException {
        InputStream databaseStream = Issue38Test.class.getClassLoader().getResourceAsStream("issue-38/Database/Database.kdbx");
        InputStream keyStream = Issue38Test.class.getClassLoader().getResourceAsStream("issue-38/Database/Database.keyx");
        assert keyStream != null;
        KdbxCreds creds = new KdbxCreds("MyPassword".getBytes(), keyStream);
        assert databaseStream != null;
        DomDatabaseWrapper database = DomDatabaseWrapper.load(creds, databaseStream);
        List<? extends DomEntryWrapper> entries = database.findEntries("Sample Entry");
        DomEntryWrapper entry = entries.get(0);
        printStream.println(entry.getTitle());
    }
}
