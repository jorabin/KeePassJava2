package org.linguafranca.pwdb.kdbx.dom;

import org.linguafranca.pwdb.checks.RecycleBinChecks;

import java.io.IOException;

/**
 * @author jo
 */
public class DomRecycleBinTest extends RecycleBinChecks {
    public DomRecycleBinTest() throws IOException {
       database = new DomDatabaseWrapper();
    }
}
