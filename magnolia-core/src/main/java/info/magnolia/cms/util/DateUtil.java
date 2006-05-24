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

    public String getFormattedDate(Date date) {
        return this.getFormattedDate(date, FORMAT_DEFAULTPATTERN);
    }

    public String getFormattedDate(Date date, String formatPattern) {
        if (formatPattern == null) {
            formatPattern = FORMAT_DEFAULTPATTERN;
        }
        String fd = DateFormatUtils.formatUTC(date, formatPattern);
        return fd;
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
}
