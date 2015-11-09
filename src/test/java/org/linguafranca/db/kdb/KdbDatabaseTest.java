package org.linguafranca.db.kdb;

import org.linguafranca.db.BasicDatabaseChecks;

/**
 * @author Jo
 */
public class KdbDatabaseTest  extends BasicDatabaseChecks {
    public KdbDatabaseTest() {
        super(new KdbDatabase());
    }
}
