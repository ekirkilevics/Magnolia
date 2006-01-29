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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessManager;

import org.apache.commons.lang.exception.NestableRuntimeException;


/**
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public abstract class ContentHandler implements Cloneable {

    /**
     * Constants for getChildren method
     */
    public static final int SORT_BY_DATE = 1;

    public static final int SORT_BY_NAME = 2;

    public static final int SORT_BY_SEQUENCE = 3;

    public static final int IGNORE_SORT = -1;

    /**
     * AccessManager instance.
     */
    protected AccessManager accessManager;

    /**
     * package private constructor
     */
    ContentHandler() {
    }

    /**
     * Set access manager for this object
     * @param manager
     */
    public void setAccessManager(AccessManager manager) {
        this.accessManager = manager;
    }

    /**
     * Get access manager if previously set for this object
     * @return AccessManager
     */
    public AccessManager getAccessManager() {
        return this.accessManager;
    }

    /**
     * Bit by bit copy of the current object.
     * @return Object cloned object
     */
    protected Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            // should never happen
            throw new NestableRuntimeException(e);
        }
    }

}
