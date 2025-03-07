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

package org.linguafranca.pwdb.protect;

import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.base.AbstractDatabase;

import java.util.Collections;
import java.util.List;

/**
 * Base class for Databases which support storage using {@link PropertyValue}s.
 * <p>
 * By default {@link Entry#STANDARD_PROPERTY_NAME_PASSWORD} is defined as protected and the property value strategy
 * establishes {@link org.linguafranca.pwdb.PropertyValue.StringStore} storage for unprotected values and
 * {@link org.linguafranca.pwdb.PropertyValue.SealedStore} for protected values.
 */
public abstract class ProtectedDatabase extends AbstractDatabase {
    private PropertyValue.Strategy valueStrategy = new PropertyValue.Strategy.Default();

    @Override
    public boolean shouldProtect(String propertyName){
        return valueStrategy.getProtectedProperties().contains(propertyName);
    }

    @Override
    public void setShouldProtect(String propertyName, boolean protect){
        if (protect) {
            valueStrategy.getProtectedProperties().add(propertyName);
        } else {
            valueStrategy.getProtectedProperties().remove(propertyName);
        }
    }

    @Override
    public List<String> listShouldProtect(){
        return Collections.unmodifiableList(valueStrategy.getProtectedProperties());
    }

    /**
     * Get the default means of storage of unprotected and protected property values
     */
    @Override
    public PropertyValue.Strategy getPropertyValueStrategy(){
        return this.valueStrategy;
    }
    /**
     * Set the default means of storage of unprotected and protected property values
     * @param strategy a property value strategy
     */
    @Override
    public void setPropertyValueStrategy(PropertyValue.Strategy strategy){
        this.valueStrategy = strategy;
    }

    @Override
    public boolean supportsPropertyValueStrategy(){
        return true;
    }
}
