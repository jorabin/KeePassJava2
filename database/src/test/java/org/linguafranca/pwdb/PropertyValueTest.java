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

package org.linguafranca.pwdb;



import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.linguafranca.pwdb.PropertyValueUtil.genericTest;

public class PropertyValueTest {

    public static final String ANOTHER_SECRET = "password with accents àéç";

    @Test
    public void bytesTest() {
        genericTest(PropertyValue.BytesStore.getFactory());
    }

    @Test
    public void sealedObjectTest() {
        genericTest(PropertyValue.SealedStore.getFactory());
    }

    @Test
    public void stringTest() {
        genericTest(PropertyValue.StringStore.getFactory());
    }

    @Test
    public void sealedScriptTest(){
        PropertyValue.SealedStore sealed = PropertyValue.SealedStore.getFactory().of(ANOTHER_SECRET);
        byte[] bytes = sealed.getValueAsBytes();
        assertArrayEquals(ANOTHER_SECRET.getBytes(StandardCharsets.UTF_8), bytes);
    }
}