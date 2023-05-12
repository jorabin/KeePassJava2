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

import java.io.PrintStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * tests the operation of Hex conversion
 */
public class HelperTest {
    static PrintStream printStream = getTestPrintStream();

    @Test
    public void testBase64() throws Exception {
        String inputString = "0tU8XFRUX2TCk5tmmrshuQ==";
        printStream.println("Input string: " + inputString);
        String hexString = "D2D53C5C54545F64C2939B669ABB21B9";
        String convertedString = Helpers.hexStringFromBase64("0tU8XFRUX2TCk5tmmrshuQ==");
        printStream.println("Converted string: " + convertedString);
        assertEquals(hexString, convertedString.toUpperCase());

        UUID uuid = Helpers.uuidFromBase64(inputString);
        printStream.println("UUID: " + uuid.toString());
        assertEquals("d2d53c5c-5454-5f64-c293-9b669abb21b9", uuid.toString());

        String uuidHex = Helpers.hexStringFromUuid(uuid);
        printStream.println("Hex from UUID: " + uuidHex);
        assertEquals(hexString, uuidHex.toUpperCase());

        String base64 = Helpers.base64FromUuid(uuid);
        printStream.println("Base 64: " + base64);
        assertEquals(inputString, base64);
    }

}
