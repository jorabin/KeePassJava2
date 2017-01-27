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

package org.linguafranca.pwdb.kdbx.simple.model;

import org.linguafranca.pwdb.kdbx.simple.SimpleEntry;
import org.linguafranca.pwdb.kdbx.simple.converter.KeePassBooleanConverter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;
import org.simpleframework.xml.convert.Convert;

import java.util.List;

/**
 * @author jo
 */
@SuppressWarnings("unused")
public abstract class EntryClasses {
    public static StringProperty getStringProperty(String name, List<StringProperty> string) {
        for (StringProperty property : string) {
            if (property.key.equals(name)) {
                return property;
            }
        }
        return null;
    }

    public static String getStringContent(StringProperty property) {
        return property == null || property.value == null? null:property.value.text;
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
        return property == null || property.value == null ? null :  property.value.ref;
    }

    @Root(name = "AutoType")
    public static class AutoType {
        @Element(name = "Enabled", type = Boolean.class)
        @Convert(KeePassBooleanConverter.class)
        protected Boolean enabled;
        @Element(name = "DataTransferObfuscation")
        protected int dataTransferObfuscation;
        @Element(name = "DefaultSequence", required = false)
        protected String defaultSequence;
        @Element(name = "Association", required = false)
        protected AutoType.Association association;

        public static class Association {
            @Element(name = "Window")
            protected String window;
            @Element(name="KeystrokeSequence")
            protected String keystrokeSequence;
            protected List<Object> windowAndKeystrokeSequence;
        }
    }

    @Root(name="String")
    public static class StringProperty implements org.simpleframework.xml.util.Entry {

        public StringProperty() {
            this("",new Value());
        }
        public StringProperty(String key, Value value) {
            this.key = key;
            this.value = value;
        }

        @Element(name="Key")
        String key;

        @Element(name="Value")
        Value value;

        @Override
        public String getName() {
            return key;
        }

        public String getKey() {
            return key;
        }

        public Value getValue() {
            return value;
        }

        @Root(name="Value")
        public static class Value {
            public Value(){
                this("");
            }
            public Value(String text) {
                this.text = text;
                this._protected = false;
            }

            public Value(String text, Boolean _protected) {
                this._protected = _protected;
                this.text = text;
            }

            @Attribute(name = "ProtectInMemory", required = false)
            @Convert(KeePassBooleanConverter.class)
            // NB converters don't work on attributes -see KdbxOutputTransformer
            protected Boolean protectInMemory;
            @Attribute(name = "Protected", required = false)
            @Convert(KeePassBooleanConverter.class)
            // NB converters don't work on attributes -see KdbxOutputTransformer
            Boolean _protected;
            @Text
            String text;

            public void setProtected(boolean aProtected) {
                this._protected = aProtected;
            }
        }
    }

    @Root(name="Binary")
    public static class BinaryProperty implements org.simpleframework.xml.util.Entry {

        @Element(name="Key")
        String key;

        @Element(name="Value")
        Value value;

        public String getKey() {
            return key;
        }

        @Override
        public String getName() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        @Root(name="Value")
        public static class Value {
            @Attribute(name="Ref")
            String ref;

            public void setRef(String ref) {
                this.ref = ref;
            }
        }
    }

    @Root(name = "History")
    public static class History {
        @Element(name = "SimpleEntry", required = false)
        protected SimpleEntry entry;
    }
}
