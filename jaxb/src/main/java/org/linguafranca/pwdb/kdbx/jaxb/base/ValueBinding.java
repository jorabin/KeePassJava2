package org.linguafranca.pwdb.kdbx.jaxb.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 * Ancestor class of StringField.Value, providing a way of flagging protection
 * separate from @Protected and @ProtectInMemory
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ValueBinding {

    @XmlTransient
    public boolean protectOnOutput;

    @XmlValue
    public String value;

    public String getValue(){
        return value;
    }

    public byte[] getValueAsByte() {
        return value.getBytes();
    }

    public void setValue(String string){
        value = string;
    }

}
