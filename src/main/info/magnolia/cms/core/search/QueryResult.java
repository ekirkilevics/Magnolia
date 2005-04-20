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
package info.magnolia.cms.core.search;


import java.util.Iterator;

/**
 * Date: Apr 4, 2005
 * Time: 11:14:57 AM
 *
 * @author Sameer Charles
 */

public interface QueryResult {

    /**
     * Gets iterator of resultant NodeData objects
     * */
    public Iterator getNodeDataIterator();

    /**
     * Gets iterator of Content objects
     * */
    public Iterator getContentIterator();


}
