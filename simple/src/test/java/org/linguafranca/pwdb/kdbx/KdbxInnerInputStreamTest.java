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

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.simple.transformer.KdbxInputTransformer;
import org.linguafranca.xml.XmlInputStreamFilter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author jo
 */
public class KdbxInnerInputStreamTest {


    @Test
    public void test() throws XMLStreamException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("ExampleDatabase.xml");
        XmlInputStreamFilter sxd = new XmlInputStreamFilter(is, new KdbxInputTransformer(new org.linguafranca.pwdb.kdbx.stream_3_1.Salsa20StreamEncryptor.None()));
        int len;
        do {
            byte b[] = new byte[8096];
            len = sxd.read(b);
            if (len >-1)
               System.out.print(new String(b, 0, len, Charset.forName("UTF-8")));
        } while (len > -1);
   }

}