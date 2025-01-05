package org.linguafranca.pwdb.kdbx;

import org.junit.Test;

import java.io.PrintStream;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.Assert.*;
import static org.linguafranca.pwdb.kdbx.Helpers.dateTimeFormatter;
import static org.linguafranca.test.util.TestUtil.getTestPrintStream;

public class HelpersTest {

    static PrintStream printStream = getTestPrintStream();

    public String testDate = "2023-05-09T16:11:29Z";
    public Date testDateAsDate = Date.from(ZonedDateTime.parse(testDate, dateTimeFormatter).toInstant());
    public String v4Encoding = "sWfs2w4AAAA=";

    @Test
    public void toDate() throws ParseException {
        Date date = Helpers.toDate(v4Encoding);
        assertEquals(testDateAsDate.getTime(), date.getTime());
    }

    @Test
    public void toDate2() throws ParseException {
        Date date = Helpers.toDate(testDate);
        assertEquals(testDateAsDate, date);
    }

    @Test
    public void toDate3() throws ParseException {
        ZonedDateTime zdt = ZonedDateTime.parse(testDate, dateTimeFormatter);
        printStream.println(zdt);
        Instant instant = zdt.toInstant();
        printStream.println(instant);
        Date date = Date.from(instant);
        printStream.println(date.clone());
        assertEquals(instant.toEpochMilli(), date.getTime());
        assertEquals(instant.toEpochMilli(), testDateAsDate.getTime());
    }

    @Test
    public void fromDate() throws ParseException {
        Helpers.isV4.set(false);
        assertEquals(testDate, Helpers.fromDate(testDateAsDate));
        Helpers.isV4.set(true);
        assertEquals(v4Encoding, Helpers.fromDate(testDateAsDate));
    }

    @Test
    public void fromDateV3() throws ParseException {
        assertEquals(testDate, Helpers.fromDateV3(testDateAsDate));
    }

    @Test
    public void fromDateV4() throws ParseException {
        String base64 = Helpers.fromDateV4(testDateAsDate);
        printStream.println(Helpers.toDate(base64));
        assertEquals(v4Encoding, base64);
    }
}