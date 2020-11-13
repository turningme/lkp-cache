package org.lkpnotice.infra.datethreadsafe;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by jpliu on 2020/9/12.
 */
public class DateUtil {
    private static Calendar cal = GregorianCalendar.getInstance();
    public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");


    static  ThreadLocal<Calendar> calendarThreadLocal = new ThreadLocal(){
        @Override
        protected Object initialValue() {
            return GregorianCalendar.getInstance();
        }
    };


    public static long firstDayMonth(final long ms, final TimeZone tz) {
        cal.setTimeZone(tz);
        cal.setTimeInMillis(ms);

        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }


    public static synchronized long firstDayMonthSafe(final long ms, final TimeZone tz) {
        cal.setTimeZone(tz);
        cal.setTimeInMillis(ms);

        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }


    public static synchronized long firstDayMonthSafeLocal(final long ms, final TimeZone tz) {
        Calendar cal = calendarThreadLocal.get();

        cal.setTimeZone(tz);
        cal.setTimeInMillis(ms);

        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
