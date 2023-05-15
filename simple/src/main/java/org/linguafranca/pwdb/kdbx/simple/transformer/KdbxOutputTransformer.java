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
import org.linguafranca.pwdb.security.StreamEncryptor;
import org.linguafranca.xml.XmlEventTransformer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static javax.xml.stream.XMLStreamConstants.*;

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
                StartElement startElement = event.asStartElement();
                Iterable<Attribute> attributeIterable = startElement::getAttributes;
                // filter out the annoying "class" attribute that simple adds to "History" element
                // also filter out Protected.
                List<Attribute> attributes = StreamSupport
                        .stream(attributeIterable.spliterator(), false)
                        .filter(a -> {
                            String s = a.getName().getLocalPart();
                            return(!s.equalsIgnoreCase("class") && !s.equalsIgnoreCase("Protected"));
                        })
                        .collect(Collectors.toList());
                // find any element that is marked for protection
                Attribute attribute = attributes
                        .stream()
                        .filter(a -> a.getName().getLocalPart().equalsIgnoreCase("kpj2-protectOnOutput"))
                        .findFirst()
                        .orElse(null);
                // protect it
                if (attribute != null) {
                    if (attribute.getValue().equalsIgnoreCase("true")) {
                        encryptContent = true;
                        attributes.add(eventFactory.createAttribute("Protected", "True"));
                    }
                    attributes.remove(attribute);
                }
                event = eventFactory.createStartElement(
                        event.asStartElement().getName(),
                        attributes.iterator(),
                        null);

                break;
            }
            case CHARACTERS: {
                if (encryptContent) {
                    String unencrypted = event.asCharacters().getData();
                    String encrypted = Helpers.encodeBase64Content(encryptor.encrypt(unencrypted.getBytes()), false);
                    event = eventFactory.createCharacters(encrypted);
                } else {
                    // we want tabs not spaces for indentation
                    if (event.asCharacters().getData().startsWith("\n")) {
                        String output = event.asCharacters().getData().replaceAll("   ", "\t");
                        event = eventFactory.createCharacters(output);
                    }
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

