package com.asv.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @since 17.08.2009
 */
public class DateUtils {

    public static Date getDate(Date date, Date time) {
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTimeInMillis(date.getTime());

        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTimeInMillis(time.getTime());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, timeCalendar.get(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    public static int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

}
