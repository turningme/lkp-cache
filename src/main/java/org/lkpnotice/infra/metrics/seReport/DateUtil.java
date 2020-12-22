package org.lkpnotice.infra.metrics.seReport;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


//https://issues.apache.org/jira/issues/?jql=project%20%3D%20LANG%20AND%20issuetype%20%3D%20Bug%20AND%20component%20%3D%20%22lang.time.*%22%20AND%20status%20in%20(Open%2C%20%22In%20Progress%22%2C%20Reopened)%20ORDER%20BY%20key%20DESC
public class DateUtil {

    private static Calendar cal = GregorianCalendar.getInstance();
    public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    private static final ThreadLocal<Calendar> CAL_SAFE = ThreadLocal.withInitial(() -> GregorianCalendar.getInstance());


    public static String yyyyMMddIntToReadable(final int yyyyMMddInt) {
        return yyyyMMddToReadable("" + yyyyMMddInt);
    }


    public static String yyyyMMddToReadable(final String yyyyMMdd) {
        if (yyyyMMdd.length() != 8) {
            throw new IllegalArgumentException("yyyyMMdd date is invalid because of wrong lengh : "
                    + yyyyMMdd.length()
                    + " date : "
                    + yyyyMMdd);
        }
        return yyyyMMdd.substring(0, 4) + "/" + yyyyMMdd.substring(4, 6) + "/" + yyyyMMdd.substring(6, 8);
    }


    public static Calendar longMsToCalendarDay(long ms, final TimeZone tz) {
        Calendar dayShifterCal = Calendar.getInstance();
        dayShifterCal.setTimeZone(tz);
        dayShifterCal.setTimeInMillis(ms);
        dayShifterCal.set(Calendar.HOUR_OF_DAY, 0);
        dayShifterCal.set(Calendar.MINUTE, 0);
        dayShifterCal.set(Calendar.SECOND, 0);
        dayShifterCal.set(Calendar.MILLISECOND, 0);
        return dayShifterCal;
    }


    public static Calendar cloneCalendarFirstHourDay(Calendar dateCal) {
        Calendar cal = cloneCalendar(dateCal);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }


    public static Calendar cloneCalendar(Calendar dateCal) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateCal.getTime());
        cal.setTimeZone(dateCal.getTimeZone());
        return cal;
    }


    public static Calendar cloneCalendarFirstDayMonth(Calendar dateCal) {
        Calendar cal = cloneCalendar(dateCal);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }


    public static int longMsToIntYyyyMMdd(final long ms, final TimeZone tz) {
        //TODO UNit test libsecond+dst +29 fevrier
        return longMsToIntYyyyMMdd(ms, tz, cal);

    }

    public static int longMsToIntYyyyMMddSafe(final long ms, final TimeZone tz) {
        Calendar cal = CAL_SAFE.get();
        return longMsToIntYyyyMMdd(ms, tz, cal);

    }

    public static int longMsToIntYyyyMMdd(final long ms, final TimeZone tz, Calendar cal) {
        //TODO UNit test libsecond+dst +29 fevrier
        cal.setTimeZone(tz);
        cal.setTimeInMillis(ms);
        int yyyyMMdd = cal.get(Calendar.YEAR)
                * 10000
                + (cal.get(Calendar.MONTH) + 1)
                * 100
                + cal.get(Calendar.DAY_OF_MONTH);
        return yyyyMMdd;

    }


    public static int yyyyMMddToInt(final String yyyyMMdd) throws Exception {
        //DONOt use the SimpleDateFormater  because it use a Date class witch is built with the default system time-zone
        try {
            int year = Integer.parseInt(yyyyMMdd.substring(0, 4));
            int month = Integer.parseInt(yyyyMMdd.substring(4, 6));
            if (month < 1 || month > 12) {
                throw new Exception("month part must be  between 1 and 12 : given month was " + month);
            }

            int day = Integer.parseInt(yyyyMMdd.substring(6, 8));
            if (day < 1 || day > 31) {
                throw new Exception("day part must be  between 1 and 31 : given day was " + day);
            }
            return year * 10000 + month * 100 + day;

        } catch (Exception e) {
            throw new Exception("Integer.parseInt("
                    + yyyyMMdd
                    + ") fail"
                    + " yyyMMdd_formater should be initialized with \"yyyyMMdd pattern\" ", e);
        }
    }



    public static long firstDayMonth(final long ms, final TimeZone tz) {
        return firstDayMonth(ms, tz, cal);
    }


    public static long firstDayMonthSafe(final long ms, final TimeZone tz) {
        Calendar c = CAL_SAFE.get();
        return firstDayMonth(ms, tz, c);
    }

    public static long firstDayMonth(final long ms, final TimeZone tz, final Calendar cal) {
        cal.setTimeZone(tz);
        cal.setTimeInMillis(ms);

        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }



    public static long firstHourDay(final long ms, final TimeZone tz, final Calendar cal) {
        cal.setTimeZone(tz);
        cal.setTimeInMillis(ms);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long firstHourDaySafe(final long ms, final TimeZone tz) {
        Calendar c = CAL_SAFE.get();
        return firstHourDay(ms, tz, c);
    }


    public static long firstHourDay(final long ms, final TimeZone tz) {
        return firstHourDay(ms, tz, cal);
    }


    public static long shiftOneDay(final long ms, final TimeZone tz) {
        cal.setTimeZone(tz);
        cal.setTimeInMillis(ms);
        cal.add(Calendar.DATE, 1);
        return cal.getTimeInMillis();
    }


    public static long shiftOnMonth(final long ms, final TimeZone tz) {
        cal.setTimeZone(tz);
        cal.setTimeInMillis(ms);
        cal.add(Calendar.MONTH, 1);
        return cal.getTimeInMillis();
    }


    public static String nanoToString(final long nanos) {
        //no need to use a timeZone define ; it's a duration and duration, like timeStamp hasn't time zone
        TimeUnit t = TimeUnit.NANOSECONDS;
        final long millisecondes = t.toMillis(nanos) % 1000;
        final long seconds = t.toSeconds(nanos) % 60;
        final long minutes = t.toMinutes(nanos) % 60;
        final long hours = t.toHours(nanos) % 24;
        final long days = t.toDays(nanos);
        if (hours > 0 && days == 0) {
            return String.format("%02d:%02d:%02d %03dms", hours, minutes, seconds, millisecondes);
        } else if (days > 0) {
            return String.format("%02dj %02d:%02d:%02d %03dms", days, hours, minutes, seconds, millisecondes);
        } else {
            return String.format("00:%02d:%02d %03dms", minutes, seconds, millisecondes);
        }
    }

}
