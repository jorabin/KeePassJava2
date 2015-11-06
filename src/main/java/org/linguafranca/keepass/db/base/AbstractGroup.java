package org.linguafranca.keepass.db.base;

import org.linguafranca.keepass.db.Entry;
import org.linguafranca.keepass.db.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Jo
 */
public abstract class AbstractGroup implements Group {
    public List<Group> findGroups(String group1) {
        ArrayList<Group> result = new ArrayList<>();
        for (Group g: getGroups()) {
            if (g.getName().equals(group1)) {
                result.add(g);
            }
        }
        return result;
    }

    public Group findGroup(Group group) {
        for (Group g: getGroups()) {
            if (g.getUuid().equals(group.getUuid())) {
                return g;
            }
        }
        return null;
    }

    public List<Entry> findEntries(String entry1) {
        List <Entry> result = new ArrayList<>(getEntries().size());
        for (Entry entry: getEntries()){
            if (entry.match(entry1)){
                result.add(entry);
            }
        }
        return result;
    }

    public List<Entry> findEntries(Entry.Matcher matcher, boolean recursive) {
        List <Entry> result = new ArrayList<>(getEntries().size());
        for (Entry entry: getEntries()){
            if (entry.match(matcher)){
                result.add(entry);
            }
        }
        if (!recursive) {
            return result;
        }
        for (Group group: getGroups()){
            result.addAll(group.findEntries(matcher, recursive));
        }
        return result;
    }

    @Override
    public String getPath() {
        Stack<Group> parents = new Stack<>();
        Group parent = this;
        parents.push(this);
        while ((parent=parent.getParent()) != null) {
            parents.push(parent);
        }
        String result = "/";
        while (parents.size() > 0) {
            result = result + parents.pop().getName() + "/";
        }
        return result;
    }

    public String toString() {
        return this.getPath();
    }
}
