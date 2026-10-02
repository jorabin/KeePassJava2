package org.linguafranca.pwdb.kdbx.validation;

import org.junit.Test;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;
import org.linguafranca.pwdb.kdbx.jackson.JacksonEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Review Issue-104 <a href="https://github.com/jorabin/KeePassJava2/issues/104">...</a>
 * <p>
 * Databases must be written as UTF-8 whatever the default encoding. The build also runs this
 * test with a default encoding of ISO-8859-1, see the example pom, which sets the
 * test.defaultEncoding property so that the test can check the setting took effect.
 */
public class Issue104Test {

    private static final KdbxCreds CREDENTIALS = new KdbxCreds("123".getBytes());
    private static final String TITLE = "Café";
    private static final String NOTES = "naïve façade, Größe, ☕";

    @Test
    public void checkDefaultEncoding() {
        String expected = System.getProperty("test.defaultEncoding");
        if (expected != null) {
            assertEquals(Charset.forName(expected), Charset.defaultCharset());
        }
    }

    @Test
    public void saveAndReloadNonAscii() throws IOException {
        JacksonDatabase database = new JacksonDatabase();
        JacksonEntry entry = database.newEntry(TITLE);
        entry.setProperty(Entry.STANDARD_PROPERTY_NAME_NOTES, NOTES);
        database.getRootGroup().addEntry(entry);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        database.save(CREDENTIALS, outputStream);

        JacksonDatabase reloaded = JacksonDatabase.load(CREDENTIALS, new ByteArrayInputStream(outputStream.toByteArray()));
        List<? extends JacksonEntry> entries = reloaded.findEntries(TITLE);
        assertEquals(1, entries.size());
        assertEquals(TITLE, entries.get(0).getTitle());
        assertEquals(NOTES, entries.get(0).getProperty(Entry.STANDARD_PROPERTY_NAME_NOTES));
    }
}
