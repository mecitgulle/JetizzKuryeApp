package com.arasholding.jetizzkuryeapp.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DataFormatter {

    public static class DateFormats {
        public static final String ddMMyyyy = "dd.MM.yyyy";
    }

    public static String GetDateFormat(DateTime dt, String pattern) {

        DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);

        return fmt.print(dt).toString();
    }

    public static DateTime GetDateTime(String strDate) {
        return DateTime.parse(strDate);
    }
}
