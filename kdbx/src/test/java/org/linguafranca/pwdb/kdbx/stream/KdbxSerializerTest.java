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
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 * @author jo
 */
public class KdbxSerializerTest {

    @Test @Ignore // can be used for detailed HMAC debugging
    public void getHmacStream() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        System.out.println("Version " + header.getVersion());
        KdbxCreds creds = new KdbxCreds("123".getBytes());
        KdbxSerializer.verifyOuterHeader(header, creds, new LittleEndianDataInputStream(inputStream));
        HmacBlockInputStream hmacBlockInputStream = new HmacBlockInputStream(header.getHmacKey(creds), inputStream, true);
        byte [] buf = new byte [1024];
        int bytesRead;
        while ((bytesRead = hmacBlockInputStream.read(buf)) != -1) {
            System.out.println(bytesRead);
        }
    }

    @Test
    public void loadAesheader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-AES.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(3, header.getVersion());
    }

    @Test
    public void loadArgonheader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        assertEquals(4, header.getVersion());
    }


    @Test
    public void loadChaChaAesHeader() throws IOException {
        String expected = CharStreams.toString(new InputStreamReader(KdbxSerializerTest.class.getClassLoader().getResourceAsStream("ChaCha-AES.xml")));
        check("ChaCha-AES.xml","V4-ChaCha-AES.kdbx");
    }

    @Test
    public void getDecryptedArgonInputStream() throws IOException {
        String expected = CharStreams.toString(new InputStreamReader(KdbxSerializerTest.class.getClassLoader().getResourceAsStream("AES-Argon2.xml")));
        check("AES-Argon2.xml","V4-AES-Argon2.kdbx");
    }

    @Test
    public void getDecryptedAesInputStream() throws IOException {
        check("AES-AES.xml", "V4-AES-AES.kdbx");
    }

    private void check(String compare, String file) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);
        InputStream is = KdbxSerializer.createUnencryptedInputStream(new KdbxCreds("123".getBytes()), new KdbxHeader(), inputStream);
        String actual = CharStreams.toString(new InputStreamReader(is, Charset.forName("UTF-8")));
        String expected = CharStreams.toString(new InputStreamReader(KdbxSerializerTest.class.getClassLoader().getResourceAsStream(compare)));
        assertEquals(expected, actual);
    }
}