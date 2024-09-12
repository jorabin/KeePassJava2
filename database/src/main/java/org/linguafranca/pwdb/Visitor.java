/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.pwdb;

import java.io.PrintStream;

/**
 * Interface for implementing a visitor for Groups, their sub-Groups and their Entries.
 *
 * @author jo
 */
public interface Visitor <G extends Group<G, E>, E extends Entry<G, E>> {
    /**
     * Called on entry to a group visit
     * @param group the group being visited
     */
    void startVisit(G group);

    /**
     * Called on exit from a group visit
     * @param group the group being exited
     */
    void endVisit(G group );

    /**
     * Called on visit to an entry
     * @param entry the entry being visited
     */
    void visit(E entry);

    /**
     * called to determine whether to visit entries before subgroups, or not
     * @return true to visit
     */
    boolean isEntriesFirst();

    /**
     * Empty implementation of Visitor
     */
    abstract class Default <G extends Group<G, E>, E extends Entry<G, E>>  implements Visitor <G, E> {

        @Override
        public void startVisit(G group) {}

        @Override
        public void endVisit(G group) {}

        @Override
        public void visit(E entry) {}

        @Override
        public boolean isEntriesFirst() {
            return true;
        }
    }

    /**
     * Visitor prints the Groups and Entries it visits to console
     */
    class Print <G extends Group<G, E>, E extends Entry<G, E>>  extends Default<G, E> {

        private final PrintStream printStream;

        public Print() {
            this(System.out);
        }

        public Print(PrintStream out) {
            this.printStream = out;
        }

        @Override
        public void startVisit(G group ) {
            printStream.println(group.toString());
        }

        @Override
        public void visit(E entry) {
            printStream.println(entry.toString());
        }

    }
}
