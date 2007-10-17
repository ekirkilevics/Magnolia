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
package info.magnolia.jaas.principal;

import info.magnolia.cms.security.auth.Entity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Hashtable;
import java.util.Map;


/**
 * @author Sameer Charles $Id$
 */
public class EntityImpl implements Entity {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String DEFAULT_NAME = "person";

    private Map properties;

    public EntityImpl() {
        this.properties = new Hashtable();
    }

    /**
     * Get name given to this principal
     *
     * @return name
     */
    public String getName() {
        final String name = (String) getProperty(NAME);
        if (StringUtils.isEmpty(name)) {
            return DEFAULT_NAME;
        }
        return name;
    }

    /**
     * @deprecated use addProperty(NAME, name)
     */
    public void setName(String name) {
        addProperty(NAME, name);
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", getName()).toString();
    }

}
