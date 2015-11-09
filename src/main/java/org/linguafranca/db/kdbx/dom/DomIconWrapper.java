package org.linguafranca.db.kdbx.dom;

import org.linguafranca.db.Icon;
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
        if (o == null || !(o instanceof Icon)) return false;

        Icon that = (Icon) o;

        return this.getIndex() == that.getIndex();

    }
}
