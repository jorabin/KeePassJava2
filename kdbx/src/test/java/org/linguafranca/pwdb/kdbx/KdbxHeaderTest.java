package org.linguafranca.pwdb.kdbx;

import com.google.common.io.CharStreams;
import com.google.common.io.LittleEndianDataInputStream;
import org.junit.Ignore;
import org.junit.Test;
import org.linguafranca.pwdb.hashedblock.HmacBlockInputStream;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * test decryption of various kinds
 */
public class KdbxHeaderTest {
    static PrintStream printStream = getTestPrintStream();

    @Test @Ignore // can be used for detailed HMAC debugging
    public void getHmacStream() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        printStream.println("Version " + header.getVersion());
        KdbxCreds creds = new KdbxCreds("123".getBytes());
        assert inputStream != null;
        //noinspection UnstableApiUsage
        KdbxSerializer.readOuterHeaderVerification(header, creds, new LittleEndianDataInputStream(inputStream));
        HmacBlockInputStream hmacBlockInputStream = new HmacBlockInputStream(header.getHmacKey(creds), inputStream, true);
        printStream.println(CharStreams.toString(new InputStreamReader(hmacBlockInputStream, StandardCharsets.UTF_8)));
    }

    // check the correct version
    @Test
    public void loadAesAesHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-AES.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }

    // check correct version
    @Test
    public void loadAesArgonHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }

    @Test
    public void loadChaChaHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-ChaCha20-AES.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }
    @Test
    public void loadChaChaArgonHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-ChaCha20-Argon2-Attachment.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }

}