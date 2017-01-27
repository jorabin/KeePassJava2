package org.linguafranca.pwdb.keepasshttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.keepasshttp.util.LogginInputStream;
import org.linguafranca.pwdb.keepasshttp.util.LogginOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Jetty Handler for PassIFox and ChromeIPass clients - emulates KeePassHttp plugin.
 */
public class KeePassHttpHandler extends AbstractHandler {

    private final DatabaseAdaptor adaptor;
    private final Processor processor;
    private Logger logger = LoggerFactory.getLogger(KeePassHttpHandler.class);
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private Crypto crypto = new Crypto();

    KeePassHttpHandler(DatabaseAdaptor adaptor) {
        this.adaptor = adaptor;
        this.processor = new Processor(adaptor);
    }

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

        logger.debug("Got a request");

        InputStream is = new LogginInputStream(request.getInputStream(), logger);
        OutputStream outputStream = new LogginOutputStream(httpServletResponse.getOutputStream(), logger);
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        Message.Request request1 = gson.fromJson(new BufferedReader(new InputStreamReader(is)),Message.Request.class);
        if (request1 == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setHandled(true);
            writer.write("That's a 400. JSON not parsed. " + request.getRemoteAddr());
            return;
        }
        if (request1.RequestType == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setHandled(true);
            writer.write("That's a 400. No request type found. " + request.getRemoteAddr());
            return;
        }

        Message.Response response = new Message.Response(request1.RequestType, adaptor.getHash());

        // set the crypto key on associate
        if (request1.RequestType.equals(Message.Type.ASSOCIATE)) {
            crypto.setKey(Helpers.decodeBase64Content(request1.Key.getBytes(), false));
        }

        // send OK even when it's fail
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        // normal part of the protocol to fail verification on test-associate
        if (!crypto.verify(request1)) {
            logger.debug("Request failed verification");
            response.Success = false;
        } else {
            try {
                // processor is responsible for setting success
                processor.process(request1, response);
                response.Id = adaptor.getId();
            } catch (Exception e) {
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.Success = false;
                response.Error = "Error processing request " + e.getMessage();
            }
        }
        // presumably errors need to be verifiable?
        crypto.makeVerifiable(response);
        gson.toJson(response, writer);
        writer.flush();
        request.setHandled(true);
    }
}
