package org.linguafranca.keepass.dom;

import org.linguafranca.keepass.db.Group;
import org.linguafranca.keepass.db.Icon;
import org.linguafranca.keepass.db.base.AbstractEntry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.DatatypeConverter;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.linguafranca.keepass.dom.DomHelper.*;

/**
 * @author jo
 */
public class DomEntryWrapper extends AbstractEntry {

    static Map<String, ValueCreator> mandatoryEntryElements = new HashMap<String, ValueCreator>() {{
        put(UUID_ELEMENT_NAME, new UuidValueCreator());
        put(ICON_ELEMENT_NAME, new ConstantValueCreator("2"));
        put(TIMES_ELEMENT_NAME, new ConstantValueCreator(""));
        put(LAST_MODIFICATION_TIME_ELEMENT_NAME, new DateValueCreator());
        put(CREATION_TIME_ELEMENT_NAME, new DateValueCreator());
        put(LAST_ACCESS_TIME_ELEMENT_NAME, new DateValueCreator());
        put(EXPIRY_TIME_ELEMENT_NAME, new DateValueCreator());
        put(EXPIRES_ELEMENT_NAME, new ConstantValueCreator("False"));
        put(USAGE_COUNT_ELEMENT_NAME, new ConstantValueCreator("0"));
        put(LOCATION_CHANGED, new DateValueCreator());
    }};

    final Element element;
    final DomDatabaseWrapper database;

    public DomEntryWrapper(Element element, DomDatabaseWrapper database) {
        this.element = element;
        this.database = database;
        ensureElements(element, mandatoryEntryElements);
        ensureProperty("Notes");
        ensureProperty("Title");
        ensureProperty("URL");
        ensureProperty("UserName");
        ensureProperty("Password");
    }

    @Override
    public String getProperty(String name) {
        Element property = getElement(String.format(PROPERTY_ELEMENT_FORMAT, name), element, false);
        if (property == null) {
            return null;
        }
        boolean protect = false;
        Element valueElement = getElement(VALUE_ELEMENT_NAME, property, false);
        if (valueElement != null && valueElement.hasAttribute("Protected")) {
            protect = Boolean.valueOf(valueElement.getAttribute("Protected"));
        }
        if (protect) {
            return database.decrypt(getElementContent(VALUE_ELEMENT_NAME, property));
        }
        return getElementContent(VALUE_ELEMENT_NAME, property);
    }

    @Override
    public void setProperty(String name, String value) {
        Element property = getElement(String.format(PROPERTY_ELEMENT_FORMAT, name), element, false);
        if (property == null) {
            property = newElement("String", element);
            setElementContent("Key", property, name);
        }

        if (database.isProtected(name)) {
            value = database.encrypt(value);
            Element valueElement = getElement(VALUE_ELEMENT_NAME, property, true);
            valueElement.setAttribute("Protected", "True");
        }
        setElementContent(VALUE_ELEMENT_NAME, property, value);
        touchElement(LAST_MODIFICATION_TIME_ELEMENT_NAME, element);
    }

    private void ensureProperty(String name){
        Element property = getElement(String.format(PROPERTY_ELEMENT_FORMAT, name), element, false);
        if (property == null) {
            Element container = newElement("String", element);
            setElementContent("Key", container, name);
            getElement("Value", container, true);
        }
    }

    @Override
    public Group getParent() {
        if (element.getParentNode() == null) {
            return null;
        }
        return new DomGroupWrapper((Element) element.getParentNode(), database);
    }

    @Override
    public UUID getUuid() {
        return uuidFromBase64(getElementContent(UUID_ELEMENT_NAME, element));
    }

    @Override
    public String getUsername() {
        return getProperty("UserName");
    }

    @Override
    public void setUsername(String username) {
        setProperty("UserName", username);
    }

    @Override
    public String getPassword() {
        return getProperty("Password");
    }

    @Override
    public void setPassword(String pass) {
        setProperty("Password", pass);
    }

    @Override
    public String getUrl() {
        return getProperty("URL");
    }

    @Override
    public void setUrl(String url) {
        setProperty("URL", url);
    }

    @Override
    public String getTitle() {
        return getProperty("Title");
    }

    @Override
    public void setTitle(String title) {
        setProperty("Title", title);
    }

    @Override
    public String getNotes() {
        return getProperty("Notes");
    }

    @Override
    public void setNotes(String notes) {
        setProperty("Notes", notes);
    }

    @Override
    public Icon getIcon() {
        return new DomIconWrapper(getElement(ICON_ELEMENT_NAME, element, false));
    }

    @Override
    public void setIcon(Icon icon) {
        getElement(ICON_ELEMENT_NAME, element, true).setTextContent(String.valueOf(icon.getIndex()));
        touchElement(LAST_MODIFICATION_TIME_ELEMENT_NAME, element);
    }

    @Override
    public Date getLastAccessTime() {
        try {
            return dateFormatter.parse(getElementContent(LAST_ACCESS_TIME_ELEMENT_NAME, element));
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    public Date getCreationTime() {
        try {
            return dateFormatter.parse(getElementContent(CREATION_TIME_ELEMENT_NAME, element));
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    public Date getExpiryTime() {
        try {
            return dateFormatter.parse(getElementContent(EXPIRY_TIME_ELEMENT_NAME, element));
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    @Override
    public Date getLastModificationTime() {
        try {
            return dateFormatter.parse(getElementContent(LAST_MODIFICATION_TIME_ELEMENT_NAME, element));
        } catch (ParseException e) {
            return new Date(0);
        }
    }
}
