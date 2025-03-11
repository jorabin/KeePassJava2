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

package org.linguafranca.pwdb.basic;

import org.linguafranca.pwdb.Icon;

import java.util.Objects;

public class BasicIcon implements Icon {
    private int index;

    public BasicIcon() {
    }

    public BasicIcon(Integer i) {
        this.index = i;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BasicIcon)) return false;
        BasicIcon basicIcon = (BasicIcon) o;
        return index == basicIcon.index;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(index);
    }
}
