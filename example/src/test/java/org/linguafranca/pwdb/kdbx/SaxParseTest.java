/*
 * Copyright (c) 2025. Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.linguafranca.pwdb.kdbx;

import org.junit.Test;
import org.linguafranca.pwdb.format.KdbxCreds;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * @author jo
 */

public class SaxParseTest extends SaxParse {

    OutputStream outputStream = getTestPrintStream();
    @Test
    public void exampleSaxParsingV3 () throws IOException, SAXException, ParserConfigurationException {
        super.exampleSaxParsing("test123.kdbx", new KdbxCreds("123".getBytes()), new PrintWriter(outputStream));
    }
    @Test
    public void exampleSaxParsingV4 () throws IOException, SAXException, ParserConfigurationException {
        super.exampleSaxParsing("V4-AES-AES.kdbx", new KdbxCreds("123".getBytes()), new PrintWriter(outputStream));
    }

}
