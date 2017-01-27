package org.linguafranca.pwdb.keepasshttp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Message definitions for the protocol
 * <p>
 * Note non idiomatic case on field names, this allows correct
 * serialization as JSON without annotations
 */
@SuppressWarnings("WeakerAccess")
public class Message {

    /**
     * Protocol version
     */
    public static final String VERSION = "1.8.4.1";

    /**
     * The valid message types
     */
    public static final class Type {
        public static final String GET_LOGINS = "get-logins";
        public static final String GET_LOGINS_COUNT = "get-logins-count";
        public static final String GET_ALL_LOGINS = "get-all-logins";
        public static final String SET_LOGIN = "set-login";
        public static final String ASSOCIATE = "associate";
        public static final String TEST_ASSOCIATE = "test-associate";
        public static final String GENERATE_PASSWORD = "generate-password";

    }

    /**
     * Base class for requests and responses, they can be cryptographically verified
     */
    public static class Verifiable {
        /**
         * Nonce value used in conjunction with all encrypted fields,
         * randomly generated for each request
         */
        public String Nonce;

        /**
         * Used to check that the correct key has been chosen
         */
        public String Verifier;
    }

    /**
     * From client to server
     */
    public static class Request extends Verifiable {

        public String RequestType;

        /**
         * Sort selection by best URL matching for given hosts
         */
        public String SortSelection;

        /**
         * Trigger unlock of database even if feature is disabled in KPH (because of user interaction to fill-in)
         */
        public String TriggerUnlock;

        /**
         * Always encrypted, used with set-login, uuid is set
         * if modifying an existing login
         */
        public String Login;
        public String Password;
        public String Uuid;

        /**
         * Always encrypted, used with get and set-login
         */
        public String Url;

        /**
         * Always encrypted, used with get-login
         */
        public String SubmitUrl;

        /**
         * Send the AES key ID with the 'associate' request
         */
        public String Key;

        /**
         * Always required, an identifier given by the KeePass user
         */
        public String Id;

        /**
         * Realm value used for filtering results.  Always encrypted.
         */
        public String Realm;
    }

    /**
     * From server to client
     */
    public static class Response extends Verifiable {
        public Response(String request, String hash) {
            RequestType = request;

            if (request.equals(Type.GET_LOGINS) ||
                    request.equals(Type.GET_ALL_LOGINS) ||
                    request.equals(Type.GENERATE_PASSWORD))
                Entries = new ArrayList<>();
            else
                Entries = null;

            this.Hash = hash;
            this.Version = VERSION;
        }

        /**
         * Mirrors the request type of KeePassRequest
         */
        public String RequestType;

        public String Error;

        public boolean Success = false;

        /**
         * The user selected String as a result of 'associate',
         * always returned on every request
         */
        public String Id;

        /**
         * response to get-logins-count, number of entries for requested Url
         */
        public Integer Count=0;

        /**
         * response the current version of KeePassHttp
         */
        public String Version = "";

        /**
         * response an unique hash of the database composed of RootGroup UUid and RecycleBin UUid
         */
        public String Hash = "";

        /**
         * The resulting entries for a get-login request
         */
        public List<ResponseEntry> Entries;

    }

    public static class ResponseEntry {
        public ResponseEntry() {
        }

        public ResponseEntry(String name, String login, String password, String uuid) {
            this(name, login, password, uuid, new ArrayList<Message.ResponseStringField>());
        }

        public ResponseEntry(String name, String login, String password, String uuid, List<ResponseStringField> StringFields) {
            this.Login = login;
            this.Password = password;
            this.Uuid = uuid;
            this.Name = name;
            this.StringFields = StringFields;
        }

        public String Login;
        public String Password;
        public String Uuid;
        public String Name;
        public List<ResponseStringField> StringFields = null;

    }

    public static class ResponseStringField {
        public ResponseStringField() {
        }

        public ResponseStringField(String key, String value) {
            Key = key;
            Value = value;
        }

        public String Key;
        public String Value;
    }

    static class KeePassHttpEntryConfig {
        public HashSet<String> Allow = new HashSet<String>();
        public HashSet<String> Deny = new HashSet<String>();
        public String Realm = null;
    }
}
