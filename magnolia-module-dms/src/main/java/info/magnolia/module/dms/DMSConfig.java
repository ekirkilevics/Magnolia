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

package info.magnolia.module.dms;

/**
 * Some usefull constants. Will get removed.
 * @author philipp
 * @deprecated
 */
public class DMSConfig {

    /**
     * Every dms user must be granted to this role
     */
    public static String LICENSE_ROLE = "dmslicense";

    /**
     * Sets some needed permissions
     */
    public static String ROLE = "dmsuser";

    /**
     * The default dms user
     */
    public static String USER = "dms";

    /**
     * The name of the repository
     */
    public static String REPOSITORY = "dms";

    /**
     * The date format used by the DMS
     */
    private static String dateFormat;

    /**
     * @return Returns the dateFormat.
     */
    public static String getDateFormat() {
        return dateFormat;
    }

    /**
     * @param dateFormat The dateFormat to set.
     */
    public static void setDateFormat(String dateFormat) {
        DMSConfig.dateFormat = dateFormat;
    }

}
