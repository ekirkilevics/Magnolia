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
package info.magnolia.cms.core;

import java.io.Serializable;

/**
 * The Magnolia equivalent to {@link NodeType}.
 *
 * @version $Id$
 *
 * @deprecated since 5.0 - use {@link MgnlNodeType}}.
 */
public final class ItemType implements Serializable {

    /**
     * Node type: base.
     */
    public static final String NT_BASE = "nt:base"; //$NON-NLS-1$

    /**
     * Node type: unstructured.
     */
    public static final String NT_UNSTRUCTURED = "nt:unstructured"; //$NON-NLS-1$

    /**
     * Node type: hierarchyNode.
     */
    public static final String NT_HIERARCHY = "nt:hierarchyNode"; //$NON-NLS-1$

    /**
     * Node type: folder.
     */
    public static final String NT_FOLDER = "mgnl:folder"; //$NON-NLS-1$

    /**
     * Node type: base.
     */
    public static final String NT_FILE = "nt:file"; //$NON-NLS-1$

    /**
     * Node type: resource.
     */
    public static final String NT_RESOURCE = "mgnl:resource"; //$NON-NLS-1$

    /**
     * Node type: metadata.
     */
    public static final String NT_METADATA = "mgnl:metaData"; //$NON-NLS-1$

    /**
     * "wfe:workItem".
     */
    public static final ItemType WORKITEM = new ItemType("workItem");

    /**
     * "wfe:expression".
     */
    public static final ItemType EXPRESSION = new ItemType("expression"); //$NON-NLS-1$

    /**
     * Mixin: node has access control.
     */
    public static final String MIX_ACCESSCONTROLLABLE = "mix:accessControllable"; //$NON-NLS-1$

    /**
     * Mixin: node can be referenced.
     */
    public static final String MIX_REFERENCEABLE = "mix:referenceable"; //$NON-NLS-1$

    /**
     * Mixin: node can be versioned.
     */
    public static final String MIX_VERSIONABLE = "mix:versionable"; //$NON-NLS-1$

    public static final String MIX_LOCKABLE = "mix:lockable"; //$NON-NLS-1$

    public static final String DELETED_NODE_MIXIN = "mgnl:deleted";

    /**
     * Magnolia content.
     * @deprecated use ItemType.CONTENT
     */
    @Deprecated
    public static final String NT_CONTENT = "mgnl:content"; //$NON-NLS-1$

    public static final String MGNL_NODE_DATA = "mgnl:nodeData";

    public static final String NT_FROZENNODE = "nt:frozenNode";

    public static final String JCR_FROZENNODE = "jcr:frozenNode";

    public static final String JCR_FROZEN_PRIMARY_TYPE = "jcr:frozenPrimaryType";

    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";

    public static final String JCR_DATA = "jcr:data";

    /**
     * Magnolia content node.
     * @deprecated use ItemType.CONTENTNODE
     */
    @Deprecated
    public static final String NT_CONTENTNODE = "mgnl:contentNode"; //$NON-NLS-1$

    public static final ItemType CONTENT = new ItemType("mgnl:content"); //$NON-NLS-1$

    public static final ItemType CONTENTNODE = new ItemType("mgnl:contentNode"); //$NON-NLS-1$

    public static final ItemType USER = new ItemType("mgnl:user"); //$NON-NLS-1$

    public static final ItemType ROLE = new ItemType("mgnl:role"); //$NON-NLS-1$

    public static final ItemType GROUP = new ItemType("mgnl:group"); //$NON-NLS-1$

    public static final ItemType SYSTEM = new ItemType("mgnl:reserve"); //$NON-NLS-1$

    public static final ItemType JCR_CONTENT = new ItemType("jcr:content"); //$NON-NLS-1$

    public static final ItemType FOLDER = new ItemType(NT_FOLDER);


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
