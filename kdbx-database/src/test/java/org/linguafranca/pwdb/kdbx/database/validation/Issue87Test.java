package org.linguafranca.pwdb.kdbx.database.validation;

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.format.KdbxCredentials;
import org.linguafranca.pwdb.format.KdbxHeader;
import org.linguafranca.pwdb.format.KdbxSerializer;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;
import org.linguafranca.pwdb.kdbx.jackson.KdbxEntry;
import org.linguafranca.pwdb.kdbx.jackson.model.KeePassFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.linguafranca.pwdb.kdbx.jackson.util.Util.streamToString;

/**
 * Review Issue-87 <a href="https://github.com/jorabin/KeePassJava2/issues/87">...</a>
 */
public class Issue87Test {


    public static final String TEST_RESOURCE1 = "issue-87/customIcon-123.kdbx";
    public static final KdbxCredentials CREDENTIALS1 = new KdbxCredentials("123".getBytes());
    public static final String TEST_OUTPUT_ISSUE_87_KDBX = "testOutput/Issue87.kdbx";


    @Test
    public void testCustomIcon() throws IOException {
        KdbxDatabase database;
        try (InputStream inputStream = Issue87Test.class.getClassLoader().getResourceAsStream(TEST_RESOURCE1)) {
            database = KdbxDatabase.load(CREDENTIALS1, inputStream);
        }

        List<Entry> entries = database.findEntries("Michael321");
        assertEquals(1, entries.size());

        KdbxEntry entry = (KdbxEntry) entries.get(0);
        UUID customIcon = entry.getCustomIconUuid();
        assertNotNull(customIcon);
        System.out.println("Custom icon id: " + customIcon);
        KeePassFile.Icon icon = entry.getCustomIcon();

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
            KdbxDatabase database1 = KdbxDatabase.load(CREDENTIALS1, is);
            List<Entry> entries1 = database1.findEntries("Michael321");
            assertEquals(1, entries1.size());
            KdbxEntry entry1 = (KdbxEntry) entries1.get(0);
            UUID customIcon1 = entry1.getCustomIconUuid();
            assertNotNull(customIcon1);
            assertEquals(customIcon, customIcon1);
            KeePassFile.Icon icon1 = entry1.getCustomIcon();
            assertEquals(icon1.name, icon.name);
            assertArrayEquals(icon1.data, icon.data);
            assertEquals(icon1.uuid, icon.uuid);
        }
    }
}