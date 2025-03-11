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

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.format.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

import java.io.InputStream;
import java.io.PrintStream;

import static org.linguafranca.util.TestUtil.getTestPrintStream;

/**
 * Example and naive speed test for various implementations.
 * <p>
 * This isn't really very interesting in V3 but remains here in case new implementations are added.
 *
 * @author jo
 */
public class OpenDbExample {

    static PrintStream printStream = getTestPrintStream();

    private interface DbLoader {
        Database load(KdbxCreds creds, InputStream inputStream) throws Exception;
    }

    private static class JacksonDbLoader implements DbLoader {
        @Override
        public Database load(KdbxCreds creds, InputStream inputStream) throws Exception {
            return KdbxDatabase.load(creds, inputStream);
        }
    }

    private static void testDb (DbLoader loader, String label, int loads, int iterations) throws Exception {
        KdbxCreds creds = new KdbxCreds("123".getBytes());
        long start = System.currentTimeMillis();
        for (int i=0; i < loads; i++) {
            try (InputStream inputStream = OpenDbExample.class.getClassLoader().getResourceAsStream("test1.kdbx")) {
                Database database = loader.load(creds, inputStream);
                for (int j = 0; j < iterations; j++) {
                    database.visit(new Visitor.Default() {
                    });
                }
            }
        }
        printStream.printf("%s %d loads %d iterations %d millis%n", label, loads, iterations, System.currentTimeMillis()-start);
    }


    public static void main(String[] args) throws Exception {
        printStream.println("Warming up JVM");
        testDb(new JacksonDbLoader(), "Jackson", 5, 20);

        printStream.println("Sleeping");
        System.gc();
        Thread.sleep(2000);

        testDb(new JacksonDbLoader(), "Jackson", 5, 20);

        printStream.println("Sleeping");
        System.gc();
        Thread.sleep(2000);

        testDb(new JacksonDbLoader(), "Jackson", 10, 1);

        printStream.println("Sleeping");
        System.gc();
        Thread.sleep(2000);

        testDb(new JacksonDbLoader(), "Jackson", 1, 50);
    }
}
