package info.magnolia.cms.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * User: enz
 * Date: Sep 6, 2004
 * Time: 8:39:06 AM
 *
 */
public class DateUtil {
	public static final String FORMAT_DEFAULTPATTERN="yyyy-MM-dd'T'HH:mm:ss.SZ";

	public DateUtil() {

	}


	public String getFormattedDate(Date date) {
		return this.getFormattedDate(date,FORMAT_DEFAULTPATTERN);
	}

	public String getFormattedDate(Date date,String formatPattern) {
		if (formatPattern==null) formatPattern=FORMAT_DEFAULTPATTERN;
		SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
		String fd=sdf.format(date);
		return fd;
	}



}
