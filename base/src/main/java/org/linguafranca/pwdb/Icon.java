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

package org.linguafranca.pwdb;

/**
 * Interface for icons.
 *
 * <p>Intended as a point of flexibility, at some point this
 * may get embellished to allow custom icons and retrieval
 * of icon assets and so on.
 *
 * @author Jo
 */
public interface Icon {
    int getIndex();

    void setIndex(int index);

}
