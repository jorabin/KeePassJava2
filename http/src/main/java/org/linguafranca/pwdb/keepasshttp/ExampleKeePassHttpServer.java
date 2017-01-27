package org.linguafranca.pwdb.keepasshttp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.pwdb.keepasshttp.PwGenerator.HexPwGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Minimal embedded server - local host origin only but no meaningful security
 */
public class ExampleKeePassHttpServer {
    private static final String DEFAULT_DB_FILE = "HttpDatabase.kdbx";
    public static void main(String[] args) throws Exception {

        Server server = new Server();

        ServerConnector http = new ServerConnector(server);
        http.setHost("127.0.0.1");
        http.setPort(19455);
        http.setIdleTimeout(300000);
        server.addConnector(http);

        KdbxCreds creds = new KdbxCreds("123".getBytes());
        // create a database if we don't have one already
        if (Files.notExists(Paths.get(DEFAULT_DB_FILE))) {
            SimpleDatabase db = new SimpleDatabase();
            db.setName("HTTP Database");
            db.save(creds,new FileOutputStream(DEFAULT_DB_FILE));
        }

        DatabaseAdaptor adaptor = new DatabaseAdaptor.Default(new File(DEFAULT_DB_FILE), creds, new HexPwGenerator(10));
        server.setHandler(new KeePassHttpHandler(adaptor));

        server.start();
        server.join();
    }
 }
