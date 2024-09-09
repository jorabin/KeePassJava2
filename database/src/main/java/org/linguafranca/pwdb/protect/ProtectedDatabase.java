package org.linguafranca.pwdb.protect;

import org.linguafranca.pwdb.*;
import org.linguafranca.pwdb.base.AbstractDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ProtectedDatabase<D extends Database<D, G, E, I>, G extends Group<D, G, E, I>, E extends Entry<D,G,E,I>, I extends Icon> extends AbstractDatabase<D,G,E,I> {
    private final List<String> protectedProperties = new ArrayList<>();
    private PropertyValue.Strategy valueStrategy;

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
