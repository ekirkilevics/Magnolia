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
package info.magnolia.cms.gui.control.list;

/**
 * @author Sameer Charles
 * $Id$
 */
public interface ListViewIterator {

    /**
     * get named value
     * @param name its a key to which value is attached in this record
     * */
    public Object getValue(String name);

    /**
     * get group name
     * @return name of the group of the current record
     * */
    public String getGroupName();

    /**
     * move next
     * */
    public void next();

    /**
     * jump to next group
     * */
    public void nextGroup();

    /**
     * checks if there is next record
     * @return true if not EOF
     * */
    public boolean hasNext();

    /**
     * checks if there are more records in the current group
     * @return true if not EOF
     * */
    public boolean hasNextInGroup();
    
}
