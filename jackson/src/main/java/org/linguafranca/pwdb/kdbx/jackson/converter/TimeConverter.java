package org.linguafranca.pwdb.kdbx.jackson.converter;

import java.util.Date;

import org.linguafranca.pwdb.kdbx.Helpers;

import com.fasterxml.jackson.databind.util.StdConverter;

public class TimeConverter extends StdConverter<String, Date>{

    @Override
    public Date convert(String value) {
        if (value.equals("${creationDate}")) {
            return new Date();
        }
        return Helpers.toDate(value);
    }
}
