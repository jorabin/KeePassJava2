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
    public void bytesTest() {
        PropertyValue.BytesStore.Factory<PropertyValue.BytesStore> factory = PropertyValue.BytesStore.getFactory();

        // test as CharSequence
        PropertyValue.BytesStore testValue = factory.of(THIS_IS_A_SECRET);
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
        PropertyValue.SealedStore.Factory<PropertyValue.SealedStore> factory = PropertyValue.SealedStore.getFactory();

        PropertyValue.SealedStore sealed = factory.of(THIS_IS_A_SECRET);
        assertEquals(THIS_IS_A_SECRET, sealed.getValue().toString());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(), sealed.getValueAsBytes());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), sealed.getValueAsChars());
        assertEquals(THIS_IS_A_SECRET, sealed.getValueAsString());

        sealed = factory.of(THIS_IS_A_SECRET.getBytes());
        assertEquals(THIS_IS_A_SECRET, sealed.getValue().toString());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(), sealed.getValueAsBytes());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), sealed.getValueAsChars());
        assertEquals(THIS_IS_A_SECRET, sealed.getValueAsString());

        sealed =factory.of(THIS_IS_A_SECRET.toCharArray());
        assertEquals(THIS_IS_A_SECRET, sealed.getValue().toString());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(), sealed.getValueAsBytes());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), sealed.getValueAsChars());
        assertEquals(THIS_IS_A_SECRET, sealed.getValueAsString());
    }
    @Test
    public void stringTest() {
        PropertyValue.Factory<PropertyValue.StringStore> factory = PropertyValue.StringStore.getFactory();

        PropertyValue.StringStore stringStore = factory.of(THIS_IS_A_SECRET);
        assertEquals(THIS_IS_A_SECRET, stringStore.getValue());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(), stringStore.getValueAsBytes());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), stringStore.getValueAsChars());
        assertEquals(THIS_IS_A_SECRET, stringStore.getValueAsString());

        stringStore = factory.of(THIS_IS_A_SECRET.getBytes());
        assertEquals(THIS_IS_A_SECRET, stringStore.getValue());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(), stringStore.getValueAsBytes());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), stringStore.getValueAsChars());
        assertEquals(THIS_IS_A_SECRET, stringStore.getValueAsString());

        stringStore =factory.of(THIS_IS_A_SECRET.toCharArray());
        assertEquals(THIS_IS_A_SECRET, stringStore.getValue());
        assertArrayEquals(THIS_IS_A_SECRET.getBytes(), stringStore.getValueAsBytes());
        assertArrayEquals(THIS_IS_A_SECRET.toCharArray(), stringStore.getValueAsChars());
        assertEquals(THIS_IS_A_SECRET, stringStore.getValueAsString());
    }

    @Test
    public void sealedScriptTest(){
        PropertyValue.SealedStore sealed = PropertyValue.SealedStore.getFactory().of(ANOTHER_SECRET);
        byte[] bytes = sealed.getValueAsBytes();
        assertArrayEquals(ANOTHER_SECRET.getBytes(StandardCharsets.UTF_8), bytes);

    }

    @Test
    public void getCharsTest(){
        PropertyValue.BytesStore cs = new PropertyValue.BytesStore("a test".toCharArray());
        char [] value = cs.getValueAsChars();
        value[0] = 'b';
        // changing the retrieved copy doesn't change the source
        assertArrayEquals("a test".toCharArray(), cs.getValueAsChars());

        byte [] bytes = cs.getValueAsBytes();
        bytes [0] = 0;
        // same for bytes
        assertArrayEquals("a test".getBytes(), cs.getValueAsBytes());
    }

    @Test
    public void putCharsTest(){
        String testValue = "a test";
        char [] chars = testValue.toCharArray();
        PropertyValue.BytesStore cs = new PropertyValue.BytesStore(chars);
        chars[0] = 'b';
        char [] value = cs.getValueAsChars();
        // changing the source doesn't change the copy
        assertArrayEquals(testValue.toCharArray(), cs.getValueAsChars());
    }
}