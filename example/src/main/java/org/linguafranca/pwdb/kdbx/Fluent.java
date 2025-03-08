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
import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

public class Fluent {

    public void fluentExample() {
        Database database = new KdbxDatabase();
        database.getRootGroup().addGroup("Group 1")
                .addEntry("as property values determined by the database property value strategy (preferred)")
                    .addProperty("prop1", "value1".getBytes())
                    .addProperty("prop2", "value2")
                    .addProperty("prop3", "value3")
                .addEntry("as explicit property values")
                    .setPropertyValue("prop1", new PropertyValue.BytesStore("value1".getBytes()))
                    .setPropertyValue("prop2", new PropertyValue.SealedStore("value2"))
                .addEntry("as strings")
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .setProperty("prop3", "value3");
    }
}
