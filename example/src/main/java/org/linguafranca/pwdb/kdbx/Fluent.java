package org.linguafranca.pwdb.kdbx;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.jackson.KdbxDatabase;

public class Fluent {

    public void fluentExample() {
        Database database = new KdbxDatabase();
        database.newGroup("Group 1")
                .addEntry()
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .setProperty("prop3", "value3")
                .getParent()
                .addEntry()
                    .setProperty("prop1", "value1")
                    .setProperty("prop2", "value2")
                    .setProperty("prop3", "value3");

    }
}
