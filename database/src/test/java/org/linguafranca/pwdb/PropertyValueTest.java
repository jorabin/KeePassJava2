package org.linguafranca.pwdb;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class PropertyValueTest {

    public static final String THIS_IS_A_SECRET = "This is a secret + לַחַיִּים";
    public static final String ANOTHER_SECRET = "password with accents àéç";
    Logger logger = LoggerFactory.getLogger(PropertyValueTest.class);

    @Test
    public void charsTest() {
        PropertyValue.CharsStore.Factory<PropertyValue.CharsStore> factory = PropertyValue.CharsStore.getFactory();

        // test as CharSequence
        PropertyValue.CharsStore testValue = factory.of(THIS_IS_A_SECRET);
        assertEquals(THIS_IS_A_SECRET, testValue.getValue().toString());
        assertEquals(THIS_IS_A_SECRET, testValue.getValueAsString());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), testValue.getValueAsChars());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(StandardCharsets.UTF_8), testValue.getValueAsBytes());
        // test as char[]
        testValue = factory.of(THIS_IS_A_SECRET.toCharArray());
        assertEquals(THIS_IS_A_SECRET, testValue.getValue().toString());
        assertEquals(THIS_IS_A_SECRET, testValue.getValueAsString());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), testValue.getValueAsChars());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(StandardCharsets.UTF_8), testValue.getValueAsBytes());
        // test as byte[]
        testValue = factory.of(THIS_IS_A_SECRET.getBytes());
        assertEquals(THIS_IS_A_SECRET, testValue.getValue().toString());
        assertEquals(THIS_IS_A_SECRET, testValue.getValueAsString());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), testValue.getValueAsChars());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(StandardCharsets.UTF_8), testValue.getValueAsBytes());
    }

    @Test
    public void sealedObjectTest() {
        PropertyValue.CharsStore.Factory<PropertyValue.CharsStore> factory = PropertyValue.CharsStore.getFactory();

        PropertyValue.CharsStore testValue = factory.of(THIS_IS_A_SECRET);
        PropertyValue.SealedStore sealed = new PropertyValue.SealedStore(testValue);
        assertEquals(testValue.getValue(), sealed.getValue());
        assertArrayEquals(testValue.getValueAsBytes(), sealed.getValueAsBytes());
        assertArrayEquals(testValue.getValueAsChars(), sealed.getValueAsChars());
        assertEquals(testValue.getValueAsString(), sealed.getValueAsString());

        testValue = factory.of(THIS_IS_A_SECRET.getBytes());
        sealed = new PropertyValue.SealedStore(testValue);
        assertArrayEquals(testValue.getValueAsBytes(), sealed.getValueAsBytes());
        assertArrayEquals(testValue.getValueAsChars(), sealed.getValueAsChars());
        assertEquals(testValue.getValueAsString(), sealed.getValueAsString());

        testValue = factory.of(THIS_IS_A_SECRET.toCharArray());
        sealed = new PropertyValue.SealedStore(testValue);
        assertArrayEquals(testValue.getValueAsBytes(), sealed.getValueAsBytes());
        assertArrayEquals(testValue.getValueAsChars(), sealed.getValueAsChars());
        assertEquals(testValue.getValueAsString(), sealed.getValueAsString());
    }
    @Test
    public void stringTest() {
        PropertyValue.Factory<PropertyValue.StringStore> factory = PropertyValue.StringStore.getFactory();

        PropertyValue testValue = factory.of(THIS_IS_A_SECRET);
        assertEquals(THIS_IS_A_SECRET, testValue.getValueAsString());
        testValue = factory.of(THIS_IS_A_SECRET.toCharArray());
        assertEquals(THIS_IS_A_SECRET, testValue.getValueAsString());
        testValue = factory.of(THIS_IS_A_SECRET.getBytes(StandardCharsets.UTF_8));
        assertEquals(THIS_IS_A_SECRET, testValue.getValueAsString());
    }

    @Test
    public void sealedScriptTest(){
        PropertyValue.SealedStore sealed = PropertyValue.SealedStore.getFactory().of(ANOTHER_SECRET);
        PropertyValue.CharsStore charStore = sealed.getAsCharsStore();
        byte[] bytes1  = charStore.getValueAsBytes();
        byte[] bytes = sealed.getValueAsBytes();
        byte[] answer = ANOTHER_SECRET.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(ANOTHER_SECRET.getBytes(StandardCharsets.UTF_8), bytes);

    }
}