package org.linguafranca.pwdb.kdbx.simple;

import org.linguafranca.pwdb.checks.RecycleBinChecks;

/**
 * @author jo
 */
public class SimpleRecycleBinTest extends RecycleBinChecks {
    public SimpleRecycleBinTest() {
       database = new SimpleDatabase();
    }
}
