package org.linguafranca.keepass.dom;

import org.linguafranca.keepass.Icon;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomIconWrapper that = (DomIconWrapper) o;

        return this.getIndex() == that.getIndex();

    }
}
