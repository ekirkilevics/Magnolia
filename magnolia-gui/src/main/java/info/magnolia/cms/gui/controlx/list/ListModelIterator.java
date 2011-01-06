/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.controlx.list;

import java.util.Iterator;


/**
 * @author Sameer Charles $Id:ListModelIterator.java 2544 2006-04-04 12:47:32Z philipp $
 */
public interface ListModelIterator extends Iterator {

    /**
     * get named value
     * @param name its a key to which value is attached in this record
     */
    public Object getValue(String name);

    /**
     * The current object representing a row. Can be of any type.
     * @return the object.
     */
    public Object getValueObject();

    /**
     * get group name
     * @return name of the group of the current record
     */
    public String getGroupName();

    /**
     * jump to next group
     */
    public Object nextGroup();

    /**
     * checks if there are more records in the current group
     * @return true if not EOF
     */
    public boolean hasNextInGroup();

    /**
     * An id to identify the current row
     */
    public String getId();

}
