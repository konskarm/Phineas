package com.example.kostas.phineas.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.Math.round;

/**
 * Created by Kostas on 9/5/2017.
 */

public class FormatUtils {
    public static String DEFAULT_SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String DEFAULT_SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
    public static String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static Locale DEFAULT_LOCALE = new Locale("en", "EN");

    public static String[] splitDateTime(String datetime, String datetimeFormat, String dateFormat,
                                         String timeFormat){
        String[] split= new String[2];
        SimpleDateFormat sdf = new SimpleDateFormat(datetimeFormat, DEFAULT_LOCALE);
        try {
            java.util.Date dt = sdf.parse(datetime);
            SimpleDateFormat sdfDate = new SimpleDateFormat(dateFormat, DEFAULT_LOCALE);
            SimpleDateFormat sdfTime = new SimpleDateFormat(timeFormat, DEFAULT_LOCALE);
            split[0] = sdfDate.format(dt);
            split[1] = sdfTime.format(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return split;
    }

    public static String mergeDateTime(String date, String time, String newDatetimeFormat,
                                       String oldDatetimeFormat){
        String datetime;
        SimpleDateFormat sdfNew = new SimpleDateFormat(newDatetimeFormat, DEFAULT_LOCALE);
        SimpleDateFormat sdfOld = new SimpleDateFormat(oldDatetimeFormat, DEFAULT_LOCALE);
        java.util.Date dt = null;
        try {
            dt = sdfOld.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        datetime = sdfNew.format(dt);
        return datetime;
    }

    public static String switchDateTimeFormat(String datetime, String oldDateTimeFormat,
                                              String newDateTimeFormat){
        SimpleDateFormat sdfOld = new SimpleDateFormat(oldDateTimeFormat, DEFAULT_LOCALE);
        SimpleDateFormat sdfNew = new SimpleDateFormat(newDateTimeFormat, DEFAULT_LOCALE);

        java.util.Date dt = null;
        try {
            dt = sdfOld.parse(datetime);
        } catch (ParseException e){
            e.printStackTrace();
        }
        return sdfNew.format(dt);
    }

    public static String switchTimeFormat(String time, String oldTimeFormat, String newTimeFormat){

        SimpleDateFormat sdfOld = new SimpleDateFormat(oldTimeFormat, DEFAULT_LOCALE);
        SimpleDateFormat sdfNew = new SimpleDateFormat(newTimeFormat, DEFAULT_LOCALE);

        java.util.Date dt = null;
        try {
            dt = sdfOld.parse(time);
        } catch (ParseException e){
            e.printStackTrace();
        }
        return sdfNew.format(dt);
    }

    public static String switchDateFormat(String date, String oldDateFormat, String newDateFormat){

        SimpleDateFormat sdfOld = new SimpleDateFormat(oldDateFormat, DEFAULT_LOCALE);
        SimpleDateFormat sdfNew = new SimpleDateFormat(newDateFormat, DEFAULT_LOCALE);

        java.util.Date dt = null;
        try {
            dt = sdfOld.parse(date);
        } catch (ParseException e){
            e.printStackTrace();
        }
        return sdfNew.format(dt);
    }

    public static String getCurrentTime(String timeFormat){
        SimpleDateFormat sdfTime = new SimpleDateFormat(timeFormat, DEFAULT_LOCALE);
        String time = sdfTime.format(new Date());
        return time;
    }

    public static String getCurrentDate(String dateFormat){
        SimpleDateFormat sdfDate = new SimpleDateFormat(dateFormat, DEFAULT_LOCALE);
        String date = sdfDate.format(new Date());
        return date;
    }

    public static String getCurrentDateTime(String dateFormat, String timeFormat){
        if (dateFormat == null)
            dateFormat = DEFAULT_DATE_FORMAT;
        if (timeFormat == null)
            timeFormat = DEFAULT_TIME_FORMAT;
        String date = getCurrentDate(dateFormat);
        String time = getCurrentTime(timeFormat);
        return mergeDateTime(date, time,
                DEFAULT_SQL_DATETIME_FORMAT,
                dateFormat + " " + timeFormat);
    }



    /**
     * This function returns the value rounded to 2 decimals
     *
     * @param value The original value
     * @return rounded value
     */
    public static double roundTo2Decimals(double value){
        return round(value * 100.0) / 100.0;
    }
}
