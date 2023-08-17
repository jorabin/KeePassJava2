package org.linguafranca.pwdb.kdbx.jackson.converter;

import java.util.Base64;

import com.fasterxml.jackson.databind.util.StdConverter;

public class Base64ToByteConverter extends StdConverter<String, byte[]> {

    @Override
    public byte[] convert(String value) {
        byte[] decode = Base64.getDecoder().decode(value);
        return decode;
    }




    
}
