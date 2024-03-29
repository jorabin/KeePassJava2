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

package org.linguafranca.pwdb.kdbx.jaxb.util;

import org.linguafranca.pwdb.kdbx.Helpers;

import java.util.Date;

/**
 * Used for loading the database template which contains placeholders.
 * @author jo
 */
public class DateAdapter {
    public static String toString(Date date) {
        return Helpers.fromDate(date);
    }

    public static Date fromString(String string) {
        // this is found in the base template
        if (string.equals("${creationDate}")) {
            return new Date();
        }
        return Helpers.toDate(string);
    }
}
