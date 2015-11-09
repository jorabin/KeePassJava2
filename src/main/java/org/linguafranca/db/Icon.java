package org.linguafranca.db;

/**
 * Interface for icons.
 *
 * <p>Intended as a point of flexibility, at some point this
 * may get embellished to allow custom icons and retrieval
 * of icon assets and so on.
 *
 * @author Jo
 */
public interface Icon {
    int getIndex();

    void setIndex(int index);

}
