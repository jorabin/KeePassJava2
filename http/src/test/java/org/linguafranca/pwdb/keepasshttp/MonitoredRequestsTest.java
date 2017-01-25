package org.linguafranca.pwdb.keepasshttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * @author jo
 */
public class MonitoredRequestsTest {
    private static String associateRequest = "{\"RequestType\":\"associate\",\"Key\":\"NTxz7Rej/a4H9OFXBmXxMp0BjIfqhy5QEYjLnqhWdkA=\",\"Nonce\":\"UJUjvviWfVDK7c6KzZNUkw==\",\"Verifier\":\"f3DwAdI7rcWAH0DzGlCMTjLtH2q2+eBLNgRuojQWLS8=\"}";
    private static String associateResponse = "{\"RequestType\":\"associate\",\"Success\":true,\"Id\":\"NEW coNECTION\",\"Count\":0,\"Version\":\"1.8.4.1\",\"Hash\":\"eefc4faf792a59d034da69c69f3643ef34ba7d8d\",\"Nonce\":\"/cdtpNsn3sjiwVtrwYmx3g==\",\"Verifier\":\"5onGXGq18yT4BM3QJnqUxXMKJ7/4fnRvjZ6R0OO0RSE=\"}";

    private static String testAssociateRequestWithId = "{\"RequestType\":\"test-associate\",\"TriggerUnlock\":false,\"Id\":\"NEW coNECTION\",\"Nonce\":\"gWgoWCbp6/9YsuvVH3hSPw==\",\"Verifier\":\"NQ0d51+hW4Y7NBB0Ns6QKPg0BSWnHwHzleMKpZsdO54=\"}";
    private static String testAssociateResponseWithId = "{\"RequestType\":\"test-associate\",\"Success\":true,\"Id\":\"NEW coNECTION\",\"Count\":0,\"Version\":\"1.8.4.1\",\"Hash\":\"eefc4faf792a59d034da69c69f3643ef34ba7d8d\",\"Nonce\":\"HMtkcQjmeRxxm8/GLcPsAQ==\",\"Verifier\":\"t+wp+yIoG497STtDUf7st9yGSLStHglihAO+GMFHnzg=\"}";

    private static String getLoginsRequest = "{\"RequestType\":\"get-logins\",\"SortSelection\":\"true\",\"TriggerUnlock\":\"false\",\"Id\":\"NEW coNECTION\",\"Nonce\":\"79y7EyDVndGn3B5iFLp4Kw==\",\"Verifier\":\"LIY9XDHscI+xgrzIVCFnqsW9yigiNzWbJ1ZpOIBKGVs=\",\"Url\":\"eZpW5dUli05H5l1+MuwiUNjXZyNemZDmQOFjgkz8uvg=\",\"SubmitUrl\":\"eZpW5dUli05H5l1+MuwiUO2/H/Faw6yvjvgrCElBRSSbGRJ3a4hXFdq0ws75fK26XT9SQQXqwOvCiTiK5Ybnxg==\"}";
    private static String getLoginsResponse = "{\"RequestType\":\"get-logins\",\"Success\":true,\"Id\":\"NEW coNECTION\",\"Count\":1,\"Version\":\"1.8.4.1\",\"Hash\":\"eefc4faf792a59d034da69c69f3643ef34ba7d8d\",\"Nonce\":\"hQ2JSllJLrVWv1LcxN8+rQ==\",\"Verifier\":\"TT2014GvCXk+bruePydwqiGFeupP+xv7F7UnC5lc7vo=\",\"Entries\":[{\"Login\":\"n1qi36EQkTQhzQiT7TFonA==\",\"Password\":\"gvgmfbelEa3QxvzRsEG0Hg==\",\"Uuid\":\"w5MJa4za3DLh+2CmbG9Yl5kr5Gba0wnptN24kZPsWHPA8hPx6Pb1XYxiseh5gARY\",\"Name\":\"xpCqAwBjmUOqVyI6dfJqGA==\"}]}";

    Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    File tempFile;

    public MonitoredRequestsTest() {
        KdbxCreds creds = new KdbxCreds("123".getBytes());

        SimpleDatabase db = new SimpleDatabase();
        db.setName("Test Database");
        try {
            tempFile = File.createTempFile("pwdb", "tmp");
            db.save(creds, new FileOutputStream(tempFile));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void sessionTest() throws Exception {
        Processor processor = new Processor(new DatabaseAdaptor.Default(tempFile,
                new KdbxCreds("123".getBytes()),
                new PwGenerator() {
                    @Override
                    public String generate() {
                        return "123";
                    }
                }));

        Message.Request request = gson.fromJson(associateRequest, Message.Request.class);
        Crypto crypto = new Crypto(request.Key);
        Message.Response actualResponse = gson.fromJson(associateResponse, Message.Response.class);

        Message.Response computedResponse = new Message.Response(request.RequestType, "eefc4faf792a59d034da69c69f3643ef34ba7d8d");
        processor.process(request, computedResponse);
        computedResponse.Id = "NEW coNECTION";

        crypto.makeVerifiable(computedResponse);

        System.out.println(gson.toJson(computedResponse));
        crypto.verify(computedResponse);
        System.out.println(gson.toJson(actualResponse));
        crypto.verify(actualResponse);


        request = gson.fromJson(testAssociateRequestWithId, Message.Request.class);
        actualResponse = gson.fromJson(testAssociateResponseWithId, Message.Response.class);

        computedResponse = new Message.Response(request.RequestType, "eefc4faf792a59d034da69c69f3643ef34ba7d8d");
        processor.process(request, computedResponse);
        crypto.makeVerifiable(computedResponse);
        computedResponse.Id = "NEW coNECTION";

        System.out.println(gson.toJson(computedResponse));
        crypto.verify(computedResponse);
        System.out.println(gson.toJson(actualResponse));
        crypto.verify(actualResponse);


        request = gson.fromJson(getLoginsRequest, Message.Request.class);
        actualResponse = gson.fromJson(getLoginsResponse, Message.Response.class);

        computedResponse = new Message.Response(request.RequestType, "eefc4faf792a59d034da69c69f3643ef34ba7d8d");
        processor.process(request, computedResponse);
        crypto.makeVerifiable(computedResponse);
        computedResponse.Id = "NEW coNECTION";

        System.out.println(gson.toJson(computedResponse));
        crypto.verify(computedResponse);
        System.out.println(gson.toJson(actualResponse));
        crypto.verify(actualResponse);
        byte[] iv = Helpers.decodeBase64Content(actualResponse.Nonce.getBytes(), false);
        for (Message.ResponseEntry entry : actualResponse.Entries) {
            System.out.println(crypto.decryptFromBase64(entry.Login, iv));
            System.out.println(crypto.decryptFromBase64(entry.Name, iv));
            System.out.println(crypto.decryptFromBase64(entry.Password, iv));
            System.out.println(crypto.decryptFromBase64(entry.Uuid, iv));
        }


    }
}
