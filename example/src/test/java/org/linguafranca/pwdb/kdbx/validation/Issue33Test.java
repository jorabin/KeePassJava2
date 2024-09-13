package org.linguafranca.pwdb.kdbx.validation;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.format.Helpers;
import org.linguafranca.pwdb.format.KdbxCreds;
import org.linguafranca.pwdb.kdbx.Util;
import org.linguafranca.pwdb.kdbx.KdbxDatabase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Review Issue-33 https://github.com/jorabin/KeePassJava2/issues/33
 */
public class Issue33Test {

    public static final String TEST_RESOURCE = "V4-AES-Argon2-CustomIcon.kdbx";
    public static final String TEST_OUTPUT_DIR = "testOutput";
    public static final File TEST_XML_FILE = Paths.get(TEST_OUTPUT_DIR,"Issue33Source.xml").toFile();
    public static final KdbxCreds CREDENTIALS = new KdbxCreds("123".getBytes());


    InputStream inputStream;

    @BeforeClass
    public static void listXml() throws IOException {
        Files.createDirectories(Paths.get(TEST_OUTPUT_DIR));
        Util.listXml(TEST_RESOURCE, CREDENTIALS, new PrintWriter(TEST_XML_FILE));
        Helpers.isV4.set(true);
    }

    @Before
    public void refreshInputStream() {
        inputStream = this.getClass().getClassLoader().getResourceAsStream(TEST_RESOURCE);
    }

    @Test
    public void testJacksonDatabase() throws IOException {
        KdbxDatabase database = KdbxDatabase.load(CREDENTIALS, inputStream);
        database.save(new StreamFormat.None(), new Credentials.None(), Files.newOutputStream(Paths.get(TEST_OUTPUT_DIR, "Issue33Jackson.xml")));
    }
}
