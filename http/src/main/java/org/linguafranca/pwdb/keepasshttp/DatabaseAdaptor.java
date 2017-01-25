package org.linguafranca.pwdb.keepasshttp;

import org.apache.commons.codec.binary.Hex;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.spongycastle.crypto.digests.SHA1Digest;


import java.io.*;
import java.util.UUID;

/**
 * @author jo
 */
public interface DatabaseAdaptor {

    String getId();
    String getHash();
    Database getDatabase();
    PwGenerator getPwGenerator();
    OutputStream getOutputStream();
    Credentials getCredentials();

    class Default implements DatabaseAdaptor {
        private Database database;
        private PwGenerator pwGenerator;
        private File databaseFile;
        private final Credentials credentials;

        Default(File file, Credentials credentials, PwGenerator pwGenerator) throws Exception {
            this.databaseFile = file;
            this.pwGenerator = pwGenerator;
            this.credentials = credentials;
            this.database = SimpleDatabase.load(credentials, new FileInputStream(file));
        }

        @Override
        public String getId() {
            return database.getName() + " (" + database.getRootGroup().getUuid().toString() + ")";
        }

        @Override
        public String getHash() {
            byte[] toHash = Helpers.hexStringFromUuid(database.getRootGroup().getUuid()).getBytes();
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

        @Override
        public PwGenerator getPwGenerator() {
            return pwGenerator;
        }

        @Override
        public OutputStream getOutputStream() {
            try {
                return new FileOutputStream(databaseFile);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Credentials getCredentials() {
            return credentials;
        }
    }
}
