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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author jo
 */
public class SaxParseTest extends SaxParse {

    @Test
    public void exampleSaxparsingV3 () throws IOException, SAXException, ParserConfigurationException {
        super.exampleSaxparsing("test123.kdbx");
    }
    @Test
    public void exampleSaxparsingV4 () throws IOException, SAXException, ParserConfigurationException {
        super.exampleSaxparsing("V4-AES-Argon2-CustomIcon.kdbx");
    }

}
