package org.linguafranca.pwdb.keepasshttp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Embedded server
 */
public class KeePassHttpServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        ServerConnector http = new ServerConnector(server);
        http.setHost("0.0.0.0");
        http.setPort(19455);
        http.setIdleTimeout(300000);
        server.addConnector(http);
        server.setHandler(new KeePassHttpHandler(new SimpleDatabase(), new PwGenerator() {
            @Override
            public String generate() {
                String[] symbols = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
                int length = 10;
                Random random = new SecureRandom();
                StringBuilder sb = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                    int indexRandom = random.nextInt( symbols.length );
                    sb.append( symbols[indexRandom] );
                }
                return sb.toString();
            }
        }));
        server.start();
        server.join();
    }
}
