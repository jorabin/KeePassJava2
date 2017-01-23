package org.linguafranca.pwdb.keepasshttp;

import org.apache.commons.codec.binary.Hex;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.spongycastle.crypto.digests.SHA1Digest;


import java.util.UUID;

/**
 * @author jo
 */
public interface DatabaseAdaptor {

    String getId();
    String getHash();
    Database getDatabase();

    class Default implements DatabaseAdaptor {
        private String id = "402881E9-58B6-5A30-0158-B65A30B20000";
        private UUID rootGroupUuid = UUID.fromString("402881E9-58B6-5A30-0158-B65AFC580001");
        private UUID recycleBinUuid = UUID.fromString("402881E9-58B6-5A30-0158-B65BC8D30002");
        private Database database = new SimpleDatabase();

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getHash() {
            byte[] toHash = (Helpers.hexStringFromUuid(rootGroupUuid) + Helpers.hexStringFromUuid(recycleBinUuid)).getBytes();
            SHA1Digest digest = new SHA1Digest();
            byte[] digestBytes = new byte[digest.getDigestSize()];
            digest.update(toHash, 0, toHash.length);
            digest.doFinal(digestBytes, 0);
            String result = new String(Hex.encodeHex(digestBytes));
            return result.toLowerCase();
        }

        @Override
        public Database getDatabase() {
            return database;
        }
    }
}
