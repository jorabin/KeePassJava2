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
import java.util.Scanner;

/**
 * @author jo
 */
public class KdbxInnerInputStreamTest {

    // TODO what does this test, exactly?
    @Test
    public void test() throws XMLStreamException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("ExampleDatabase.xml");
        XmlInputStreamFilter sxd = new XmlInputStreamFilter(is, new KdbxInputTransformer(new StreamEncryptor.None()));
        Scanner s = new Scanner(is);
        while (s.hasNext()) {
            System.out.println(s.nextLine());
        }
        s.close();
   }

}