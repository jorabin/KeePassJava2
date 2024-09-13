/*
 * Copyright 2023 Giuseppe Valente
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
package org.linguafranca.pwdb.kdbx.jackson.converter;

import java.util.Date;
import java.util.Objects;

import org.linguafranca.pwdb.format.Helpers;

import com.fasterxml.jackson.databind.util.StdConverter;

public class StringToDateConverter extends StdConverter<String, Date> {

    @Override
    public Date convert(String value) {
        // TODO: It would really be better if we could inhibit deserialize date elements that are not present
        if (Objects.isNull(value) || value.isEmpty()) {
            return null;
        }
        if(value.equals("${creationDate}")) {
            return new Date();
        }
        return Helpers.toDate(value);
    }
}
