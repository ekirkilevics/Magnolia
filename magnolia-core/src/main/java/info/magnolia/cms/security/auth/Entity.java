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
package info.magnolia.cms.security.auth;

import java.io.Serializable;
import java.security.Principal;


/**
 * @author Sameer Charles $Id$
 */
public interface Entity extends Principal, Serializable {

    /**
     * default properties
     */
    public static final String FULL_NAME = "fullName";

    public static final String NAME = "name";

    public static final String EMAIL = "email";

    public static final String LANGUAGE = "language";

    public static final String LOCALE = "locale";

    public static final String ADDRESS_LINE = "address";

    public static final String PASSWORD = "password";

    /**
     * @return the name of the entity, or a default value if no name was set.
     */
    public String getName();

    /**
     * @deprecated not used - use addProperty(Entity.NAME)
     */
    public void setName(String name);

    public void addProperty(String key, Object value);

    public Object getProperty(String key);
}
