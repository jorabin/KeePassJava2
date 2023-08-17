package org.linguafranca.pwdb.kdbx.jackson;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "History")
public class JacksonHistory {
    
    @JacksonXmlProperty(localName = "Entry") /** Workaround jackson  **/
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<JacksonEntry> entry;

    public JacksonHistory() {

    }

    /**
     * @return the entry
     */
    public List<JacksonEntry> getEntry() {
        return entry;
    }

    /**
     * @param entry the entry to set
     */
    public void setEntry(List<JacksonEntry> entry) {
        this.entry = entry;
    }


    


}
