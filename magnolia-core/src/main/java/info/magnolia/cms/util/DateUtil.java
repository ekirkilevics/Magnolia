/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.context.MgnlContext;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class DateUtil {

    /**
     * Default date format.
     */
    public static final String FORMAT_DEFAULTPATTERN = "yyyy-MM-dd'T'HH:mm:ss.SZ"; //$NON-NLS-1$
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
    public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    public String getFormattedDate(Date date) {
        return this.getFormattedDate(date, FORMAT_DEFAULTPATTERN);
    }

    public String getFormattedDate(Date date, String formatPattern) {
        if (formatPattern == null) {
            formatPattern = FORMAT_DEFAULTPATTERN;
        }
        return DateFormatUtils.formatUTC(date, formatPattern);
    }

    /**
     * Uses the current locale (user) to format the date and time
     * @param val Date or Calendar
     * @return the String
     */
    public static String formatDateTime(Object val) {
        FastDateFormat format = FastDateFormat.getDateTimeInstance(
                FastDateFormat.SHORT,
                FastDateFormat.SHORT,
                MgnlContext.getLocale());
        return format.format(val);
    }

    /**
     * Get the equivalent UTC calendar to a local calendar
     */
    public static Calendar getLocalCalendarFromUTC(Calendar utc) {
        Date valueDate = utc.getTime();
        Calendar c = Calendar.getInstance(); // this has the default timezone for the server
        c.setTime(valueDate);
        return c;
    }

    /**
     * Convert a string date from a dialog date to a UTC calendar ready to be stored in the repository
     */
    public static Calendar getUTCCalendarFromDialogString(String dateString) throws ParseException {
        SimpleDateFormat sdf = (dateString.length()>YYYY_MM_DD.length()) ? new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS) : new SimpleDateFormat(YYYY_MM_DD) ;
        return getUTCCalendarFromLocalDate(sdf.parse(dateString));
    }

    /**
     * Convert a local date time to a UTC calendar
     */
    public static Calendar getUTCCalendarFromLocalDate(Date date) {
        Calendar instance = getCurrentUTCCalendar();
        instance.setTimeInMillis(date.getTime());
        return instance;
    }


    /**
     * Get UTC Calendar for current time
     */
    public static Calendar getCurrentUTCCalendar() {
        return Calendar.getInstance(UTC_TIME_ZONE);
    }
}
