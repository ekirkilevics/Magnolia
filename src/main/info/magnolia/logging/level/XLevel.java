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
package info.magnolia.logging.level;

import org.apache.log4j.Level;


/**
 * Date: May 13, 2004 Time: 4:13:05 PM
 * @author Sameer charles
 * @version 2.0
 */
/**
 * @see org.apache.log4j.Level
 */
public final class XLevel extends Level {

    /**
     * TRACE
     */
    static public final int TRACE_INT = Level.FATAL_INT + 1;

    private static String TRACE_STR = "TRACE";

    public static final XLevel TRACE = new XLevel(TRACE_INT, TRACE_STR, 7);

    /**
     * EXCHANGE
     */
    static public final int EXCHANGE_INT = TRACE_INT + 1;

    private static String EXCHANGE_STR = "EXCHANGE";

    public static final XLevel EXCHANGE = new XLevel(EXCHANGE_INT, EXCHANGE_STR, 7);

    public XLevel(int level, String strLevel, int syslogEquiv) {
        super(level, strLevel, syslogEquiv);
    }

    public static Level toLevel(String sArg) {
        String stringVal = sArg.toUpperCase();
        if (stringVal.equals(TRACE_STR))
            return (Level) toLevel(sArg, XLevel.TRACE);
        if (stringVal.equals(EXCHANGE_STR))
            return (Level) toLevel(sArg, XLevel.EXCHANGE);
        return (Level) toLevel(sArg, XLevel.TRACE);
    }

    public static Level toLevel(String sArg, Level defaultValue) {
        if (sArg == null)
            return defaultValue;
        String stringVal = sArg.toUpperCase();
        if (stringVal.equals(TRACE_STR))
            return XLevel.TRACE;
        if (stringVal.equals(EXCHANGE_STR))
            return XLevel.EXCHANGE;
        return Level.toLevel(sArg, (Level) defaultValue);
    }

    public static Level toLevel(int i) throws IllegalArgumentException {
        switch (i) {
            case TRACE_INT :
                return XLevel.TRACE;
            case EXCHANGE_INT :
                return XLevel.EXCHANGE;
        }
        return Level.toLevel(i);
    }
}
