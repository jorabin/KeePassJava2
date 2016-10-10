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

package org.linguafranca.pwdb.kdbx;

import org.linguafranca.xml.XmlEventTransformer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class KdbxInputTransformer implements XmlEventTransformer {
    private final StreamEncryptor streamEncryptor;
    private XMLEventFactory xmlEventFactory = new com.fasterxml.aalto.stax.EventFactoryImpl();
    private boolean decryptContent;

    public KdbxInputTransformer (StreamEncryptor streamEncryptor) {
        this.streamEncryptor = streamEncryptor;
    }

    public XMLEvent transform (XMLEvent event) {
        switch (event.getEventType()) {
            case XMLEvent.START_ELEMENT: {
                Attribute attribute = event.asStartElement().getAttributeByName(new QName("Protected"));
                if (attribute != null) {
                    decryptContent = Helpers.toBoolean(attribute.getValue());
                }
                break;
            }
            case XMLEvent.END_ELEMENT: {
                decryptContent = false;
                break;
            }
            case XMLEvent.CHARACTERS: {
                if (decryptContent) {
                    String text = event.asCharacters().getData();
                    text = new String(streamEncryptor.decrypt(Helpers.decodeBase64Content(text.getBytes(), false)));
                    event = xmlEventFactory.createCharacters(text);
                }
                break;
            }
        }
        return event;
    }
}
