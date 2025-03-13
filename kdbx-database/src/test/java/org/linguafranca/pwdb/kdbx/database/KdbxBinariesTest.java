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
package org.linguafranca.pwdb.kdbx.database;

import org.junit.jupiter.api.Nested;
import org.linguafranca.pwdb.test.BinaryPropertiesTest;

public class KdbxBinariesTest {

    @Nested
    public class KdbxBinariesV4Test
            extends
                KdbxTestBase
            implements
                BinaryPropertiesTest {
        KdbxBinariesV4Test() {
            database =  loadDatabase("123".getBytes(), "V4-ChaCha20-Argon2-Attachment.kdbx");
        }
    }
    @Nested
    public class KdbxBinariesV3Test
            extends
                KdbxTestBase
            implements
                BinaryPropertiesTest {
        KdbxBinariesV3Test() {
            database =  loadDatabase("123".getBytes(), "Attachment.kdbx");
        }
    }
}
