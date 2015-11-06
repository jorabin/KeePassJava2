package org.linguafranca.keepass.dom;

import org.linguafranca.keepass.db.Icon;
import org.w3c.dom.Element;

/**
 * @author jo
 */
public class DomIconWrapper implements Icon {

    private final Element element;

    public DomIconWrapper(Element element) {
        this.element = element;
    }

    @Override
    public int getIndex() {
        return Integer.parseInt(element.getTextContent());
    }

    @Override
    public void setIndex(int index) {
        element.setTextContent(String.valueOf(index));
    }
}
