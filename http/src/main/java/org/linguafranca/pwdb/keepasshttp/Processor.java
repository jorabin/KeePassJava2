package org.linguafranca.pwdb.keepasshttp;

import org.linguafranca.pwdb.kdbx.Helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * Contains message handlers for
 *
 * @author jo
 */
public class Processor {

    private Map<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();
    private DatabaseAdaptor adaptor = new DatabaseAdaptor.Default();
    private Crypto crypto = new Crypto();

    public Processor(String base64Key) {
        this(Helpers.decodeBase64Content(base64Key.getBytes(), false));
    }

    public Processor(byte[] binaryKey) {
        this();
        crypto.setKey(binaryKey);
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

    public Crypto getCrypto() {
        return crypto;
    }

    public String getHash() {
        return adaptor.getHash();
    }

    public interface RequestHandler {
        void process(Message.Request request, Message.Response response);
    }

    public RequestHandler getHandler(String requestType) {
        return handlers.get(requestType);
    }


    private class AssociateHandler implements RequestHandler {
        @Override
        public void process(Message.Request request, Message.Response response) {
            Processor.this.crypto.setKey(Helpers.decodeBase64Content(request.Key.getBytes(), false));

            response.Id = adaptor.getId();
            response.Success = true;
        }
    }

    private class TestAssociateHandler implements RequestHandler {
        @Override
        public void process(Message.Request request, Message.Response response) {
            response.Success = false;
            if (request.Id != null) {
                response.Success = request.Id.equals(adaptor.getId());
            }
        }
    }

    private class GetLoginsHandler implements Processor.RequestHandler {
        public void process(Message.Request r, Message.Response resp) {

            resp.Entries.add(new Message.ResponseEntry("FB", "FBLOGIN", "FBPASS",
                    Helpers.base64FromUuid(UUID.randomUUID())));
            resp.Entries.add(new Message.ResponseEntry("FB2", "FBLOGIN2", "FBPASS2",
                    Helpers.base64FromUuid(UUID.randomUUID())));
            resp.Id = adaptor.getId();
            resp.Success = true;
            resp.Count = 0;

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

}
