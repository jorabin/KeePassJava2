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

package org.linguafranca.pwdb;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Make available as a utility to provide for PropertyValue extension  implementations
 */
public class PropertyValueUtil {

    public static final String THIS_IS_A_SECRET = "This is a secret + לַחַיִּים";

    public static void genericTest(PropertyValue.Factory<?> factory) {
        genericTest(factory, true);
    }

    public static void genericTest(PropertyValue.Factory<?> factory, boolean testStringReturn) {
        // test input CharSequence
        PropertyValue testValue = factory.of(THIS_IS_A_SECRET);
        test(testStringReturn, testValue);

        // test input char[]
        testValue = factory.of(THIS_IS_A_SECRET.toCharArray());
        test(testStringReturn, testValue);


        // test input byte[]
        testValue = factory.of(THIS_IS_A_SECRET.getBytes(StandardCharsets.UTF_8));
        test(testStringReturn, testValue);

    }

    private static void test(boolean testStringReturn, PropertyValue testValue) {
        if (testStringReturn) assertEquals(THIS_IS_A_SECRET, testValue.getValue().toString());
        assertEquals(THIS_IS_A_SECRET, testValue.getValueAsString());

        // get value as chars
        char[] chars = testValue.getValueAsChars();
        // return value is correct
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), chars);
        chars[0] = 'z';
        // return value is still correct
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), testValue.getValueAsChars());


        // get value as bytes
        byte[] bytes = testValue.getValueAsBytes();
        // return value is correct
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(StandardCharsets.UTF_8), bytes);
        bytes[0] = 'z';
        // return value is still correct
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(StandardCharsets.UTF_8), testValue.getValueAsBytes());
    }

}
