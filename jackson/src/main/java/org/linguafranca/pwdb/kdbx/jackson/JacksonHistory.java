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
package org.linguafranca.pwdb.kdbx.jackson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class JacksonHistory {

    @JacksonXmlProperty(localName = "Entry") /** Workaround jackson **/
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<JacksonEntry> entry;

    public JacksonHistory() {
        entry = new ArrayList<JacksonEntry>();
    }

    /**
     * @return the entry
     */
    public List<JacksonEntry> getEntry() {
        return entry;
    }

    /**
     * @param entry the entry to set
     */
    public void setEntry(List<JacksonEntry> entry) {
        this.entry = entry;
    }

}
