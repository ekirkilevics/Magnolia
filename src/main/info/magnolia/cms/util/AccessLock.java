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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 1.1
 * @deprecated as on magnolia 2.0
 */
public final class AccessLock {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(AccessLock.class);

    /**
     * Utility class, don't instantiate.
     */
    private AccessLock() {
        // unused
    }

    /**
     * <p>
     * sets the access lock. No content access method check for this lock accept EntryServlet while delivering the final
     * page or any resource (images/documents....)
     * </p>
     */
    public static void set() {
        /* create a tmp file as a lock flag */
        File lockFile = getLockFile();
        if (!lockFile.exists()) {
            try {
                lockFile.createNewFile();
            }
            catch (IOException e) {
                log.error("Failed to set access lock");
                log.error(e.getMessage());
            }
        }
    }

    /**
     * <p>
     * releases any lock set by the set method (if any)
     * </p>
     */
    public static void release() {
        File lockFile = getLockFile();
        if ((lockFile != null) && lockFile.exists()) {
            lockFile.delete();
        }
    }

    /**
     * <p>
     * Checks is the access lock has been set
     * </p>
     */
    public static boolean isSet() {
        File lockFile = getLockFile();
        if (lockFile.exists()) {
            return true;
        }
        return false;
    }

    /**
     * @return lock file instance
     */
    private static File getLockFile() {
        try {
            return new File(Path.getTempDirectoryPath() + "/.AccessLock");
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            log.fatal("Failed to get AccessLock file");
            log.fatal("Check Java -D options for magnolia tmp directory");
        }
        return null;
    }
}
