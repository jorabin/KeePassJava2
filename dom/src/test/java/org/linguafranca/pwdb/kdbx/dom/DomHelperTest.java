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

package org.linguafranca.pwdb.kdbx.dom;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.Helpers;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * @author jo
 */
public class DomHelperTest {

    static PrintStream printStream = getTestPrintStream();

    @Test
    public void testBase64RandomUuid() throws Exception {
        // just check that it can do something
        String uuid1 =  DomHelper.base64RandomUuid();
        printStream.println(Helpers.uuidFromBase64(uuid1));
        String uuid2 = DomHelper.base64RandomUuid();
        printStream.println(Helpers.uuidFromBase64(uuid2));
    }
}