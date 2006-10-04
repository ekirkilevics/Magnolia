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
package info.magnolia.cms.gui.controlx.list;

/**
 * Provides values for models (list,...)
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public interface ValueProvider {

    /**
     * get value - first check for property in this object - then look for the getter for this name - else search in
     * MetaData
     * @param name
     * @param obj
     */
    public abstract Object getValue(String name, Object obj);

}