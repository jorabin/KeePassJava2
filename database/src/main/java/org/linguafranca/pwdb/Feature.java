/*
 * Copyright (c) 2026. Jo Rabin
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

/**
 * Optional features of a {@link Database}, see {@link Database#supports(Feature)}.
 * <p>
 * When a database doesn't support a feature, getters answer as if there were nothing there
 * (an empty list, {@code ""}, {@code false} or, for lookups, {@code null}), and setters throw
 * {@link UnsupportedOperationException}, except when setting that "nothing" value.
 */
public enum Feature {
    /** The database has a name, see {@link Database#getName()} */
    DATABASE_NAME,
    /** Entries may have properties other than {@link Entry#STANDARD_PROPERTY_NAMES} */
    AD_HOC_PROPERTIES,
    /** Entries may have binary properties (attachments) */
    BINARY_PROPERTIES,
    /** Entries may have more than one binary property */
    MULTIPLE_BINARY_PROPERTIES,
    /** The database has a recycle bin */
    RECYCLE_BIN,
    /** Property values are stored according to a {@link PropertyValue.Strategy}, so may be protected */
    PROPERTY_VALUE_STRATEGY
}
