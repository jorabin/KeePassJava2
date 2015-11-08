package org.linguafranca.keepass.kdb;

import org.linguafranca.keepass.Icon;

/**
 * Contains the index of an icon
 *
 * @author jo
 */
public class KdbIcon implements Icon {
    int index = 0;

    KdbIcon(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Icon)) return false;

        Icon icon = (Icon) o;

        return index == icon.getIndex();

    }
}
