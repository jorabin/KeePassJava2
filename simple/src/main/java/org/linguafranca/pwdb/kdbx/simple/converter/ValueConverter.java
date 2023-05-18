package org.linguafranca.pwdb.kdbx.simple.converter;

import org.apache.commons.codec.binary.Base64;
import org.linguafranca.pwdb.kdbx.simple.model.EntryClasses;
import org.linguafranca.pwdb.security.StreamEncryptor;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Value converter takes care of encryption and decryption of protected fields
 */
public class ValueConverter implements Converter<EntryClasses.StringProperty.Value> {

    private final StreamEncryptor encryption;

    public ValueConverter(StreamEncryptor encryption) {
        this.encryption = encryption;
    }

    /**
     * De-serialise (unmarshal) the input node
     * @param node this is the node to deserialize the object from
     *
     * @return the de-serialised object
     */
    @Override
    public EntryClasses.StringProperty.Value read(InputNode node) throws Exception {
        EntryClasses.StringProperty.Value value = new EntryClasses.StringProperty.Value();
        if (Objects.nonNull(node.getAttribute("Protected")) && node.getAttribute("Protected").getValue().equals("True")) {
            byte[] encrypted = new byte[0];
            // you can only read the value once
            String text = node.getValue();
            if (Objects.nonNull(text)) {
                encrypted = Base64.decodeBase64(text.getBytes());
            }
            String decrypted = new String(encryption.decrypt(encrypted), StandardCharsets.UTF_8);
            value.setText(decrypted);
            value.setProtected(null);
            value.setProtectOnOutput(true);
        } else {
            value.setProtectOnOutput(false);
            value.setText(node.getValue());
        }
        return value;
    }

    /**
     * Serialise (marshal) the object
     * @param node this is the node to serialized the object to
     * @param value this is the value that is to be serialized
     */
    @Override
    public void write(OutputNode node, EntryClasses.StringProperty.Value value) throws Exception {
        if (value.getProtectOnOutput()) {
            byte [] encrypted = encryption.encrypt(value.getText().getBytes(StandardCharsets.UTF_8));
            byte [] base64Encoded = Base64.encodeBase64(encrypted);
            node.setValue(new String(base64Encoded));
            node.getAttributes().put("Protected", "True");
        } else {
            node.setValue(value.getText());
        }
        //node.getAttributes().remove("protectInMemory");
    }
}
