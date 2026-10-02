package org.linguafranca.pwdb.kdbx.validation;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxSerializer;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.linguafranca.pwdb.kdbx.Util.streamToString;

/**
 * Review Issue-88 <a href="https://github.com/jorabin/KeePassJava2/issues/88">...</a>
 */
public class Issue88Test {


    public static final String TEST_RESOURCE1 = "issue-88/newDb-123.kdbx";
    public static final KdbxCreds CREDENTIALS1 = new KdbxCreds("123".getBytes());
    public static final String TEST_OUTPUT_ISSUE_88_KDBX = "testOutput/Issue88.kdbx";


    @Test
    public void testDefaultAutoType() throws IOException {
        JacksonDatabase database;
        try (InputStream inputStream = Issue88Test.class.getClassLoader().getResourceAsStream(TEST_RESOURCE1)) {
            database = JacksonDatabase.load(CREDENTIALS1, inputStream);
        }

        Path path = Paths.get(TEST_OUTPUT_ISSUE_88_KDBX);
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            database.save(CREDENTIALS1, outputStream);
        }

        // output the XML of the file we just saved, to check that the empty element is serialized
        try (InputStream is = Files.newInputStream(path);
             InputStream ss = KdbxSerializer.createUnencryptedInputStream(CREDENTIALS1, new KdbxHeader(), is)) {
            System.out.println(streamToString(ss));
            System.out.println();
            System.out.flush();
        }
    }
}