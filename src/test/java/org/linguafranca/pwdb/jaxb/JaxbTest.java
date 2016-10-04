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

package org.linguafranca.pwdb.jaxb;

import org.junit.Test;
import org.linguafranca.pwdb.kdbx.jaxb.binding.KeePassFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * @author jo
 */
public class JaxbTest {

    @Test
    public void unmarshal() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(KeePassFile.class);
        Unmarshaller u = jc.createUnmarshaller();
        KeePassFile kpf = (KeePassFile) u.unmarshal(getClass().getClassLoader().getResourceAsStream("ExampleDatabase.xml"));
        System.out.println(kpf.getMeta().getDatabaseDescription());

    }
}
