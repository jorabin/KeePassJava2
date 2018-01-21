/*
 * Copyright 2015 Jo Rabin
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

package org.linguafranca.pwdb.kdbx;

import com.google.common.io.ByteStreams;
import org.junit.Ignore;
import org.junit.Test;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleEntry;
import org.linguafranca.pwdb.kdbx.simple.transformer.KdbxOutputTransformer;
import org.linguafranca.xml.XmlEventTransformer;
import org.linguafranca.xml.XmlInputStreamFilter;
import org.linguafranca.xml.XmlOutputStreamFilter;

import javax.xml.stream.XMLStreamException;
import java.io.*;

import static org.junit.Assert.assertEquals;

/**
 * @author jo
 */
public class KdbxInnerOutputStreamTest {

    @Test
    public void test() throws Exception {
        final SimpleDatabase database = new SimpleDatabase();
        final SimpleEntry entry = database.newEntry();
        entry.setTitle("Password Encyption Test");
        entry.setPassword("password");
        database.getRootGroup().addEntry(entry);
        database.save(new KdbxCreds.None(), ByteStreams.nullOutputStream());
    }

    @Test @Ignore
    public void testOutputStreamFilter () throws IOException, XMLStreamException {
        File temp = File.createTempFile("temp", "temp");
        OutputStream outputStream = new FileOutputStream(temp);
        XmlOutputStreamFilter filter = new XmlOutputStreamFilter(outputStream, new KdbxOutputTransformer.None());
        outputStream.write("<test>hello world</test>".getBytes());
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = new FileInputStream(temp);
        XmlInputStreamFilter filter1 = new XmlInputStreamFilter(inputStream, new XmlEventTransformer.None());
        byte[] b = new byte[1024];
        int l = filter1.read(b);
        String s = new String(b,0,l);
        assertEquals("<test>hello world</test>", s);
    }


}