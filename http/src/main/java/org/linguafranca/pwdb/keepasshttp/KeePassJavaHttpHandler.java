package org.linguafranca.pwdb.keepasshttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.keepasshttp.util.LogginInputStream;
import org.linguafranca.pwdb.keepasshttp.util.LogginOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.digests.SHA1Digest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * @author jo
 */
public class KeePassJavaHttpHandler extends AbstractHandler {

    private Logger logger = LoggerFactory.getLogger(KeePassJavaHttpHandler.class);
    private Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    Processor processor = new Processor();

    @Override
    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {

        logger.debug("Got a request");
        InputStream is = new LogginInputStream(request.getInputStream(), logger);

        Message.Request request1 = gson.fromJson(new BufferedReader(new InputStreamReader(is)),Message.Request.class);
        if (request1 == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setHandled(true);
            httpServletResponse.getWriter().write("That's a 400. JSON not parsed. " + request.getRemoteAddr());
            return;
        }
        if (request1.RequestType == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setHandled(true);
            httpServletResponse.getWriter().write("That's a 400. No request type found. " + request.getRemoteAddr());
            return;
        }

        Message.Response response = new Message.Response(request1.RequestType, processor.getHash());
        Processor.RequestHandler handler = processor.getHandler(request1.RequestType);

        if (handler == null) {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setHandled(true);
            response.Success = false;
            response.Error = "No valid request type found " + request1.RequestType;
            httpServletResponse.getWriter().write(gson.toJson(response));
            return;
        }

        if (request1.RequestType.equals(Message.Type.ASSOCIATE)) {
            processor.getCrypto().setKey(Helpers.decodeBase64Content(request1.Key.getBytes(), false));
        }

        if (request1.RequestType.equals(Message.Type.TEST_ASSOCIATE) && request1.Id == null ||
                !processor.getCrypto().verify(request1)) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.Success = false;
            response.Error = "Request did not verify";
            processor.getCrypto().makeVerifiable(response);
            httpServletResponse.getWriter().write(gson.toJson(response));
            request.setHandled(true);
            return;
        }

        handler.process(request1, response);

        processor.getCrypto().makeVerifiable(response);

        OutputStream outputStream = new LogginOutputStream(httpServletResponse.getOutputStream(), logger);
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        gson.toJson(response, writer);
        writer.flush();
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        request.setHandled(true);
    }

}
