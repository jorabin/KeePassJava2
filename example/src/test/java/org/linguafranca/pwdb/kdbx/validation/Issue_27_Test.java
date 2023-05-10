package org.linguafranca.pwdb.kdbx.validation;

import com.google.common.io.CharStreams;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Issue_27_Test {
    /**
     * Check load of problem file
     */
    @Test
    public void testIssue27() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("issue-27/bogus-timestamp2.kdbx");
        KdbxCreds creds = new KdbxCreds("passwordless".getBytes());
        JaxbDatabase db = JaxbDatabase.load(creds, is);
        List<? extends JaxbEntry> entries = db.findEntries("testtitle");

        for (JaxbEntry entry: entries) {
            System.out.println(Helpers.fromDate(entry.getCreationTime()));
            assertEquals("2021-01-11T09:18:56Z", Helpers.fromDate(entry.getCreationTime()));
        }
    }

    /**
     * Verify that V4 dates are still processed correctly
     */
    @Test
    public void testV4Date() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("V4-AES-AES.kdbx");
        KdbxCreds creds = new KdbxCreds("123".getBytes());
        JaxbDatabase db = JaxbDatabase.load(creds, is);
        List<? extends JaxbEntry> entries = db.findEntries("Sample Entry #2 - Copy");

        for (JaxbEntry entry: entries) {
            System.out.println(Helpers.fromDate(entry.getCreationTime()));
            assertEquals("2018-01-26T13:20:58Z", Helpers.fromDate(entry.getCreationTime()));
        }
    }

    @Test @Ignore
    public void testIssue27XML() throws IOException {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("issue-27/bogus-timestamp2.kdbx");
        KdbxCreds creds = new KdbxCreds("passwordless".getBytes());
        InputStream plainText = KdbxSerializer.createUnencryptedInputStream(creds,new KdbxHeader(), is);
        System.out.println(CharStreams.toString(new InputStreamReader(plainText, StandardCharsets.UTF_8)));
    }
}
