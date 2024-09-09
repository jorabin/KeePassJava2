package org.linguafranca.pwdb.protect;

import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.base.AbstractDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base class for Databases which support storage using {@link PropertyValue}s.
 * <p>
 * By default {@link Entry#STANDARD_PROPERTY_NAME_PASSWORD} is defined as protected and the property value strategy
 * establishes {@link org.linguafranca.pwdb.PropertyValue.StringStore} storage for unprotected values and
 * {@link org.linguafranca.pwdb.PropertyValue.SealedStore} for protected values.
 */
public abstract class ProtectedDatabase<D extends Database<D, G, E, I>, G extends Group<D, G, E, I>, E extends Entry<D,G,E,I>, I extends Icon> extends AbstractDatabase<D,G,E,I> {
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private final List<String> protectedProperties = new ArrayList<>(Arrays.asList(Entry.STANDARD_PROPERTY_NAME_PASSWORD));
    private PropertyValue.Strategy valueStrategy = new PropertyValue.Strategy() {
        @Override
        public PropertyValue.Factory getUnprotectectedValueFactory() {
            return new PropertyValue.StringStore.Factory();
        }

        @Override
        public PropertyValue.Factory getProtectectedValueFactory() {
            return new PropertyValue.SealedStore.Factory();
        }
    };

    @Override
    public boolean shouldProtect(String propertyName){
        return protectedProperties.contains(propertyName);
    }

    @Override
    public void setShouldProtect(String propertyName, boolean protect){
        if (protect) {
            protectedProperties.add(propertyName);
        } else {
            protectedProperties.remove(propertyName);
        }
    }

    @Override
    public List<String> listShouldProtect(){
        return Collections.unmodifiableList(protectedProperties);
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
