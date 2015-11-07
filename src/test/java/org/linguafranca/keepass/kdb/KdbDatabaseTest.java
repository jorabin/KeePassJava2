package org.linguafranca.keepass.kdb;

import org.linguafranca.keepass.BasicDatabaseChecks;

/**
 * @author Jo
 */
public class KdbDatabaseTest  extends BasicDatabaseChecks {
    public KdbDatabaseTest() {
        super(new KdbDatabase());
    }
}
