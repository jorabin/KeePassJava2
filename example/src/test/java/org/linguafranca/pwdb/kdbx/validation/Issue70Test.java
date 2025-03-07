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

package org.linguafranca.pwdb.kdbx.validation;

import org.junit.Test;
import org.linguafranca.pwdb.format.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

import java.io.IOException;
import java.io.InputStream;

/**
 * Review Issue-70 https://github.com/jorabin/KeePassJava2/issues/70
 */
public class Issue70Test {

    public static final String TEST_RESOURCE1 = "issue-70/test2.kdbx";
    public static final KdbxCreds CREDENTIALS1 = new KdbxCreds("KeePassJava2".getBytes());
    public static final String TEST_RESOURCE2 = "Database-4.1-123.kdbx";
    public static final KdbxCreds CREDENTIALS2 = new KdbxCreds("123".getBytes());
    public static final String TEST_RESOURCE3 = "issue-70/test-hugoo10.kdbx";
    public static final KdbxCreds CREDENTIALS3 = new KdbxCreds("test".getBytes());


    /* custom data missing from meta */
    @Test
    public void testCustomData() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(TEST_RESOURCE1)) {
            KdbxDatabase database = KdbxDatabase.load(CREDENTIALS1, inputStream);
        }
    }

    @Test
    public void testFileFormat_4_1() throws IOException {
       try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(TEST_RESOURCE2)){
            KdbxDatabase database = KdbxDatabase.load(CREDENTIALS2, inputStream);
       }
    }

    /* custom data missing from entry definition */
    @Test
    public void testCustomData2() throws IOException {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(TEST_RESOURCE3)){
            KdbxDatabase database = KdbxDatabase.load(CREDENTIALS3, inputStream);
        }
    }

}