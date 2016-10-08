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

package org.linguafranca.pwdb.kdbx.jaxb.example;

import org.junit.Test;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Simple illustration of hooking a SAX parser up to process a KDBX file
 * 
 * @author jo
 */
public class SaxParsingTest {
    @Test
    public void exampleSaxparsing () throws IOException, SAXException, ParserConfigurationException {
        new QuickStart().exampleSaxparsing();
    }
}
