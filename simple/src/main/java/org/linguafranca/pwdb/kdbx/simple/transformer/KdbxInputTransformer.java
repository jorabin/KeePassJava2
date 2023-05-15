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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Transform protected elements on input
 *
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class KdbxInputTransformer implements XmlEventTransformer {
    private XMLEventFactory xmlEventFactory = new com.fasterxml.aalto.stax.EventFactoryImpl();
    private final StreamEncryptor streamEncryptor;
    private boolean decryptContent;

    public KdbxInputTransformer (StreamEncryptor streamEncryptor) {
        this.streamEncryptor = streamEncryptor;
    }
    XMLEventFactory eventFactory = com.fasterxml.aalto.stax.EventFactoryImpl.newInstance();

    public XMLEvent transform (XMLEvent event) {
        switch (event.getEventType()) {
            case START_ELEMENT: {
                StartElement startElement = event.asStartElement();
                Iterable<Attribute> attributeIterable = startElement::getAttributes;
                List<Attribute> attributes = StreamSupport
                        .stream(attributeIterable.spliterator(), false)
                        .collect(Collectors.toList());

                // find any element that is marked for protection
                Attribute attribute = attributes
                        .stream()
                        .filter(a -> a.getName().getLocalPart().equalsIgnoreCase("Protected"))
                        .findFirst()
                        .orElse(null);

                // set flag so it gets encrypted and remove attribute, set attribute for output
                if (attribute != null) {
                    if (attribute.getValue().equalsIgnoreCase("true")) {
                        decryptContent = true;
                        attributes.add(eventFactory.createAttribute("kpj2-ProtectOnOutput", "True"));
                    }
                    attributes.remove(attribute);
                    event = eventFactory.createStartElement(
                            event.asStartElement().getName(),
                            attributes.iterator(),
                            null);
                }
                break;
            }
            case END_ELEMENT: {
                decryptContent = false;
                break;
            }
            case CHARACTERS: {
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
