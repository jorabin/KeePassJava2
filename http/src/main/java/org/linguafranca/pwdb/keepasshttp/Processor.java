package org.linguafranca.pwdb.keepasshttp;

import org.linguafranca.pwdb.kdbx.Helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * Contains message processors for
 *
 * @author jo
 */
public class Processor {
    private interface MessageProcessor {
        void process(Message.Request request, Message.Response response);
    }

    private Map<String, MessageProcessor> processors = new HashMap<String, MessageProcessor>();
    private DatabaseAdaptor adaptor = new DatabaseAdaptor.Default();

    public Processor() {
        processors.put(Message.Type.TEST_ASSOCIATE, new TestAssociate());
        processors.put(Message.Type.ASSOCIATE, new Associate());
        processors.put(Message.Type.GET_LOGINS, new GetLogins());
        processors.put(Message.Type.GET_LOGINS_COUNT, new GetLoginsCount());
        processors.put(Message.Type.GET_ALL_LOGINS, new GetAllLogins());
        processors.put(Message.Type.SET_LOGIN, new SetLogin());
        processors.put(Message.Type.GENERATE_PASSWORD, new GeneratePassword());
    }

    public String getHash() {
        return adaptor.getHash();
    }

    public void process(Message.Request request, Message.Response response) {
        processors.get(request.RequestType).process(request, response);
    }

     private class Associate implements MessageProcessor {
        @Override
        public void process(Message.Request request, Message.Response response) {
            response.Id = adaptor.getId();
            response.Success = true;
        }
    }

    private class TestAssociate implements MessageProcessor {
        @Override
        public void process(Message.Request request, Message.Response response) {
            response.Success = false;
            if (request.Id != null) {
                response.Success = request.Id.equals(adaptor.getId());
                response.Id = adaptor.getId();
            }
        }
    }

    private class GetLogins implements MessageProcessor {
        public void process(Message.Request r, Message.Response resp) {

            resp.Entries.add(new Message.ResponseEntry("FB", "FBLOGIN", "FBPASS",
                    Helpers.base64FromUuid(UUID.randomUUID())));
            resp.Entries.add(new Message.ResponseEntry("FB2", "FBLOGIN2", "FBPASS2",
                    Helpers.base64FromUuid(UUID.randomUUID())));
            resp.Id = adaptor.getId();
            resp.Success = true;
            resp.Count = resp.Entries.size();
        }
    }

    private class GeneratePassword implements MessageProcessor {
        public void process(Message.Request r, Message.Response resp) {
        }
    }

    private class GetAllLogins implements MessageProcessor {
        public void process(Message.Request r, Message.Response resp) {
        }
    }

    private class GetLoginsCount implements MessageProcessor {
        public void process(Message.Request r, Message.Response resp) {
        }
    }

    private class SetLogin implements MessageProcessor {
        public void process(Message.Request r, Message.Response resp) {
        }
    }

}
