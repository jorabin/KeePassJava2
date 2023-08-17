package org.linguafranca.pwdb.kdbx.jackson.converter;

import java.math.BigInteger;

import com.fasterxml.jackson.databind.util.StdConverter;


public class ByteToBase64Converter extends StdConverter<byte[], String>{

    @Override
    public String convert(byte[] value) {
        String binaryStr = new BigInteger(1, value).toString(2);
        return binaryStr;

    }
    
}
