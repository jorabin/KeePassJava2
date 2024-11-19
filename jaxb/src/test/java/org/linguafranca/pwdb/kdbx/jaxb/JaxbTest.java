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

package org.linguafranca.pwdb.kdbx.jaxb;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.jaxb.binding.KeePassFile;
import org.linguafranca.pwdb.kdbx.jaxb.binding.StringField;

import javax.xml.bind.*;
import java.io.PrintStream;

import static org.linguafranca.test.util.TestUtil.getTestPrintStream;

/**
 * @author jo
 */
public class JaxbTest {
    static PrintStream printStream = getTestPrintStream();

    @Test
    public void unmarshal() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(KeePassFile.class);
        Unmarshaller u = jc.createUnmarshaller();
        u.setEventHandler(new ValidationEventHandler() {
            @Override
            public boolean handleEvent(ValidationEvent event) {
                printStream.println(event.getLocator().getLineNumber() +": "+ event.getMessage());
                return true;
            }
        });
        u.setListener(new Unmarshaller.Listener() {
            @Override
            public void afterUnmarshal(Object target, Object parent) {
                if (target instanceof StringField.Value) {
                    StringField.Value value = (StringField.Value) target;
                    printStream.println(value.getValue());
                }
                super.afterUnmarshal(target, parent);
            }
        });
        KeePassFile kpf = (KeePassFile) u.unmarshal(getClass().getClassLoader().getResourceAsStream("ExampleDatabase.xml"));
        printStream.println(kpf.getMeta().getDatabaseDescription());

    }
}
