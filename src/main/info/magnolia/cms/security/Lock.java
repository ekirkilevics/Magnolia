/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.security;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Hashtable;


/**
 * Date: Jun 25, 2004
 * Time: 10:03:05 AM
 *
 *
 * @author Sameer Charles
 * @version 2.0
 */




public class Lock {



    private static String SESSION_LOCK = "magnolia:sessionLock";


    private static Logger log = Logger.getLogger(Lock.class);
    private static boolean isSystemLocked;
    private static Date lockSetDate;
    private static Hashtable lockedHierarchyList = new Hashtable();




    public static void setSessionLock(HttpServletRequest request) {
        log.info("Session lock enabled for user ( "
                +Authenticator.getUserId(request)+" ) on "+(new Date()).toString());
        request.getSession().setAttribute(SESSION_LOCK,(new Date()).toString());
    }



    public static boolean isSessionLocked(HttpServletRequest request) {
        if (request.getSession().getAttribute(Lock.SESSION_LOCK) != null)
            return true;
        return false;
    }



    public static void setHierarchyLock(String path) {
        Lock.lockedHierarchyList.put(path,"");
    }


    public static void resetHierarchyLock(String path) {
        Lock.lockedHierarchyList.remove(path);
    }


    public static boolean isHierarchyLocked(String path) {
        return (Lock.lockedHierarchyList.get(path) != null);
    }



    public static void setSystemLock() {
        if (Lock.isSystemLocked()) {
            log.info("System lock exist, created on "+Lock.lockSetDate.toString());
        } else {
            Lock.isSystemLocked = true;
            Lock.lockSetDate = new Date();
            log.info("New System lock created on "+Lock.lockSetDate.toString()+" )");
        }
    }


    public static void resetSystemLock() {
        if (!Lock.isSystemLocked()) {
            log.info("No Lock found to reset");
        } else {
            log.info("Resetting system lock created on "+Lock.lockSetDate.toString());
            Lock.isSystemLocked = false;
        }
    }


    public static boolean isSystemLocked() {
        return Lock.isSystemLocked;
    }



}
