package org.linguafranca.pwdb.kdbx.jaxb.base;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 * providing a way of flagging protection
 */
public abstract class ValueBinding {

    @XmlTransient
    public boolean protectOnOutput;

    protected String value;

    public String getValue(){
        return value;
    }

    public void setValue(String string){
        value = string;
    }

}
