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
package info.magnolia.cms.beans.runtime;

import java.util.Hashtable;
import java.util.Map;


/**
 * @author Sameer charles
 * @version 2.0
 */
public final class SystemProperty {

    private static Map properties = new Hashtable();

    /**
     * Utility class, don't instantiate.
     */
    private SystemProperty() {
        // unused
    }

    /**
     * @param name
     * @param value
     */
    public static void setProperty(String name, String value) {
        SystemProperty.properties.put(name, value);
    }

    /**
     * @param name
     */
    public static String getProperty(String name) {
        return (String) SystemProperty.properties.get(name);
    }

    /**
     * 
     */
    public static Map getPropertyList() {
        return SystemProperty.properties;
    }
}
