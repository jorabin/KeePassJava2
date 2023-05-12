package org.linguafranca.pwdb.kdbx;

import com.google.common.io.CharStreams;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.StreamFormat;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Util {

    public static InputStream getDecryptedInputStream (String resourceName, byte [] password) throws IOException {
        return getDecryptedInputStream(resourceName, password, new KdbxHeader());
    }

    public static InputStream getDecryptedInputStream (String resourceName, byte [] password, KdbxHeader header) throws IOException {
        InputStream is = Util.class.getClassLoader().getResourceAsStream(resourceName);
        KdbxCreds creds = new KdbxCreds(password);
        return KdbxSerializer.createUnencryptedInputStream(creds, header, is);
    }

    public static String streamToString(InputStream inputStream) throws IOException {
        return CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    /**
     * Example shows how to list XML with decoded field values (but not decrypted passwords)
     */
    public static void listDatabase(String resourceName, Credentials creds, OutputStream outputStream) throws IOException {
        DomDatabaseWrapper database = DomDatabaseWrapper.load(creds, Util.class.getClassLoader().getResourceAsStream(resourceName));
        database.save(new StreamFormat.None(), new KdbxCreds.None(), outputStream);
    }


    /**
     * Example shows how to list the XML of a database in a raw form
     * @param resourceName the name of a resource to find on the classpath
     * @param password the password for the resource
     * @param printWriter a PrintWriter to list the contents
     */
    public static void listXml(String resourceName, byte [] password, PrintWriter printWriter) throws IOException {
        printWriter.format(resourceName + "\n");
        printWriter.println(streamToString(getDecryptedInputStream(resourceName, password)));
        printWriter.println();
        printWriter.flush();
    }

    /**
     * Example shows how to list KdbxParameters of a KdbxHeader. You don't need a password as the
     * header is not encrypted. From V4 InnerStream encryption is defined in the Inner Header
     * which is stored encrypted and is not read until the database is read.
     * @param resourceName the name of a resource to find on the classpath
     * @param printWriter a PrintWriter to list the contents
     */
    public static void listKdbxHeaderProperties(String resourceName, PrintWriter printWriter) throws IOException {
        printWriter.format(resourceName + "\n");
        KdbxHeader kdbxHeader = new KdbxHeader();
        InputStream is = Util.class.getClassLoader().getResourceAsStream(resourceName);
        KdbxSerializer.readOuterHeader(is, kdbxHeader);
        printWriter.format("Version: %d\n", kdbxHeader.getVersion());
        printWriter.format("Cipher Algorithm: %s\n", kdbxHeader.getCipherAlgorithm().getName());
        printWriter.format("Key Derivation Function: %s\n", kdbxHeader.getKeyDerivationFunction().getName());
        String pseName = kdbxHeader.getVersion() == 3 ? kdbxHeader.getProtectedStreamAlgorithm().name() :
                        "Inner Stream Algorithm not in header in V4";
        printWriter.format("Inner Stream Algorithm: %s\n", pseName);
        printWriter.println();
        printWriter.flush();
    }
}
