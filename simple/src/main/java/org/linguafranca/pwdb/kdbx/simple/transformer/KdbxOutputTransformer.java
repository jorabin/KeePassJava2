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

package org.linguafranca.pwdb.kdbx.simple.transformer;

import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.StreamEncryptor;
import org.linguafranca.xml.XmlEventTransformer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import java.util.ArrayList;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Transform protected elements on output
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class KdbxOutputTransformer implements XmlEventTransformer {

    private XMLEventFactory eventFactory = com.fasterxml.aalto.stax.EventFactoryImpl.newInstance();
    private StreamEncryptor encryptor;
    private Boolean encryptContent = false;

    public KdbxOutputTransformer(StreamEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public XMLEvent transform(XMLEvent event) {
        switch (event.getEventType()) {
            case START_ELEMENT: {
                Attribute attribute = event.asStartElement().getAttributeByName(new QName("Protected"));
                if (attribute != null) {
                    encryptContent = Helpers.toBoolean(attribute.getValue());
                    // this is a workaround for Simple XML not calling converter on attributes
                    List<Attribute> attributes = new ArrayList<>();
                    if (attribute.getValue().toLowerCase().equals("true")) {
                        attributes.add(eventFactory.createAttribute("Protected", "True"));
                    }
                    event = eventFactory.createStartElement(
                            event.asStartElement().getName(),
                            attributes.iterator(),
                            null);
                }
                break;
            }
            case CHARACTERS: {
                if (encryptContent) {
                    String unencrypted = event.asCharacters().getData();
                    String encrypted = Helpers.encodeBase64Content(encryptor.encrypt(unencrypted.getBytes()), false);
                    event = eventFactory.createCharacters(encrypted);
                }
                break;
            }
            case END_ELEMENT: {
                encryptContent = false;
                break;
            }
        }
        return event;
    }
}

