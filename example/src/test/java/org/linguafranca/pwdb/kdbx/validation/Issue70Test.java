package org.linguafranca.pwdb.kdbx.validation;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.Util;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.test.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Review Issue-33 https://github.com/jorabin/KeePassJava2/issues/70
 */
public class Issue70Test {

    public static final String TEST_RESOURCE1 = "issue-70/test2.kdbx";
    public static final KdbxCreds CREDENTIALS = new KdbxCreds("KeePassJava2".getBytes());


    InputStream inputStream;

    @BeforeClass
    public static void listXml() throws IOException {
        Util.listXml(TEST_RESOURCE1, CREDENTIALS, new PrintWriter(TestUtil.getTestPrintStream()));
        Helpers.isV4.set(true);
    }

    @Before
    public void refreshInputStream() {
        inputStream = this.getClass().getClassLoader().getResourceAsStream(TEST_RESOURCE1);
    }

    @Test
    public void testDomDatabaseWrapper() throws IOException {
        DomDatabaseWrapper database = DomDatabaseWrapper.load(CREDENTIALS, inputStream);
    }

    @Test
    public void testJaxbDatabase() throws IOException {
        JaxbDatabase database = JaxbDatabase.load(CREDENTIALS, inputStream);
    }

    @Test
    public void testSimpleDatabase() throws IOException {
        SimpleDatabase database = SimpleDatabase.load(CREDENTIALS, inputStream);
    }

    @Test
    public void testJacksonDatabase() throws IOException {
        JacksonDatabase database = JacksonDatabase.load(CREDENTIALS, inputStream);
    }
}
