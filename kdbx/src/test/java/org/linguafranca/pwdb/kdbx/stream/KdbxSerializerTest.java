package org.linguafranca.pwdb.kdbx.stream;

import com.google.common.io.LittleEndianDataInputStream;
import org.junit.Ignore;
import org.junit.Test;
import org.linguafranca.pwdb.hashedblock.HmacBlockInputStream;
import org.linguafranca.pwdb.kdbx.KdbxCredentials;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxSerializer;

import java.io.*;
import java.util.Scanner;

/**
 * @author jo
 */
public class KdbxSerializerTest {

    @Test
    public void loadArgonheader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        System.out.println("Version " + header.getVersion());
    }

    @Test
    public void getHmacStream() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        System.out.println("Version " + header.getVersion());
        byte [] hmacKey = KdbxSerializer.verifyOuterHeader(header, new KdbxCredentials.Password("123".getBytes()), new LittleEndianDataInputStream(inputStream));
        HmacBlockInputStream hmacBlockInputStream = new HmacBlockInputStream(hmacKey, inputStream, true);
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
        System.out.println("Version " + header.getVersion());
    }

    @Test
    public void loadChaChaAesHeader() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-ChaCha-AES.kdbx");
        KdbxHeader header = KdbxSerializer.readOuterHeader(inputStream, new KdbxHeader());
        System.out.println("Version " + header.getVersion());
    }

    @Test
    public void getDecryptedArgonInputStream() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-Argon2.kdbx");
        InputStream is = KdbxSerializer.createUnencryptedInputStream(new KdbxCreds("123".getBytes()), new KdbxHeader(), inputStream);

        PrintWriter pw = new PrintWriter(new FileOutputStream("temp.xml"));
        Scanner s = new Scanner(is);
        while (s.hasNext()) {
            pw.println(s.nextLine());
        }
        s.close();
        pw.close();
    }

    @Test
    public void getDecryptedAesInputStream() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-AES-AES.kdbx");
        InputStream is = KdbxSerializer.createUnencryptedInputStream(new KdbxCreds("123".getBytes()), new KdbxHeader(), inputStream);
        Scanner s = new Scanner(is);
        while (s.hasNext()) {
            System.out.println(s.nextLine());
        }
        s.close();
    }

    @Test
    public void getDecryptedCHaChaInputStream() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("V4-ChaCha-AES.kdbx");
        InputStream is = KdbxSerializer.createUnencryptedInputStream(new KdbxCreds("123".getBytes()), new KdbxHeader(), inputStream);
        Scanner s = new Scanner(is);
        while (s.hasNext()) {
            System.out.println(s.nextLine());
        }
        s.close();

    }

}