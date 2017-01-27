package org.linguafranca.pwdb.kdbx.jaxb;

import org.linguafranca.pwdb.checks.RecycleBinChecks;

/**
 * @author jo
 */
public class JaxbRecycleBinTest extends RecycleBinChecks {
    public JaxbRecycleBinTest() {
       database = new JaxbDatabase();
    }
}
