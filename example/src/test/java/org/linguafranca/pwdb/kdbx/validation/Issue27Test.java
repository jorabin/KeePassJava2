package org.linguafranca.pwdb.kdbx.validation;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxSerializer;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class Issue27Test {
    static PrintStream printStream = getTestPrintStream();
    /**
     * Check load of problem file
     */
    @Test
    public void testIssue27() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("issue-27/bogus-timestamp2.kdbx");
        KdbxCreds creds = new KdbxCreds("passwordless".getBytes());
        JaxbDatabase db = JaxbDatabase.load(creds, is);
        List<? extends JaxbEntry> entries = db.findEntries("testtitle");

        for (JaxbEntry entry: entries) {
            printStream.println(Helpers.fromDateV3(entry.getCreationTime()));
            assertEquals("2021-01-11T09:18:56Z", Helpers.fromDateV3(entry.getCreationTime()));
        }
    }

    /**
     * Verify that V4 dates are still processed correctly
     */
    @Test
    public void testV4Date() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("V4-AES-AES.kdbx");
        KdbxCreds creds = new KdbxCreds("123".getBytes());
        JaxbDatabase db = JaxbDatabase.load(creds, is);
        List<? extends JaxbEntry> entries = db.findEntries("Sample Entry #2 - Copy");

        for (JaxbEntry entry: entries) {
            printStream.println(Helpers.fromDate(entry.getCreationTime()));
            assertEquals("2018-01-26T13:20:58Z", Helpers.fromDateV3(entry.getCreationTime()));
        }
    }

    @Test @Ignore
    public void testIssue27XML() throws IOException {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("issue-27/bogus-timestamp2.kdbx");
        KdbxCreds creds = new KdbxCreds("passwordless".getBytes());
        InputStream plainText = KdbxSerializer.createUnencryptedInputStream(creds,new KdbxHeader(), is);
        printStream.println(IOUtils.toString(new InputStreamReader(plainText, StandardCharsets.UTF_8)));
    }
}
