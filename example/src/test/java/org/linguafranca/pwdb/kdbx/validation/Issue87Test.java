package org.linguafranca.pwdb.kdbx.validation;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxSerializer;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;
import org.linguafranca.pwdb.kdbx.jackson.JacksonEntry;
import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.linguafranca.pwdb.kdbx.Util.streamToString;

/**
 * Review Issue-87 <a href="https://github.com/jorabin/KeePassJava2/issues/87">...</a>
 */
public class Issue87Test {


    public static final String TEST_RESOURCE1 = "issue-87/customIcon-123.kdbx";
    public static final KdbxCreds CREDENTIALS1 = new KdbxCreds("123".getBytes());
    public static final String TEST_OUTPUT_ISSUE_87_KDBX = "testOutput/Issue87.kdbx";


    @Test
    public void testCustomIcon() throws IOException {
        JacksonDatabase database;
        try (InputStream inputStream = Issue87Test.class.getClassLoader().getResourceAsStream(TEST_RESOURCE1)) {
            database = JacksonDatabase.load(CREDENTIALS1, inputStream);
        }

        List<? extends JacksonEntry> entries = database.findEntries("Michael321");
        assertEquals(1, entries.size());

        UUID customIcon = entries.get(0).getCustomIconUuid();
        assertNotNull(customIcon);
        System.out.println("Custom icon id: " + customIcon);
        KeePassFile.Icon icon = entries.get(0).getCustomIcon();

        Path path = Paths.get(TEST_OUTPUT_ISSUE_87_KDBX);
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            database.save(CREDENTIALS1, outputStream);
        }

        // output the XML of the file we just saved, to check that the custom icon is still there
        try (InputStream is = Files.newInputStream(path);
             InputStream ss = KdbxSerializer.createUnencryptedInputStream(CREDENTIALS1, new KdbxHeader(), is)) {
            System.out.println(streamToString(ss));
            System.out.println();
            System.out.flush();
        }

        // now load the newly written database and check the custom icon is the same as the original
        try (InputStream is = Files.newInputStream(path)) {
            JacksonDatabase database1 = JacksonDatabase.load(CREDENTIALS1, is);
            List<? extends JacksonEntry> entries1 = database1.findEntries("Michael321");
            assertEquals(1, entries1.size());
            UUID customIcon1 = entries1.get(0).getCustomIconUuid();
            assertNotNull(customIcon1);
            assertEquals(customIcon, customIcon1);
            KeePassFile.Icon icon1 = entries1.get(0).getCustomIcon();
            assertEquals(icon1.name, icon.name);
            assertArrayEquals(icon1.data, icon.data);
            assertEquals(icon1.uuid, icon.uuid);
        }
    }
}