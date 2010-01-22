/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.core;

import javax.jcr.RepositoryException;

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.util.DeprecationUtil;

/**
 * @author Sameer Charles
 * @version $Revision $ ($Author $) $Id$
 */
public abstract class ContentHandler implements Cloneable {

    /**
     * HierarchyManager instance.
     */
    protected HierarchyManager hierarchyManager;

    /**
     * package private constructor
     */
    ContentHandler() {
    }

    /**
     * Set access manager for this object
     * @param manager
     * @deprecated use setHierarchyManager instead
     */
    public void setAccessManager(AccessManager manager) {
        DeprecationUtil.isDeprecated("The AccessManager is defined by the HierarchyManager");
    }

    /**
     * Get access manager if previously set for this object
     * @return AccessManager
     * @deprecated since 4.0 - use getHierarchyManager instead
     */
    public AccessManager getAccessManager() {
        HierarchyManager hm = getHierarchyManager();
        if(hm != null){
            return hm.getAccessManager();
        }
        return null;
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
            throw new RuntimeException(e);
        }
    }

    /**
     * Get hierarchy manager if previously set for this object
     * @return HierarchyManager
     * @throws RepositoryException
     */
    public HierarchyManager getHierarchyManager() {
        return hierarchyManager;
    }

    /**
     * Set hierarchy manager
     * @param hierarchyManager
     */
    public void setHierarchyManager(HierarchyManager hierarchyManager) {
        this.hierarchyManager = hierarchyManager;
    }

}
