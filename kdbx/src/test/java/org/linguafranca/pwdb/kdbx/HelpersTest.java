package org.linguafranca.pwdb.kdbx;

import org.junit.Test;

import java.io.PrintStream;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.*;
import static org.linguafranca.pwdb.kdbx.Helpers.inFormat;
import static org.linguafranca.util.TestUtil.getTestPrintStream;

public class HelpersTest {

    static PrintStream printStream = getTestPrintStream();

    public String testDate = "2023-05-09T17:11:29Z";
    public String v4Encoding = "sWfs2w4AAAA=";

    @Test
    public void toDate() throws ParseException {
        Date date = Helpers.toDate(v4Encoding);
        assertEquals(inFormat.parse(testDate), date);
    }

    @Test
    public void fromDate() throws ParseException {
        Helpers.isV4.set(false);
        assertEquals(testDate, Helpers.fromDate(inFormat.parse(testDate)));
        Helpers.isV4.set(true);
        assertEquals(v4Encoding, Helpers.fromDate(inFormat.parse(testDate)));
    }

    @Test
    public void fromDateV3() throws ParseException {
        Date date = inFormat.parse(testDate);
        assertEquals(testDate, Helpers.fromDateV3(date));
    }

    @Test
    public void fromDateV4() throws ParseException {
        Date date = inFormat.parse(testDate);
        String base64 = Helpers.fromDateV4(date);
        printStream.println(Helpers.toDate(base64));
        assertEquals(v4Encoding, base64);
    }
}