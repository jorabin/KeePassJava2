/*
 * Copyright 2015 Giuseppe Valente
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

package org.linguafranca.pwdb.kdbx.jackson.model;

import java.util.ArrayList;
import java.util.List;

import org.linguafranca.pwdb.PropertyValue;
import org.linguafranca.pwdb.kdbx.jackson.JacksonEntry;
import org.linguafranca.pwdb.kdbx.jackson.converter.BooleanToStringConverter;
import org.linguafranca.pwdb.kdbx.jackson.converter.StringToBooleanConverter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public abstract class EntryClasses {
    public static StringProperty getStringProperty(String name, List<StringProperty> string) {
        for (StringProperty property : string) {
            if (property.key.equals(name)) {
                return property;
            }
        }
        return null;
    }

    public static BinaryProperty getBinaryProp(String name, List<BinaryProperty> binary) {
        for (BinaryProperty property : binary) {
            if (property.key.equals(name)) {
                return property;
            }
        }
        return null;
    }

    public static String getBinaryContent(BinaryProperty property) {
        return property == null || property.value == null ? null : property.value.ref;
    }

    public static class AutoType {
        @JacksonXmlProperty(localName = "Enabled")
        @JsonDeserialize(converter = StringToBooleanConverter.class)
        @JsonSerialize(converter = BooleanToStringConverter.class)
        protected Boolean enabled;

        @JacksonXmlProperty(localName = "DataTransferObfuscation")
        protected int dataTransferObfuscation;

        @JacksonXmlProperty(localName = "DefaultSequence")
        protected String defaultSequence;

        @JacksonXmlProperty(localName = "Association")
        protected AutoType.Association association;

        public static class Association {

            @JacksonXmlProperty(localName = "Window")
            protected String window;
            @JacksonXmlProperty(localName = "KeystrokeSequence")
            protected String keystrokeSequence;

            protected List<Object> windowAndKeystrokeSequence;
        }
    }

    public static class StringProperty {

        @JacksonXmlProperty(localName = "Key")
        String key;

        @JacksonXmlProperty(localName = "Value")
        PropertyValue value;

        @SuppressWarnings("unused")
        public StringProperty() {}

        public StringProperty(String key, PropertyValue value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public PropertyValue getValue() {
            return value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(PropertyValue value) {
            this.value = value;
        }
    }

    public static class BinaryProperty {

        @JacksonXmlProperty(localName = "Key")
        String key;

        @JacksonXmlProperty(localName = "Value")
        Value value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public static class Value {
            @JacksonXmlProperty(localName = "Ref", isAttribute = true)
            String ref;

            public void setRef(String ref) {
                this.ref = ref;
            }
        }
    }

    public static class History {

        @JacksonXmlProperty(localName = "Entry") /* Workaround jackson */
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<JacksonEntry> list;

        public History() {
            list = new ArrayList<>();
        }

        public List<JacksonEntry> getHistory() {
            return list;
        }
    }
}
