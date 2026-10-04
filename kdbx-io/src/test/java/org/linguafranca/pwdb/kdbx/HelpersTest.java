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

import org.junit.jupiter.api.Test;
import org.linguafranca.pwdb.format.Helpers;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.linguafranca.pwdb.format.Helpers.dateTimeFormatter;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class HelpersTest {

    static PrintStream printStream = getTestPrintStream();

    public String testDate = "2023-05-09T16:11:29Z";
    public Instant testInstant = ZonedDateTime.parse(testDate, dateTimeFormatter).toInstant();
    public String v4Encoding = "sWfs2w4AAAA=";

    @Test
    public void toInstantV4() {
        assertEquals(testInstant, Helpers.toInstant(v4Encoding));
    }

    @Test
    public void toInstantV3() {
        assertEquals(testInstant, Helpers.toInstant(testDate));
    }

    @Test
    public void fromInstant() {
        try {
            Helpers.isV4.set(false);
            assertEquals(testDate, Helpers.fromInstant(testInstant));
            Helpers.isV4.set(true);
            assertEquals(v4Encoding, Helpers.fromInstant(testInstant));
        } finally {
            Helpers.isV4.set(false);
        }
    }

    @Test
    public void fromInstantV3() {
        assertEquals(testDate, Helpers.fromInstantV3(testInstant));
    }

    @Test
    public void fromInstantV4() {
        String base64 = Helpers.fromInstantV4(testInstant);
        printStream.println(Helpers.toInstant(base64));
        assertEquals(v4Encoding, base64);
    }

    /**
     * KDBX times are whole seconds, fractions are dropped
     */
    @Test
    public void fractionsOfASecond() {
        Instant withFraction = testInstant.plusMillis(999);
        assertEquals(testDate, Helpers.fromInstantV3(withFraction));
        assertEquals(v4Encoding, Helpers.fromInstantV4(withFraction));
    }
}
