package com.bkrc.bkrcv3.common.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class LocalDateUtils {
    private class Const {
        public static final String YEAR_FORMAT = "yyyy";
        public static final String DATE_FORMAT = "yyyyMMdd";
        public static final String DATETIME_FORMAT = "yyyyMMddHHmmss";
        public static final String DATEHOUR_FORMAT = "yyyyMMddHH";

        public static final String DATEMINUTE_FORMAT = "yyyyMMddHHmm";

        public static final String DATENANO_FORMAT = "yyyyMMddHHmmssSSSS";
        public static final String SHORT_YEAR_DATETIME_FORMAT = "yyMMddHHmmss";

        public static final String DATE_FORMAT_HYPHEN = "yyyy-MM-dd";

        public static final String ITEM_RESTOCK_FORMAT_HYPHEN = "MM/dd";
    }
    private LocalDateUtils() {
    }

    public static DateTimeFormatter getDateTimeFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault());
    }

    public static String getDate(LocalDateTime date) {
        return localDateTimeToString(date, "yyyy-MM-dd");
    }

    public static String getDateTime(LocalDateTime date) {
        return localDateTimeToString(date, "yyyy-MM-dd HH:mm:ss");
    }



    public static String localDateToString(LocalDate date, String pattern) {
        if (date == null) {
            return "";
        }
        return getDateTimeFormatter(pattern).format(date);
    }

    public static String localDateToString(LocalDate date) {
        if (date == null) {
            return "";
        }
        return localDateToString(date, Const.DATE_FORMAT);
    }


    public static String localDateTimeToString(LocalDateTime date, String pattern) {
        if (date == null) {
            return "";
        }
        return getDateTimeFormatter(pattern).format(date);
    }

    public static String localDateTimeToString(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        return getDateTimeFormatter(Const.DATETIME_FORMAT).format(date);
    }


    public static LocalDate getLocalDate(String date, String pattern) {
        DateTimeFormatter formatter = getDateTimeFormatter(pattern);

        return LocalDate.parse(date, formatter);
    }


    public static LocalDate getLocalDate(String date) {
        return getLocalDate(date, Const.DATE_FORMAT);
    }

    public static String processMessage(String message) {
        return "SKC" + message;
    }

    public static LocalDateTime getLocalDateTime(String date, String pattern) {
        DateTimeFormatter formatter = getDateTimeFormatter(pattern);
        return LocalDateTime.parse(date, formatter);
    }

    public static LocalDateTime getLocalDateTime(String date) {
        return getLocalDateTime(date, Const.DATETIME_FORMAT);
    }

    public static String getCustomDate(String yyyymmdd) {
        int year = Integer.parseInt(yyyymmdd.substring(0, 4));
        int month = Integer.parseInt(yyyymmdd.substring(5, 7));
        int date = Integer.parseInt(yyyymmdd.substring(8, 10));

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        return dateFormatter.format(cal.getTime());
    }
}
