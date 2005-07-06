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
package info.magnolia.exchange;

/**
 * Date: May 4, 2004 Time: 11:15:13 AM
 * @author Sameer Charles
 */
public final class PacketType {

    /**
     * Supported packet data types
     */
    public static final int STRING = 1;

    public static final int BINARY = 2;

    public static final int LONG = 3;

    public static final int DOUBLE = 4;

    public static final int DATE = 5;

    public static final int BOOLEAN = 6;

    public static final int OBJECT = 7;

    /**
     * String representations for packet data types
     */
    public static final String TYPENAME_STRING = "String"; //$NON-NLS-1$

    public static final String TYPENAME_BINARY = "Binary"; //$NON-NLS-1$

    public static final String TYPENAME_LONG = "Long"; //$NON-NLS-1$

    public static final String TYPENAME_DOUBLE = "Double"; //$NON-NLS-1$

    public static final String TYPENAME_DATE = "Date"; //$NON-NLS-1$

    public static final String TYPENAME_BOOLEAN = "Boolean"; //$NON-NLS-1$

    public static final String TYPENAME_OBJECT = "Object"; //$NON-NLS-1$

    /**
     * Utility class, don't instantiate.
     */
    private PacketType() {
        // unused
    }

    /**
     * @param type
     * @return String representation of the type specified
     */
    public static String getNameByType(int type) {
        switch (type) {
            case STRING:
                return TYPENAME_STRING;
            case BINARY:
                return TYPENAME_BINARY;
            case BOOLEAN:
                return TYPENAME_BOOLEAN;
            case LONG:
                return TYPENAME_LONG;
            case DOUBLE:
                return TYPENAME_DOUBLE;
            case DATE:
                return TYPENAME_DATE;
            case OBJECT:
                return TYPENAME_OBJECT;
            default:
                throw new IllegalArgumentException("unknown type: " + type); //$NON-NLS-1$
        }
    }
}
