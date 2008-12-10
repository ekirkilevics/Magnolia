/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class DateUtil {

    public static final String FORMAT_DATE_SHORT = "date short";

    public static final String FORMAT_DATE_MEDIUM = "date";

    public static final String FORMAT_DATE_LONG = "date long";

    public static final String FORMAT_DATETIME_SHORT = "datetime short";

    public static final String FORMAT_DATETIME_MEDIUM = "datetime";

    public static final String FORMAT_DATETIME_LONG = "datetime long";

    public static final String FORMAT_TIME_SHORT = "time short";

    public static final String FORMAT_TIME_MEDIUM = "time";

    public static final String FORMAT_TIME_LONG = "time long";

    /**
     * Default date format.
     */
    public static final String FORMAT_DEFAULTPATTERN = "yyyy-MM-dd'T'HH:mm:ss.SZ"; //$NON-NLS-1$

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static final String YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd' 'HH:mm:ss";

    public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    /**
     * This is a util
     */
    private DateUtil() {
    }

    /**
     * Supports implicit formats like: date, date long, datetime, dateime long, time and time long
     */
    public static String format(Date date, String formatPattern) {
        return format(date, formatPattern, Locale.getDefault());
    }
        
    public static String format(Date date, String formatPattern, Locale locale) {
        if(date == null){
            return StringUtils.EMPTY;
        }

        if (formatPattern == null) {
            formatPattern = FORMAT_DEFAULTPATTERN;
        }
        
        if(FORMAT_DATE_SHORT.equals(formatPattern)){
            return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale).format(date);
        }
        else if (FORMAT_DATE_MEDIUM.equals(formatPattern)){
            return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale).format(date);
        }
        else if (FORMAT_DATE_LONG.equals(formatPattern)){
            return SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG, locale).format(date);
        }
        else if (FORMAT_TIME_SHORT.equals(formatPattern)){
            return SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT,locale).format(date);
        }
        else if (FORMAT_TIME_MEDIUM.equals(formatPattern)){
            return SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM,locale).format(date);
        }
        else if (FORMAT_TIME_LONG.equals(formatPattern)){
            return SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG,locale).format(date);
        }
        else if (FORMAT_DATETIME_SHORT.equals(formatPattern)){
            return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT,SimpleDateFormat.SHORT,locale).format(date);
        }
        else if (FORMAT_DATETIME_MEDIUM.equals(formatPattern)){
            return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM, locale).format(date);
        }
        else if (FORMAT_DATETIME_LONG.equals(formatPattern)){
            return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG, locale).format(date);
        }
        else {
            return DateFormatUtils.format(date, formatPattern, locale);
        }
    }

    /**
     * Uses the current locale (user) to format the date and time
     * @param val Date or Calendar
     * @return the String
     */
    public static String formatDateTime(Object val) {
        if(val == null){
            return StringUtils.EMPTY;
        }
        FastDateFormat format = FastDateFormat.getDateTimeInstance(
            FastDateFormat.SHORT,
            FastDateFormat.SHORT,
            MgnlContext.getLocale());
        return format.format(val);
    }

    /**
     * Uses the current locale (user) to format the date
     * @param val Date or Calendar
     * @return the String
     */
    public static String formatDate(Object val) {
        if(val == null){
            return StringUtils.EMPTY;
        }
        FastDateFormat format = FastDateFormat.getDateInstance(
            FastDateFormat.SHORT,
            MgnlContext.getLocale());
        return format.format(val);
    }

    /**
     * Uses the current locale (user) to parse the date and time
     * @param val Date or Calendar
     * @return the String
     * @throws ParseException
     */
    public static Date parseDateTime(String dateStr) throws ParseException {
        DateFormat format = SimpleDateFormat.getDateTimeInstance(
            FastDateFormat.SHORT,
            FastDateFormat.SHORT,
            MgnlContext.getLocale());
        return (Date)format.parseObject(dateStr);
    }

    /**
     * Uses the current locale (user) to parse the date
     * @param val Date or Calendar
     * @return the String
     * @throws ParseException
     */
    public static Date parseDate(String dateStr) throws ParseException {
        DateFormat format = SimpleDateFormat.getDateInstance(
            FastDateFormat.SHORT,
            MgnlContext.getLocale());
        return format.parse(dateStr);
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
        SimpleDateFormat sdf = (dateString.length() > YYYY_MM_DD.length())
            ? new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS)
            : new SimpleDateFormat(YYYY_MM_DD);
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
