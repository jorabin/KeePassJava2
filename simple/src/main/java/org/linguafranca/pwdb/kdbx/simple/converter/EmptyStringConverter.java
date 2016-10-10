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

package org.linguafranca.pwdb.kdbx.simple.converter;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * @author jo
 */
public class EmptyStringConverter implements Converter<String> {
    @Override
    public String read(InputNode inputNode) throws Exception {
        String value = inputNode.getValue();
        return value == null ? "": value;
    }

    @Override
    public void write(OutputNode outputNode, String s) throws Exception {
        if(s==null || s.equals("")) {
            outputNode.setValue(null);
        } else {
            outputNode.setValue(s);
        }
    }
}
