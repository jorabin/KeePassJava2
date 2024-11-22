package org.linguafranca.pwdb.example;

import org.junit.Test;

import static org.linguafranca.pwdb.util.PropertyValueUtil.genericTest;

public class SecureCharBufferPropertyValueTest {

    @Test
    public void storageTest(){
        genericTest(new SecureCharBufferPropertyValue.Factory(), false);
    }

}