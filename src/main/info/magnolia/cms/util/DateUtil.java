/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.util;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class DateUtil {

    /**
     * Default date format.
     */
    public static final String FORMAT_DEFAULTPATTERN = "yyyy-MM-dd'T'HH:mm:ss.SZ";

    public String getFormattedDate(Date date) {
        return this.getFormattedDate(date, FORMAT_DEFAULTPATTERN);
    }

    public String getFormattedDate(Date date, String formatPattern) {
        if (formatPattern == null) {
            formatPattern = FORMAT_DEFAULTPATTERN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
        String fd = sdf.format(date);
        return fd;
    }
}
