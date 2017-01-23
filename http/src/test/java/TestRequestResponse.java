import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.keepasshttp.Crypto;
import org.linguafranca.pwdb.keepasshttp.Processor;
import org.linguafranca.pwdb.keepasshttp.Message;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author jo
 */
public class TestRequestResponse {
    private static String requestString = "{\"RequestType\":\"associate\",\"Key\":\"WQISYVajkviMNaSBIaUTgZbUaJD1RujU6eoh3B89+lc=\",\"Nonce\":\"V2bJ4VFRrbH+oUd0W8swjQ==\",\"Verifier\":\"oegZmDzOLFVv8jAYxfnOqBk+0KiasSr6J/8v9xbUIkE=\"}";
    private static String getLoginsAssociate ="{\"RequestType\":\"associate\",\"Key\":\"W4z49b+uO2vLGzdqBzfKSY/ynmfLRcxfBYmk99OIT3k=\",\"Nonce\":\"9N3pLnqsXgyh3H7wGXrMQQ==\",\"Verifier\":\"b1z9Rhd8pYSUEczdPx4wWyQTtB9oQFjNzx5EfOfqdNw=\"}";
    private static String getLogins ="{\"RequestType\":\"get-logins\",\"SortSelection\":\"true\",\"TriggerUnlock\":\"false\",\"Id\":\"1234\",\"Nonce\":\"v/93UGpWJYaglsrWTFsq6A==\",\"Verifier\":\"kltoi/9kt2rrv5SReMDYN7zX1XDeBdCPvU78m0qWqVQ=\",\"Url\":\"RNTCyUHMs3smRf99uQx0j/Y0T2dCDAEmYJyx1Gt9zhs=\",\"SubmitUrl\":\"RNTCyUHMs3smRf99uQx0jxIm9b/18UmkIR70L37lxckUpUNjh8Oxre5P6yc74d93OHG3f3OrYEpsItig+IXOAQ==\"}";
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();


    @Test
    public void testVerifyAssociateRequestResponse() {
        Message.Request r = gson.fromJson(requestString, Message.Request.class);
        Processor processor = new Processor(r.Key);
        assertTrue(processor.getCrypto().verify(r));

        Message.Response response = new Message.Response(r.RequestType,"");
        processor.getCrypto().makeVerifiable(response);

        assertTrue(processor.getCrypto().verify(response));

    }
    @Test
    public void testGetLogins() {
        Message.Request r = gson.fromJson(getLoginsAssociate, Message.Request.class);
        Processor processor = new Processor(r.Key);
        processor.getCrypto().verify(r);

        Message.Request l = gson.fromJson(getLogins, Message.Request.class);
        assertTrue(processor.getCrypto().verify(l));
        String encodedUrl = l.Url;
        PaddedBufferedBlockCipher cipher = processor.getCrypto().getCipher(Crypto.CMode.DECRYPT, Helpers.decodeBase64Content(l.Nonce.getBytes(), false));
        String unencodedUrl = processor.getCrypto().CryptoTransform(encodedUrl, true, false, cipher);
        System.out.println(unencodedUrl);
        assertEquals("https://www.facebook.com", unencodedUrl);

        cipher.reset();
        String encodedSubmitUrl = l.SubmitUrl;
        String unencodedSubmitUrl = processor.getCrypto().CryptoTransform(encodedSubmitUrl,true,false, cipher);
        System.out.println(unencodedSubmitUrl);
        assertEquals("https://www.facebook.com/login.php?login_attempt=1&lwv=110", unencodedSubmitUrl);

        Message.Response response = new Message.Response(l.RequestType, processor.getHash());
        response.Success=true;
        response.Count=1;
        response.Entries.add(new Message.ResponseEntry("a", "b", "c","uuid", new ArrayList<Message.ResponseStringField>()));
        processor.getCrypto().makeVerifiable(response);

        assertTrue(processor.getCrypto().verify(response));
    }


}
