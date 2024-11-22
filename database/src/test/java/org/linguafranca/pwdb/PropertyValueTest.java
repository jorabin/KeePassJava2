package org.linguafranca.pwdb;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.linguafranca.pwdb.util.PropertyValueUtil.genericTest;

public class PropertyValueTest {

    public static final String ANOTHER_SECRET = "password with accents àéç";

    @Test
    public void bytesTest() {
        genericTest(PropertyValue.BytesStore.getFactory());
    }

    @Test
    public void sealedObjectTest() {
        genericTest(PropertyValue.SealedStore.getFactory());
    }

    @Test
    public void stringTest() {
        genericTest(PropertyValue.StringStore.getFactory());
    }

    @Test
    public void sealedScriptTest(){
        PropertyValue.SealedStore sealed = PropertyValue.SealedStore.getFactory().of(ANOTHER_SECRET);
        byte[] bytes = sealed.getValueAsBytes();
        assertArrayEquals(ANOTHER_SECRET.getBytes(StandardCharsets.UTF_8), bytes);
    }
}