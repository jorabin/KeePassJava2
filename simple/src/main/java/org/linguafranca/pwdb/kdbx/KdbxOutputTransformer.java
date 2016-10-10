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

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @author jo
 */
@SuppressWarnings("WeakerAccess")
public class KdbxOutputTransformer implements XmlEventTransformer {

    private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private Boolean encryptContent = false;
    private StreamEncryptor encryptor;

    public KdbxOutputTransformer(StreamEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public XMLEvent transform(XMLEvent event) {
        if (event.getEventType() == START_ELEMENT) {
            Attribute attribute = event.asStartElement().getAttributeByName(new QName("Protected"));
            if (attribute == null) {
                return event;
            }
            encryptContent = Helpers.toBoolean(attribute.getValue());
        }
        if (event.getEventType() == XMLEvent.CHARACTERS) {
            if (encryptContent) {
                String unencrypted = event.asCharacters().getData();
                String encrypted = Helpers.encodeBase64Content(encryptor.encrypt(unencrypted.getBytes()), false);
                event = eventFactory.createCharacters(encrypted);
            }
        }
        if (event.getEventType() == XMLEvent.END_ELEMENT) {
            encryptContent = false;
        }
        return event;
    }
}

