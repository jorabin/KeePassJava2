package org.linguafranca.pwdb.keepasshttp;

/**
 * @author jo
 */
public class Http {
    public enum CMode {
        ENCRYPT(true), DECRYPT(false);
        private final boolean encrypt;

        CMode(boolean encrypt) {
            this.encrypt = encrypt;
        }

        public boolean getEncrypt() {
            return encrypt;
        }
    }

/*
    public static class KeePassHttpExt
    {

        /// <summary>
        /// an arbitrarily generated uuid for the keepasshttp root entry
        /// </summary>
        public final byte[] KEEPASSHTTP_UUID = {
                0x34, (byte) 0x69, (byte) 0x7a, (byte) 0x40, (byte) 0x8a, (byte) 0x5b, (byte) 0x41, (byte) 0xc0,
                (byte) 0x9f, (byte) 0x36, (byte) 0x89, (byte) 0x7d, (byte) 0x62, (byte) 0x3e, (byte) 0xcb, (byte) 0x31
        };

        private final int DEFAULT_NOTIFICATION_TIME = 5000;
        public final String KEEPASSHTTP_NAME = "KeePassHttp Settings";
        private final String KEEPASSHTTP_GROUP_NAME = "KeePassHttp Passwords";
        public final String ASSOCIATE_KEY_PREFIX = "AES Key: ";
        private IPluginHost host;
        private HttpListener listener;
        public final int DEFAULT_PORT = 19455;
        public final String DEFAULT_HOST = "localhost";
        /// <summary>
        /// TODO make configurable
        /// </summary>
        private const String HTTP_SCHEME = "http://";
        //private const String HTTPS_PREFIX = "https://localhost:";
        //private int HTTPS_PORT = DEFAULT_PORT + 1;
        private Thread httpThread;
        private volatile boolean stopped = false;
        Dictionary<String, RequestHandler> handlers = new Dictionary<String, RequestHandler>();

        //public String UpdateUrl = "";
        public String UpdateUrl = "https://passifox.appspot.com/kph/latest-version.txt";

    private SearchParameters MakeSearchParameters()
    {
        SearchParameters p = new SearchParameters();
        p.SearchInTitles = true;
        p.RegularExpression = true;
        p.SearchInGroupNames = false;
        p.SearchInNotes = false;
        p.SearchInOther = false;
        p.SearchInPasswords = false;
        p.SearchInTags = false;
        p.SearchInUrls = true;
        p.SearchInUserNames = false;
        p.SearchInUuids = false;
        return p;
    }

    private String CryptoTransform(String input, bool base64in, bool base64out, Aes cipher, CMode mode)
    {
        byte[] bytes;
        if (base64in)
            bytes = decode64(input);
        else
            bytes = Encoding.UTF8.GetBytes(input);


        using (var c = mode == CMode.ENCRYPT ? cipher.CreateEncryptor() : cipher.CreateDecryptor()) {
        var buf = c.TransformFinalBlock(bytes, 0, bytes.Length);
        return base64out ? encode64(buf) : Encoding.UTF8.GetString(buf);
    }
    }

    private PwEntry GetConfigEntry(bool create)
    {
        var root = host.Database.RootGroup;
        var uuid = new PwUuid(KEEPASSHTTP_UUID);
        var entry = root.FindEntry(uuid, false);
        if (entry == null && create)
        {
            entry = new PwEntry(false, true);
            entry.Uuid = uuid;
            entry.Strings.Set(PwDefs.TitleField, new ProtectedString(false, KEEPASSHTTP_NAME));
            root.AddEntry(entry, true);
            UpdateUI(null);
        }
        return entry;
    }

    private int GetNotificationTime()
    {
        var time = DEFAULT_NOTIFICATION_TIME;
        var entry = GetConfigEntry(false);
        if (entry != null)
        {
            var s = entry.Strings.ReadSafe("Prompt Timeout");
            if (s != null && s.Trim() != "")
            {
                try
                {
                    time = Int32.Parse(s) * 1000;
                }
                catch { }
            }
        }

        return time;
    }

    private void ShowNotification(String text)
    {
        ShowNotification(text, null, null);
    }

    private void ShowNotification(String text, EventHandler onclick)
    {
        ShowNotification(text, onclick, null);
    }

    private void ShowNotification(String text, EventHandler onclick, EventHandler onclose)
    {
        MethodInvoker m = delegate
        {
            var notify = host.MainWindow.MainNotifyIcon;
            if (notify == null)
                return;

            EventHandler clicked = null;
            EventHandler closed = null;

            clicked = delegate
            {
                notify.BalloonTipClicked -= clicked;
                notify.BalloonTipClosed -= closed;
                if (onclick != null)
                    onclick(notify, null);
            };
            closed = delegate
            {
                notify.BalloonTipClicked -= clicked;
                notify.BalloonTipClosed -= closed;
                if (onclose != null)
                    onclose(notify, null);
            };

            //notify.BalloonTipIcon = ToolTipIcon.Info;
            notify.BalloonTipTitle = "KeePassHttp";
            notify.BalloonTipText = text;
            notify.ShowBalloonTip(GetNotificationTime());
            // need to add listeners after showing, or closed is sent right away
            notify.BalloonTipClosed += closed;
            notify.BalloonTipClicked += clicked;
        };
        if (host.MainWindow.InvokeRequired)
            host.MainWindow.Invoke(m);
        else
            m.Invoke();
    }

    public override bool Initialize(IPluginHost host)
    {
        var httpSupported = HttpListener.IsSupported;
        this.host = host;

        var optionsMenu = new ToolStripMenuItem("KeePassHttp Options...");
        optionsMenu.Click += OnOptions_Click;
        optionsMenu.Image = KeePassHttp.Properties.Resources.earth_lock;
        //optionsMenu.Image = global::KeePass.Properties.Resources.B16x16_File_Close;
        this.host.MainWindow.ToolsMenu.DropDownItems.Add(optionsMenu);

        if (httpSupported)
        {
            try
            {
                handlers.Add(Request.TEST_ASSOCIATE, TestAssociateHandler);
                handlers.Add(Request.ASSOCIATE, AssociateHandler);
                handlers.Add(Request.GET_LOGINS, GetLoginsHandler);
                handlers.Add(Request.GET_LOGINS_COUNT, GetLoginsCountHandler);
                handlers.Add(Request.GET_ALL_LOGINS, GetAllLoginsHandler);
                handlers.Add(Request.SET_LOGIN, SetLoginHandler);
                handlers.Add(Request.GENERATE_PASSWORD, GeneratePassword);

                listener = new HttpListener();

                var configOpt = new ConfigOpt(this.host.CustomConfig);

                listener.Prefixes.Add(HTTP_SCHEME + configOpt.ListenerHost + ":" + configOpt.ListenerPort.ToString() + "/");
                //listener.Prefixes.Add(HTTPS_PREFIX + HTTPS_PORT + "/");
                listener.Start();

                httpThread = new Thread(new ThreadStart(Run));
                httpThread.Start();
            } catch (HttpListenerException e) {
                MessageBox.Show(host.MainWindow,
                        "Unable to start HttpListener!\nDo you really have only one installation of KeePassHttp in your KeePass-directory?\n\n" + e,
                        "Unable to start HttpListener",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error
                );
            }
        }
        else
        {
            MessageBox.Show(host.MainWindow, "The .NET HttpListener is not supported on your OS",
                    ".NET HttpListener not supported",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
            );
        }
        return httpSupported;
    }

    void OnOptions_Click(object sender, EventArgs e)
    {
        var form = new OptionsForm(new ConfigOpt(host.CustomConfig));
        UIUtil.ShowDialogAndDestroy(form);
    }

    private void Run()
    {
        while (!stopped)
        {
            try
            {
                var r = listener.BeginGetContext(new AsyncCallback(RequestHandler), listener);
                r.AsyncWaitHandle.WaitOne();
                r.AsyncWaitHandle.Close();
            }
            catch (ThreadInterruptedException) { }
            catch (HttpListenerException e) {
                MessageBox.Show(host.MainWindow, "Unable to process request!\n\n" + e,
                        "Unable to process request",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error
                );
            }
        }
    }

    private JsonSerializer NewJsonSerializer()
    {
        var settings = new JsonSerializerSettings();
        settings.DefaultValueHandling = DefaultValueHandling.Ignore;
        settings.NullValueHandling = NullValueHandling.Ignore;

        return JsonSerializer.Create(settings);
    }
    private Response ProcessRequest(Request r, HttpListenerResponse resp)
    {
        String hash = host.Database.RootGroup.Uuid.ToHexString() + host.Database.RecycleBinUuid.ToHexString();
        hash = getSHA1(hash);

        var response = new Response(r.RequestType, hash);

        using (var aes = new AesManaged())
        {
            aes.Mode = CipherMode.CBC;
            aes.Padding = PaddingMode.PKCS7;
            var handler = handlers[r.RequestType];
            if (handler != null)
            {
                try
                {
                    handler(r, response, aes);
                }
                catch (Exception e)
                {
                    ShowNotification("***BUG*** " + e, (s,evt) => MessageBox.Show(host.MainWindow, e + ""));
                    response.Error = e + "";
                    resp.StatusCode = (int)HttpStatusCode.BadRequest;
                }
            }
            else
            {
                response.Error = "Unknown command: " + r.RequestType;
                resp.StatusCode = (int)HttpStatusCode.BadRequest;
            }
        }

        return response;
    }
    private void RequestHandler(IAsyncResult r)
    {
        try {
            _RequestHandler(r);
        } catch (Exception e) {
            MessageBox.Show(host.MainWindow, "RequestHandler failed: " + e);
        }
    }
    private void _RequestHandler(IAsyncResult r)
    {
        if (stopped) return;
        var l    = (HttpListener)r.AsyncState;
        var ctx  = l.EndGetContext(r);
        var req  = ctx.Request;
        var resp = ctx.Response;

        var serializer = NewJsonSerializer();
        Request request = null;

        resp.StatusCode = (int)HttpStatusCode.OK;
        using (var ins = new JsonTextReader(new StreamReader(req.InputStream)))
        {
            try
            {
                request = serializer.Deserialize<Request>(ins);
            }
            catch (JsonSerializationException e)
            {
                var buffer = Encoding.UTF8.GetBytes(e + "");
                resp.StatusCode = (int)HttpStatusCode.BadRequest;
                resp.ContentLength64 = buffer.Length;
                resp.OutputStream.Write(buffer, 0, buffer.Length);
            } // ignore, bad request
        }

        var db = host.Database;

        var configOpt = new ConfigOpt(this.host.CustomConfig);

        if (request != null && (configOpt.UnlockDatabaseRequest || request.TriggerUnlock == "true") && !db.IsOpen)
        {
            host.MainWindow.Invoke((MethodInvoker)delegate
            {
                host.MainWindow.EnsureVisibleForegroundWindow(true, true);
            });

            // UnlockDialog not already opened
            bool bNoDialogOpened = (KeePass.UI.GlobalWindowManager.WindowCount == 0);
            if (!db.IsOpen && bNoDialogOpened)
            {
                host.MainWindow.Invoke((MethodInvoker)delegate
                {
                    host.MainWindow.OpenDatabase(host.MainWindow.DocumentManager.ActiveDocument.LockedIoc, null, false);
                });
            }
        }

        if (request != null && db.IsOpen)
        {
            Response response = null;
            if (request != null)
                response = ProcessRequest(request, resp);

            resp.ContentType = "application/json";
            var writer = new StringWriter();
            if (response != null)
            {
                serializer.Serialize(writer, response);
                var buffer = Encoding.UTF8.GetBytes(writer.ToString());
                resp.ContentLength64 = buffer.Length;
                resp.OutputStream.Write(buffer, 0, buffer.Length);
            }
        }
        else
        {
            resp.StatusCode = (int)HttpStatusCode.ServiceUnavailable;
        }

        var outs = resp.OutputStream;
        outs.Close();
        resp.Close();
    }

    public override void Terminate()
    {
        stopped = true;
        listener.Stop();
        listener.Close();
        httpThread.Interrupt();
    }

    private void UpdateUI(PwGroup group)
    {
        var win = host.MainWindow;
        if (group == null) group = host.Database.RootGroup;
        var f = (MethodInvoker) delegate {
        win.UpdateUI(false, null, true, group, true, null, true);
    };
        if (win.InvokeRequired)
            win.Invoke(f);
        else
            f.Invoke();
    }

    internal String[] GetUserPass(PwEntry entry)
    {
        return GetUserPass(new PwEntryDatabase(entry, host.Database));
    }

    internal String[] GetUserPass(PwEntryDatabase entryDatabase)
    {
        // follow references
        SprContext ctx = new SprContext(entryDatabase.entry, entryDatabase.database,
                SprCompileFlags.All, false, false);
        String user = SprEngine.Compile(
                entryDatabase.entry.Strings.ReadSafe(PwDefs.UserNameField), ctx);
        String pass = SprEngine.Compile(
                entryDatabase.entry.Strings.ReadSafe(PwDefs.PasswordField), ctx);
        var f = (MethodInvoker)delegate
        {
            // apparently, SprEngine.Compile might modify the database
            host.MainWindow.UpdateUI(false, null, false, null, false, null, false);
        };
        if (host.MainWindow.InvokeRequired)
            host.MainWindow.Invoke(f);
        else
            f.Invoke();

        return new String[] { user, pass };
    }

    /// <summary>
    /// Liefert den SHA1 Hash
    /// </summary>
    /// <param name="input">EingabeString</param>
    /// <returns>SHA1 Hash der EingabeStrings</returns>
    private String getSHA1(String input)
    {
        //Umwandlung des EingaString in den SHA1 Hash
        System.Security.Cryptography.SHA1 sha1 = new System.Security.Cryptography.SHA1CryptoServiceProvider();
        byte[] textToHash = Encoding.Default.GetBytes(input);
        byte[] result = sha1.ComputeHash(textToHash);

        //SHA1 Hash in String konvertieren
        System.Text.StringBuilder s = new System.Text.StringBuilder();
        foreach (byte b in result)
        {
            s.Append(b.ToString("x2").ToLower());
        }

        return s.ToString();
    }
}
*/
}
