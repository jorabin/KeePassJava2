package org.linguafranca.pwdb.keepasshttp;

import org.apache.commons.codec.binary.Hex;
import org.linguafranca.pwdb.kdbx.Helpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jo
 */
public class Processor {

    private byte[] binaryKey;
    private String id;
    private Map<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private String makeId() {
        return id = "Today's ID is " + dateFormat.format(new Date());
    }

    private UUID rootGroupUuid = UUID.randomUUID();
    private UUID recycleBinUuid = UUID.randomUUID();

    public String getHash() {
        byte[] toHash = (Helpers.hexStringFromUuid(rootGroupUuid) + Helpers.hexStringFromUuid(recycleBinUuid)).getBytes();
        SHA1Digest digest = new SHA1Digest();
        byte[] digestBytes = new byte[digest.getDigestSize()];
        digest.update(toHash, 0, toHash.length);
        digest.doFinal(digestBytes, 0);
        String result = new String(Hex.encodeHex(digestBytes));
        return result.toLowerCase();
    }

    public interface RequestHandler {
        void process(Message.Request request, Message.Response response);
    }

    public RequestHandler getHandler(String requestType) {
        return handlers.get(requestType);
    }

    public Processor(byte[] binaryKey) {
        this();
        this.binaryKey = binaryKey;
    }

    public Processor(String base64Key) {
        this(Helpers.decodeBase64Content(base64Key.getBytes()));
    }

    public Processor() {
        handlers.put(Message.Type.TEST_ASSOCIATE, new TestAssociateHandler());
        handlers.put(Message.Type.ASSOCIATE, new AssociateHandler());
        handlers.put(Message.Type.GET_LOGINS, new GetLoginsHandler());
/*
        handlers.put(Message.Request.GET_LOGINS_COUNT, GetLoginsCountHandler);
        handlers.put(Message.Request.GET_ALL_LOGINS, GetAllLoginsHandler);
        handlers.put(Message.Request.SET_LOGIN, SetLoginHandler);
        handlers.put(Message.Request.GENERATE_PASSWORD, GeneratePassword);
*/
    }

    public PaddedBufferedBlockCipher getCipher(Http.CMode mode, byte[] iv) {
        PaddedBufferedBlockCipher result = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
        result.init(mode.getEncrypt(), new ParametersWithIV(new KeyParameter(getKey()), iv));
        return result;
    }

    public boolean verify(Message.Verifiable verifiable) {
        byte[] verifier = Helpers.decodeBase64Content(verifiable.Verifier.getBytes(), false);
        byte[] iv = Helpers.decodeBase64Content(verifiable.Nonce.getBytes(), false);

        PaddedBufferedBlockCipher cipher = getCipher(Http.CMode.DECRYPT, iv);

        byte[] output = new byte[cipher.getOutputSize(verifier.length)];
        int outputlen = cipher.processBytes(verifier, 0, verifier.length, output, 0);

        try {
            cipher.doFinal(output, outputlen);
            byte[] comparison = new byte[output.length];
            System.arraycopy(verifiable.Nonce.getBytes(),0,comparison,0,verifiable.Nonce.length());
            return Arrays.equals(output, comparison);
        } catch (InvalidCipherTextException e) {
            return false;
        }
    }

    public void makeVerifiable(Message.Response response) {
        byte[] iv = new SecureRandom().generateSeed(16);
        response.Nonce = Helpers.encodeBase64Content(iv, false);
        PaddedBufferedBlockCipher cipher = getCipher(Http.CMode.ENCRYPT, iv);
        response.Verifier = CryptoTransform(response.Nonce, false, true, cipher);

        if (response.RequestType.equals(Message.Type.GET_LOGINS)) {
            for (Message.ResponseEntry entry: response.Entries) {
                entry.Login = encryptToBase64(entry.Login, response);
                entry.Uuid = encryptToBase64(entry.Uuid, response);
                entry.Name = encryptToBase64(entry.Name, response);
                entry.Password = encryptToBase64(entry.Password, response);
            }
        }
    }

    public String decryptFromBase64(String input, Message.Verifiable verifiable){
        return CryptoTransform(input, true, false, getCipher(Http.CMode.DECRYPT, Helpers.decodeBase64Content(verifiable.Nonce.getBytes())));
    }

    public String encryptToBase64(String input, Message.Verifiable verifiable){
        return CryptoTransform(input, false, true, getCipher(Http.CMode.ENCRYPT, Helpers.decodeBase64Content(verifiable.Nonce.getBytes())));
    }

    public String CryptoTransform(String input, boolean base64in, boolean base64out, PaddedBufferedBlockCipher cipher) {
        byte[] bytes;
        if (base64in) {
            bytes = Helpers.decodeBase64Content(input.getBytes(), false);
        } else {
            bytes = input.getBytes();
        }

        byte[] output = new byte[cipher.getOutputSize(bytes.length)];
        int outputlen = cipher.processBytes(bytes, 0, bytes.length, output, 0);
        try {
            int len = cipher.doFinal(output, outputlen);
            // padded buffer is required on bas64 i.e. encrypted direction
            if (base64out) {
                return Helpers.encodeBase64Content(output, false);
            }
            // trim to buffer length
            return new String(output, 0, outputlen + len);
        } catch (InvalidCipherTextException e) {
            throw new IllegalStateException(e);
        }
    }

    private class AssociateHandler implements RequestHandler {
        @Override
        public void process(Message.Request request, Message.Response response) {
            Processor.this.binaryKey = Helpers.decodeBase64Content(request.Key.getBytes());
            if (!verify(request)) {
                logger.warn("Verification failed for " + request.RequestType);
                return;
            }

            response.Id = makeId();
            response.Success = true;
            makeVerifiable(response);
        }
    }

    private class TestAssociateHandler implements RequestHandler {
        @Override
        public void process(Message.Request request, Message.Response response) {
            if (request.Verifier == null || request.Nonce == null) {
                return;
            }
            if (!verify(request)) {
                logger.warn("Verification failed for " + request.RequestType);
                return;
            }
            response.Success = false;
            if (request.Id != null) {
                response.Success = request.Id.equals(id);
            }
            makeVerifiable(response);
        }
    }

    private class GetLoginsHandler implements Processor.RequestHandler {
        public void process(Message.Request r, Message.Response resp) {
            if (!verify(r)) {
                logger.warn("Verification failed for " + r.RequestType);
                return;
            }

            resp.Entries.add(new Message.ResponseEntry("FB", "FBLOGIN", "FBPASS",
                    Helpers.base64FromUuid(UUID.randomUUID())));
            resp.Entries.add(new Message.ResponseEntry("FB2", "FBLOGIN2", "FBPASS2",
                    Helpers.base64FromUuid(UUID.randomUUID())));
            resp.Id = id;
            resp.Success = true;
            resp.Count = 0;
            makeVerifiable(resp);

/*
        string submithost = null;
        var host = GetHost(CryptoTransform(r.Url, true, false, aes, CMode.DECRYPT));
        if (r.SubmitUrl != null)
            submithost = GetHost(CryptoTransform(r.SubmitUrl, true, false, aes, CMode.DECRYPT));

        var items = FindMatchingEntries(r, aes);
        if (items.ToList().Count > 0) {
            Func<PwEntry, bool> filter = delegate(PwEntry e)
            {
                var c = GetEntryConfig(e);

                var title = e.Strings.ReadSafe(PwDefs.TitleField);
                var entryUrl = e.Strings.ReadSafe(PwDefs.UrlField);
                if (c != null) {
                    return title != host && entryUrl != host && !c.Allow.Contains(host) || (submithost != null && !c.Allow.Contains(submithost) && submithost != title && submithost != entryUrl);
                }
                return title != host && entryUrl != host || (submithost != null && title != submithost && entryUrl != submithost);
            }
            ;

            var configOpt = new ConfigOpt(this.host.CustomConfig);
            var config = GetConfigEntry(true);
            var autoAllowS = config.Strings.ReadSafe("Auto Allow");
            var autoAllow = autoAllowS != null && autoAllowS.Trim() != "";
            autoAllow = autoAllow || configOpt.AlwaysAllowAccess;
            var needPrompting = from e in items where filter(e.entry) select e;

            if (needPrompting.ToList().Count > 0 && !autoAllow) {
                var win = this.host.MainWindow;

                using(var f = new AccessControlForm())
                {
                    win.Invoke((MethodInvoker) delegate
                    {
                        f.Icon = win.Icon;
                        f.Plugin = this;
                        f.Entries = (from e in items where filter(e.entry) select e.entry).ToList();
                        //f.Entries = needPrompting.ToList();
                        f.Host = submithost != null ? submithost : host;
                        f.Load += delegate {
                        f.Activate();
                    }
                        ;
                        f.ShowDialog(win);
                        if (f.Remember && (f.Allowed || f.Denied)) {
                            foreach(var e in needPrompting)
                            {
                                var c = GetEntryConfig(e.entry);
                                if (c == null)
                                    c = new KeePassHttpEntryConfig();
                                var set = f.Allowed ? c.Allow : c.Deny;
                                set.Add(host);
                                if (submithost != null && submithost != host)
                                    set.Add(submithost);
                                SetEntryConfig(e.entry, c);

                            }
                        }
                        if (!f.Allowed) {
                            items = items.Except(needPrompting);
                        }
                    });
                }
            }

            string compareToUrl = null;
            if (r.SubmitUrl != null) {
                compareToUrl = CryptoTransform(r.SubmitUrl, true, false, aes, CMode.DECRYPT);
            }
            if (String.IsNullOrEmpty(compareToUrl))
                compareToUrl = CryptoTransform(r.Url, true, false, aes, CMode.DECRYPT);

            compareToUrl = compareToUrl.ToLower();

            foreach(var entryDatabase in items)
            {
                string entryUrl = String.Copy(entryDatabase.entry.Strings.ReadSafe(PwDefs.UrlField));
                if (String.IsNullOrEmpty(entryUrl))
                    entryUrl = entryDatabase.entry.Strings.ReadSafe(PwDefs.TitleField);

                entryUrl = entryUrl.ToLower();

                entryDatabase.entry.UsageCount = (ulong) LevenshteinDistance(compareToUrl, entryUrl);

            }

            var itemsList = items.ToList();

            if (configOpt.SpecificMatchingOnly) {
                itemsList = (from e in itemsList
                orderby e.entry.UsageCount ascending
                select e).ToList();

                ulong lowestDistance = itemsList.Count > 0 ?
                        itemsList[0].entry.UsageCount :
                        0;

                itemsList = (from e in itemsList
                where e.entry.UsageCount == lowestDistance
                orderby e.entry.UsageCount
                select e).ToList();

            }

            if (configOpt.SortResultByUsername) {
                var items2 = from e in itemsList orderby e.entry.UsageCount ascending, GetUserPass (e)[0]
                ascending select e;
                itemsList = items2.ToList();
            } else {
                var items2 = from e in itemsList orderby e.entry.UsageCount ascending, e.
                entry.Strings.ReadSafe(PwDefs.TitleField) ascending select e;
                itemsList = items2.ToList();
            }

            foreach(var entryDatabase in itemsList)
            {
                var e = PrepareElementForResponseEntries(configOpt, entryDatabase);
                resp.Entries.Add(e);
            }

            if (itemsList.Count > 0) {
                var names = (from e in resp.Entries select e.Name).Distinct<string> ();
                var n = String.Join("\n    ", names.ToArray < string > ());

                if (configOpt.ReceiveCredentialNotification)
                    ShowNotification(String.Format("{0}: {1} is receiving credentials for:\n    {2}", r.Id, host, n));
            }

            resp.Success = true;
            resp.Id = r.Id;
            SetResponseVerifier(resp, aes);

            foreach(var entry in resp.Entries)
            {
                entry.Name = CryptoTransform(entry.Name, false, true, aes, CMode.ENCRYPT);
                entry.Login = CryptoTransform(entry.Login, false, true, aes, CMode.ENCRYPT);
                entry.Uuid = CryptoTransform(entry.Uuid, false, true, aes, CMode.ENCRYPT);
                entry.Password = CryptoTransform(entry.Password, false, true, aes, CMode.ENCRYPT);

                if (entry.StringFields != null) {
                    foreach(var sf in entry.StringFields)
                    {
                        sf.Key = CryptoTransform(sf.Key, false, true, aes, CMode.ENCRYPT);
                        sf.Value = CryptoTransform(sf.Value, false, true, aes, CMode.ENCRYPT);
                    }
                }
            }

            resp.Count = resp.Entries.Count;
        } else {
            resp.Success = true;
            resp.Id = r.Id;
            SetResponseVerifier(resp, aes);
        }
*/
        }
    }

    private byte[] getKey() {
        return binaryKey;
    }
}
