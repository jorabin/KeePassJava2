/*
 * Copyright 2015 Jo Rabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.linguafranca.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A filter to accept a stream, interpret as XML, allow transformation
 * as XML then forward as a stream.
 *
 * <p>Although this means that the interpretation of the XML will happen
 * twice, here and in the target application, some such applications
 * do not accept XML streams. e.g. the Simple XML framework.
 *
 * @author jo
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class XmlOutputStreamFilter extends PipedOutputStream {

    protected Future<Boolean> future;
    protected PipedInputStream pipedInputStream;

    public XmlOutputStreamFilter(final OutputStream outputStream, final XmlEventTransformer eventTransformer) throws IOException {

        super();
        pipedInputStream = new PipedInputStream(this);

        Callable<Boolean> output = new Callable<Boolean>() {
            public Boolean call() {
                try {
                    XMLEventReader eventReader = new com.fasterxml.aalto.stax.InputFactoryImpl()
                            .createXMLEventReader(pipedInputStream);
                    XMLEventWriter eventWriter = new com.fasterxml.aalto.stax.OutputFactoryImpl()
                            .createXMLEventWriter(outputStream);

                    XMLEvent event = null;
                    while (eventReader.hasNext()) {
                        event = eventReader.nextEvent();
                        event = eventTransformer.transform(event);
                        eventWriter.add(event);
                        eventWriter.flush();
                    }

                    eventReader.close();
                    eventWriter.flush();
                    eventWriter.close();
                    outputStream.flush();
                    outputStream.close();
                } catch (XMLStreamException | IOException e) {
                    throw new IllegalStateException(e);
                }
                return true;
            }
        };
        future = Executors.newSingleThreadExecutor().submit(output);
    }

    public void cancel(boolean interrupt){
        future.cancel(interrupt);
    }

    public boolean isDone(){
        return future.isDone();
    }

    public boolean await() throws IOException {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}

