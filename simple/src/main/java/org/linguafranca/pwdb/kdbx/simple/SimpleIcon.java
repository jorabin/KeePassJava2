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

package org.linguafranca.pwdb.kdbx.simple;

import org.linguafranca.pwdb.Icon;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class SimpleIcon implements Icon {
        private int index;

    public SimpleIcon() {
    }

    public SimpleIcon(int index) {
        this.index = index;
    }

    @Override
        public int getIndex() {
            return index;
        }

        @Override
        public void setIndex(int i) {
            index = i;
        }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleIcon that = (SimpleIcon) o;

        return index == that.index;

    }

    @Override
    public int hashCode() {
        return index;
    }
}
