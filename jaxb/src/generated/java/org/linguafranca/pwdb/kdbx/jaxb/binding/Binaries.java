//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.05.17 at 11:23:19 AM BST 
//


package org.linguafranca.pwdb.kdbx.jaxb.binding;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Binary field for elements are centrally stored in this element.
 *                 The same Binary element may be referenced from many Entries.
 *             
 * 
 * <p>Java class for binaries complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="binaries">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="Binary">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>base64Binary">
 *                 &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="Compressed" type="{}keepassBoolean" default="False" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "binaries", propOrder = {
    "binary"
})
public class Binaries {

    @XmlElement(name = "Binary")
    protected List<Binaries.Binary> binary;

    /**
     * Gets the value of the binary property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the binary property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBinary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Binaries.Binary }
     * 
     * 
     */
    public List<Binaries.Binary> getBinary() {
        if (binary == null) {
            binary = new ArrayList<Binaries.Binary>();
        }
        return this.binary;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>base64Binary">
     *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="Compressed" type="{}keepassBoolean" default="False" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Binary {

        @XmlValue
        protected byte[] value;
        @XmlAttribute(name = "ID")
        protected Integer id;
        @XmlAttribute(name = "Compressed")
        @XmlJavaTypeAdapter(Adapter2 .class)
        protected Boolean compressed;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setValue(byte[] value) {
            this.value = value;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link Integer }
         *     
         */
        public Integer getID() {
            return id;
        }

        /**
         * Sets the value of the id property.
         * 
         * @param value
         *     allowed object is
         *     {@link Integer }
         *     
         */
        public void setID(Integer value) {
            this.id = value;
        }

        /**
         * Gets the value of the compressed property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public Boolean getCompressed() {
            if (compressed == null) {
                return new Adapter2().unmarshal("False");
            } else {
                return compressed;
            }
        }

        /**
         * Sets the value of the compressed property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCompressed(Boolean value) {
            this.compressed = value;
        }

    }

}
