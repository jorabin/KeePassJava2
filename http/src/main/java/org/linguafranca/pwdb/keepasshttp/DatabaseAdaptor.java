package org.linguafranca.pwdb.keepasshttp;

import org.apache.commons.codec.binary.Hex;
import org.linguafranca.pwdb.Credentials;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.spongycastle.crypto.digests.SHA1Digest;

import java.io.*;

/**
 * Adaptor for {@link Database} supporting the requirements of the KeePassHttp protocol.
 */
public interface DatabaseAdaptor {

    /**
     * The Id by which this database is known following association
     */
    String getId();

    /**
     * Each database has a Hash
     */
    String getHash();

    /**
     * A password generator
     */
    PwGenerator getPwGenerator();

    /**
     * Where to save, when saving. To be closed by caller.
     */
    OutputStream getOutputStream();

    /**
     * Credentials to use, when saving
     */
    Credentials getCredentials();

    /**
     * The underlying Database
     */
    Database getDatabase();

    /**
     * Default implementation of Adaptor
     */
    class Default implements DatabaseAdaptor {
        private final Database database;
        private final PwGenerator pwGenerator;
        private final File databaseFile;
        private final Credentials credentials;

        /**
         * Constructor for Databse from File
         * @param file the file containing the databse
         * @param credentials credentials for the databse
         * @param pwGenerator a password generator
         * @throws Exception if the database can't be constructed
         */
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

        // in the C# version this is a hash of the root group UUID and the recycle bin UUID
        // we don't have the concept of recycle bin (yet)
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
