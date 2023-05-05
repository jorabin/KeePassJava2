package org.linguafranca.pwdb.kdbx.stream;

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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * test decryption of various kinds
 */
public class KdbxSerializerTest {

    @Test @Ignore // can be used for detailed HMAC debugging
    public void getHmacStream() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        System.out.println("Version " + header.getVersion());
        KdbxCreds creds = new KdbxCreds("123".getBytes());
        assert inputStream != null;
        //noinspection UnstableApiUsage
        KdbxSerializer.readOuterHeaderVerification(header, creds, new LittleEndianDataInputStream(inputStream));
        HmacBlockInputStream hmacBlockInputStream = new HmacBlockInputStream(header.getHmacKey(creds), inputStream, true);
        System.out.println(CharStreams.toString(new InputStreamReader(hmacBlockInputStream, StandardCharsets.UTF_8)));
    }

    // check the correct version
    @Test
    public void loadAesheader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-AES.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(3, header.getVersion());
    }

    // check correct version v4
    @Test
    public void loadArgonheader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }

    /**
     * Check correct content against previously extracted content
     */
    @Test
    public void loadChaChaAesHeader() throws IOException {
        check("ChaCha-AES.xml","V4-ChaCha-AES.kdbx");
    }

    /**
     * Check correct content against previously extracted content
     */
    @Test
    public void getDecryptedArgonInputStream() throws IOException {
        check("AES-Argon2.xml","V4-AES-Argon2.kdbx");
    }

    /**
     * Check correct content against previously extracted content
     */
    @Test
    public void getDecryptedAesInputStream() throws IOException {
        check("AES-AES.xml", "V4-AES-AES.kdbx");
    }

    private void check(String compare, String file) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);
        InputStream is = KdbxSerializer.createUnencryptedInputStream(new KdbxCreds("123".getBytes()), new KdbxHeader(), inputStream);
        String actual = CharStreams.toString(new InputStreamReader(is, StandardCharsets.UTF_8));
        //noinspection DataFlowIssue
        String expected = CharStreams.toString(new InputStreamReader(KdbxSerializerTest.class.getClassLoader().getResourceAsStream(compare)));
        assertEquals(expected, actual);
    }
}