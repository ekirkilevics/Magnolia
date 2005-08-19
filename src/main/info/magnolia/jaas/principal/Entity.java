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
package info.magnolia.jaas.principal;

import java.io.Serializable;
import java.security.Principal;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles $Id :$
 */
public class Entity implements Principal, Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String DEFAULT_NAME = "person";

    /**
     * default properties
     */
    public static final String FULL_NAME = "fullName";

    public static final String NAME = "name";

    public static final String EMAIL = "email";

    public static final String LANGUAGE = "language";

    public static final String LOCALE = "locale";

    public static final String ADDRESS_LINE = "address";

    /**
     * properties
     */
    private String name;

    private Map properties;

    public Entity() {
        this.properties = new Hashtable();
    }

    /**
     * Get name given to this principal
     * @return name
     */
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return DEFAULT_NAME;
        }
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }
}
