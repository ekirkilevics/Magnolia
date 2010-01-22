/**
 * This file Copyright (c) 2010 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 * 
 * Any modifications to this file must keep this entire header
 * intact.
 * 
 */
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.NodeDataUtil;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Implementing some default behavior.
 * @author pbaerfuss
 * @version $Id$
 *
 */
public abstract class AbstractNodeData implements NodeData{

    protected String name;
    protected Content parent;

    protected AbstractNodeData(Content parent, String name) {
        this.parent = parent;
        this.name = name;
    }
    
    public HierarchyManager getHierarchyManager() {
        return parent.getHierarchyManager();
    }

    public String getName() {
        return this.name;
    }

    public String getHandle() {
        return Path.getAbsolutePath(parent.getHandle(), name);
    }

    public boolean isGranted(long permissions) {
        return getHierarchyManager().getAccessManager().isGranted(getHandle(), permissions);
    }
    
    public String getString(String lineBreak) {
        return getString().replaceAll("\n", lineBreak); //$NON-NLS-1$
    }

    public Content getParent() throws AccessDeniedException, ItemNotFoundException, javax.jcr.AccessDeniedException, RepositoryException {
        return this.parent;
    }
    
    public void setParent(Content parent) {
        this.parent = parent;
    }
    
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(getHierarchyManager().getName() + ":");
        buffer.append(getHandle());
        buffer.append("[");
        buffer.append(NodeDataUtil.getTypeName(this));
        buffer.append("]");

        return buffer.toString();
    }
}