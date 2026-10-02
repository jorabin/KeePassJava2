/*
 * Copyright 2026 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.linguafranca.pwdb;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.KdbxHeader;
import org.linguafranca.pwdb.kdbx.KdbxSerializer;
import org.linguafranca.pwdb.kdbx.KdbxStreamFormat;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Attachments in the XML Meta/Binaries and the V4 inner header, issues 97 and 98
 */
public class JacksonBinariesTest {

    private static final Credentials CREDENTIALS = new KdbxCreds("123".getBytes());
    private static final Pattern META_BINARIES = Pattern.compile("<Binaries>.*?</Binaries>", Pattern.DOTALL);

    /**
     * the inner header and decrypted XML of a KDBX file
     */
    private static class Contents {
        final KdbxHeader header = new KdbxHeader();
        final String xml;

        Contents(byte[] file) throws IOException {
            try (InputStream inputStream = KdbxSerializer.createUnencryptedInputStream(CREDENTIALS, header, new ByteArrayInputStream(file))) {
                xml = new String(readAll(inputStream), StandardCharsets.UTF_8);
            }
        }

        boolean hasMetaBinaries() {
            return xml.contains("<Binaries>");
        }
    }

    @Test
    public void v3WritesBinaryElements() throws Exception {
        JacksonDatabase database = load("V4-ChaCha20-Argon2-Attachment.kdbx");
        Contents contents = new Contents(save(database, new KdbxStreamFormat(new KdbxHeader(3))));

        Matcher matcher = META_BINARIES.matcher(contents.xml);
        assertTrue(matcher.find());
        assertTrue(matcher.group().contains("<Binary ID=\"0\""));
        assertFalse(matcher.group().contains("<Binaries ID="));
    }

    @Test
    public void v4LeavesBinariesOutOfXml() throws Exception {
        JacksonDatabase database = load("Attachment.kdbx");
        Contents contents = new Contents(save(database, new KdbxStreamFormat(new KdbxHeader(4))));

        assertFalse(contents.hasMetaBinaries());
        assertEquals(2, contents.header.getBinaries().size());
    }

    @Test
    public void v4SavedTwiceKeepsInnerHeaderSize() throws Exception {
        JacksonDatabase database = load("V4-ChaCha20-Argon2-Attachment.kdbx");
        save(database);
        Contents contents = new Contents(save(database));

        assertEquals(2, contents.header.getBinaries().size());
    }

    @Test
    public void streamFormatNoneAfterV4KeepsBinaries() throws Exception {
        JacksonDatabase database = load("V4-ChaCha20-Argon2-Attachment.kdbx");
        save(database);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        database.save(new StreamFormat.None(), new Credentials.None(), outputStream);

        assertTrue(new String(outputStream.toByteArray(), StandardCharsets.UTF_8).contains("<Binary ID=\"0\""));
    }

    @Test
    public void attachmentsSurviveV3V4V3() throws Exception {
        JacksonDatabase original = load("Attachment.kdbx");
        Map<String, byte[]> expected = attachments(original);

        byte[] v4 = save(original, new KdbxStreamFormat(new KdbxHeader(4)));
        JacksonDatabase fromV4 = JacksonDatabase.load(CREDENTIALS, new ByteArrayInputStream(v4));
        assertAttachments(expected, fromV4);

        byte[] v3 = save(fromV4, new KdbxStreamFormat(new KdbxHeader(3)));
        assertAttachments(expected, JacksonDatabase.load(CREDENTIALS, new ByteArrayInputStream(v3)));
    }

    /**
     * Before issue 98 Jackson wrote each Meta binary as a Binaries element
     */
    @Test
    public void loadsBinariesWrittenBy224() throws Exception {
        JacksonDatabase original = load("Attachment.kdbx");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        original.save(new StreamFormat.None(), new Credentials.None(), outputStream);
        String xml = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

        Matcher matcher = META_BINARIES.matcher(xml);
        assertTrue(matcher.find());
        String oldStyle = matcher.group()
                .replace("<Binary ID=", "<Binaries ID=")
                .replace("</Binary>", "</Binaries>");
        xml = xml.substring(0, matcher.start()) + oldStyle + xml.substring(matcher.end());

        JacksonDatabase database = JacksonDatabase.load(new StreamFormat.None(), new Credentials.None(),
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertAttachments(attachments(original), database);
    }

    private JacksonDatabase load(String resource) throws IOException {
        return JacksonDatabase.load(CREDENTIALS, getClass().getClassLoader().getResourceAsStream(resource));
    }

    private static byte[] save(JacksonDatabase database, StreamFormat<?> streamFormat) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        database.save(streamFormat, CREDENTIALS, outputStream);
        return outputStream.toByteArray();
    }

    private static byte[] save(JacksonDatabase database) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        database.save(CREDENTIALS, outputStream);
        return outputStream.toByteArray();
    }

    private static Map<String, byte[]> attachments(JacksonDatabase database) {
        final Map<String, byte[]> result = new TreeMap<>();
        database.visit(new Visitor.Default() {
            @Override
            public void visit(Entry entry) {
                for (Object name : entry.getBinaryPropertyNames()) {
                    result.put(entry.getUuid() + "/" + name, entry.getBinaryProperty((String) name));
                }
            }
        });
        return result;
    }

    private static void assertAttachments(Map<String, byte[]> expected, JacksonDatabase database) {
        Map<String, byte[]> actual = attachments(database);
        assertEquals(expected.keySet(), actual.keySet());
        assertFalse(expected.isEmpty());
        for (String key : expected.keySet()) {
            assertTrue(key, Arrays.equals(expected.get(key), actual.get(key)));
        }
    }

    private static byte[] readAll(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, read);
        }
        return outputStream.toByteArray();
    }
}
