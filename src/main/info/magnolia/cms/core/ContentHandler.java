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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessManager;


/**
 * @version 2.01
 */
public abstract class ContentHandler {

    public static final int SORT_BY_DATE = 1;

    public static final int SORT_BY_NAME = 2;

    public static final int SORT_BY_SEQUENCE = 3;

    public static final int IGNORE_SORT = -1;

    protected AccessManager accessManager;

    /**
     * package private constructor
     */
    ContentHandler() {
    }

    /**
     * <p>
     * bit by bit copy of the current object
     * </p>
     * @return Object cloned object
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void setAccessManager(AccessManager manager) {
        this.accessManager = manager;
    }

    public AccessManager getAccessManager() {
        return this.accessManager;
    }

}
