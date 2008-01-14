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
