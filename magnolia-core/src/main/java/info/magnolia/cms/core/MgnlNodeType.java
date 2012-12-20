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

/**
 * Keeps constants for node-types used within Magnolia.
 * Only when converting MetaData to mixin(s) (MAGNOLIA-4640) we realized that we could benefit a lot from something
 * more powerful than just a few constants. We wanted to model what properties are defined on what nodeTypes and also host the related utility methods at the same place.
 * That's how {@link info.magnolia.jcr.util.NodeTypes} was born. As this type was only introduced with Magnolia 4.5 we'll not drop it we'll not drop it in the near future,
 * even though we deprecated it in 5.0 release.
 *
 * @deprecated since 5.0 use {@link info.magnolia.jcr.util.NodeTypes} for mgnl nodeType or property names or {@link org.apache.jackrabbit.JcrConstants} for jcr ones.
 */
public final class MgnlNodeType {

    /**
     * Prefix for our nodeTypes.
     */
    public static final String NT_PREFIX = "nt:";

    /**
     * Prefix for jcr mixin's.
     */
    public static final String MIX_PREFIX = "mix:";

    /**
     * Prefix for mgnl-properties.
     */
    public static final String MGNL_PREFIX = NodeTypes.MGNL_PREFIX;

    /**
     * Prefix for jcr-properties.
     */
    public static final String JCR_PREFIX = NodeTypes.JCR_PREFIX;

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
     * Node type: file.
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
     * Node type: content.
     */
    public static final String NT_CONTENT = NodeTypes.Content.NAME;

    /**
     * Node type: content node.
     */
    public static final String NT_CONTENTNODE = NodeTypes.ContentNode.NAME;

    public static final String NT_PAGE = NodeTypes.Page.NAME;
    public static final String NT_AREA = NodeTypes.Area.NAME;
    public static final String NT_COMPONENT = NodeTypes.Component.NAME;

    public static final String MGNL_NODE_DATA = NodeTypes.NodeData.NAME;

    public static final String NT_FROZENNODE = JcrConstants.NT_FROZENNODE;

    public static final String USER = NodeTypes.User.NAME;

    public static final String ROLE = NodeTypes.Role.NAME;

    public static final String GROUP = NodeTypes.Group.NAME;

    public static final String SYSTEM = NodeTypes.System.NAME;

    // Mixins

    /**
     * Mixin: node has access control.
     */
    public static final String MIX_ACCESSCONTROLLABLE = MIX_PREFIX + "accessControllable";

    /**
     * Mixin: node can be referenced.
     */
    public static final String MIX_REFERENCEABLE = JcrConstants.MIX_REFERENCEABLE;

    /**
     * Mixin: node can be versioned.
     */
    public static final String MIX_VERSIONABLE = JcrConstants.MIX_VERSIONABLE;

    public static final String MIX_LOCKABLE = JcrConstants.MIX_LOCKABLE;

    public static final String MIX_DELETED = NodeTypes.Deleted.NAME;

    // JCR properties.

    public static final String JCR_FROZENNODE = JcrConstants.JCR_FROZENNODE;

    public static final String JCR_FROZEN_PRIMARY_TYPE = JcrConstants.JCR_FROZENPRIMARYTYPE;

    public static final String JCR_PRIMARY_TYPE = JcrConstants.JCR_PRIMARYTYPE;

    public static final String JCR_DATA = JcrConstants.JCR_DATA;

    public static final String JCR_CONTENT = JcrConstants.JCR_CONTENT;

}
