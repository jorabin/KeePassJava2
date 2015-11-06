package org.linguafranca.keepass.db.base;

import org.linguafranca.keepass.db.Entry;
import org.linguafranca.keepass.db.Group;

import java.util.Stack;

/**
 * @author Jo
 */
public abstract class AbstractEntry implements Entry {
    public  boolean matchTitle(String text){
        return (getTitle().toLowerCase().contains(text.toLowerCase()));
    }

    public  boolean matchNote(String text){
        return (getNotes().toLowerCase().contains(text.toLowerCase()));
    }

    public boolean match(String text) {
        return matchTitle(text) || matchNote(text);
    }

    public boolean match(Entry.Matcher matcher) {
        return matcher.matches(this);
    }

    public String getPath() {
        Group parent = this.getParent();
        String result = "";
        if (parent != null) {
            result = parent.getPath();
        }
        return result + getTitle();
    }

    public String toString() {
        return this.getPath();
    }
}
