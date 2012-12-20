/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

import info.magnolia.jcr.util.NodeTypes;
import org.apache.jackrabbit.JcrConstants;

import java.io.Serializable;

/**
 * The Magnolia equivalent to {@link javax.jcr.nodetype.NodeType}.
 *
 * @deprecated since 4.5 - directly use {@link info.magnolia.jcr.util.NodeTypes} or {@link org.apache.jackrabbit.JcrConstants}.
 */
public final class ItemType implements Serializable {

    /**
     * Node type: base.
     */
    public static final String NT_BASE = JcrConstants.NT_BASE;

    /**
     * Node type: unstructured.
     */
    public static final String NT_UNSTRUCTURED = JcrConstants.NT_UNSTRUCTURED;

    /**
     * Node type: hierarchyNode.
     */
    public static final String NT_HIERARCHY = JcrConstants.NT_HIERARCHYNODE;

    /**
     * Node type: folder.
     */
    public static final String NT_FOLDER = NodeTypes.Folder.NAME;

    /**
     * Node type: base.
     */
    public static final String NT_FILE = JcrConstants.NT_FILE;

    /**
     * Node type: resource.
     */
    public static final String NT_RESOURCE = NodeTypes.Resource.NAME;

    /**
     * Node type: metadata.
     */
    public static final String NT_METADATA = NodeTypes.MetaData.NAME;

    /**
     * "wfe:workItem".
     */
    public static final ItemType WORKITEM = new ItemType("workItem");

    /**
     * "wfe:expression".
     */
    public static final ItemType EXPRESSION = new ItemType("expression");

    /**
     * Mixin: node has access control.
     */
    public static final String MIX_ACCESSCONTROLLABLE = MgnlNodeType.MIX_ACCESSCONTROLLABLE;

    /**
     * Mixin: node can be referenced.
     */
    public static final String MIX_REFERENCEABLE = JcrConstants.MIX_REFERENCEABLE;

    /**
     * Mixin: node can be versioned.
     */
    public static final String MIX_VERSIONABLE = JcrConstants.MIX_VERSIONABLE;

    public static final String MIX_LOCKABLE = JcrConstants.MIX_LOCKABLE;

    public static final String DELETED_NODE_MIXIN = NodeTypes.Deleted.NAME;

    /**
     * Magnolia content.
     */
    public static final String NT_CONTENT = NodeTypes.Content.NAME;

    public static final String MGNL_NODE_DATA = MgnlNodeType.MGNL_NODE_DATA;

    public static final String NT_FROZENNODE = JcrConstants.NT_FROZENNODE;

    public static final String JCR_FROZENNODE = JcrConstants.JCR_FROZENNODE;

    public static final String JCR_FROZEN_PRIMARY_TYPE = JcrConstants.JCR_FROZENPRIMARYTYPE;

    public static final String JCR_PRIMARY_TYPE = JcrConstants.JCR_PRIMARYTYPE;

    public static final String JCR_DATA = JcrConstants.JCR_DATA;

    /**
     * Magnolia content node.
     * @deprecated use ItemType.CONTENTNODE
     */
    @Deprecated
    public static final String NT_CONTENTNODE = NodeTypes.ContentNode.NAME;

    public static final ItemType CONTENT = new ItemType(NT_CONTENT);

    public static final ItemType CONTENTNODE = new ItemType(NT_CONTENTNODE);

    public static final ItemType USER = new ItemType(NodeTypes.User.NAME);

    public static final ItemType ROLE = new ItemType(NodeTypes.Role.NAME);

    public static final ItemType GROUP = new ItemType(NodeTypes.Group.NAME);

    public static final ItemType SYSTEM = new ItemType(NodeTypes.System.NAME);

    public static final ItemType JCR_CONTENT = new ItemType(JcrConstants.JCR_CONTENT);

    public static final ItemType FOLDER = new ItemType(NT_FOLDER);

    public static final ItemType PAGE = new ItemType(NodeTypes.Page.NAME);
    public static final ItemType AREA = new ItemType(NodeTypes.Area.NAME);
    public static final ItemType COMPONENT = new ItemType(NodeTypes.Component.NAME);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Node name.
     */
    private final String systemName;

    /**
     * Ctor.
     * @param systemName jcr system name
     */
    public ItemType(String systemName) {
        this.systemName = systemName;
    }

    /**
     * Getter for <code>name</code>.
     * @return Returns the name.
     */
    public String getSystemName() {
        return this.systemName;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ItemType)) {
            return false;
        }
        ItemType rhs = (ItemType) object;
        return this.systemName.equals(rhs.systemName);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.systemName;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.systemName.hashCode();
    }

}
