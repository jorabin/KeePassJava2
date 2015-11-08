package org.linguafranca.keepass;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Jo
 */
public abstract class AbstractGroup implements Group {

    @Override
    public List<Group> findGroups(String group1) {
        ArrayList<Group> result = new ArrayList<>();
        for (Group g: getGroups()) {
            if (g.getName().equals(group1)) {
                result.add(g);
            }
        }
        return result;
    }

    @Override
    public Group findGroup(Group group) {
        for (Group g: getGroups()) {
            if (g.getUuid().equals(group.getUuid())) {
                return g;
            }
        }
        return null;
    }

    @Override
    public List<Entry> findEntries(String find, boolean recursive) {
        List <Entry> result = new ArrayList<>(getEntries().size());
        for (Entry entry: getEntries()){
            if (entry.match(find)){
                result.add(entry);
            }
        }
        if (recursive) {
            for (Group group : getGroups()) {
                result.addAll(group.findEntries(find, true));
            }
        }
        return result;
    }

    @Override
    public List<Entry> findEntries(Entry.Matcher matcher, boolean recursive) {
        List <Entry> result = new ArrayList<>(getEntries().size());
        for (Entry entry: getEntries()){
            if (entry.match(matcher)){
                result.add(entry);
            }
        }
        if (recursive) {
            for (Group group : getGroups()) {
                result.addAll(group.findEntries(matcher, true));
            }
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

    @Override
    public String toString() {
        return this.getPath();
    }
}
