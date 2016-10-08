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

package org.linguafranca.pwdb.kdbx.dom;

import org.linguafranca.pwdb.Icon;
import org.w3c.dom.Element;

/**
 * Class wraps Icons from a {@link DomSerializableDatabase} as {@link Icon}
 *
 * @author jo
 */
public class DomIconWrapper implements Icon {

    private final Element element;

    public DomIconWrapper(Element element) {
        this.element = element;
    }

    @Override
    public int getIndex() {
        return Integer.parseInt(element.getTextContent());
    }

    @Override
    public void setIndex(int index) {
        element.setTextContent(String.valueOf(index));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Icon)) return false;

        Icon that = (Icon) o;

        return this.getIndex() == that.getIndex();

    }
}
