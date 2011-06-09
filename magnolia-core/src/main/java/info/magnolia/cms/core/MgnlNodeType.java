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

import javax.jcr.nodetype.NodeType;

/**
 * Constant for node-types used within Magnolia - the equivalent to {@link NodeType}.
 *
 * @version $Id$
 */
public final class MgnlNodeType {

    /**
     * Prefix for our nodeTypes.
     */
    public static final String NT_PREFIX = "nt:";

    /**
     * Prefix for our mixin's.
     */
    public static final String MIX_PREFIX = "mix:";

    /**
     * Prefix for mgnl-properties.
     */
    public static final String MGNL_PREFIX = "mgnl:";

    /**
     * Prefix for jcr-properties.
     */
    public static final String JCR_PREFIX = "jcr:";

    /**
     * Node type: base.
     */
    public static final String NT_BASE = NodeType.NT_BASE;

    /**
     * Node type: unstructured.
     */
    public static final String NT_UNSTRUCTURED = NodeType.NT_UNSTRUCTURED;

    /**
     * Node type: hierarchyNode.
     */
    public static final String NT_HIERARCHY = NodeType.NT_HIERARCHY_NODE;

    /**
     * Node type: folder.
     *
     * TODO dlipp: why not NodeType.NT_FOLDER - why prefixed with mgnl???
     */
    public static final String NT_FOLDER = MGNL_PREFIX + "folder";

    /**
     * Node type: file.
     */
    public static final String NT_FILE = NodeType.NT_FILE;

    /**
     * Node type: resource.
     */
    public static final String NT_RESOURCE = MGNL_PREFIX+ "resource";

    /**
     * Node type: metadata.
     */
    public static final String NT_METADATA = MGNL_PREFIX+ "metaData";

    /**
     * Mixin: node has access control.
     */
    public static final String MIX_ACCESSCONTROLLABLE = MIX_PREFIX + "accessControllable";

    /**
     * Mixin: node can be referenced.
     */
    public static final String MIX_REFERENCEABLE = NodeType.MIX_REFERENCEABLE;

    /**
     * Mixin: node can be versioned.
     */
    public static final String MIX_VERSIONABLE = NodeType.MIX_VERSIONABLE;

    public static final String MIX_LOCKABLE = NodeType.MIX_LOCKABLE;

    public static final String DELETED_NODE_MIXIN = MGNL_PREFIX + "deleted";

    /**
     * Magnolia content.
     */
    public static final String NT_CONTENT = MGNL_PREFIX + "content";

    /**
     * TODO dlipp: to be replaced by mgnl:property? NodeData is Content-API term...
     */
    public static final String MGNL_NODE_DATA = MGNL_PREFIX + "nodeData";

    public static final String NT_FROZENNODE = NodeType.NT_FROZEN_NODE;

    public static final String JCR_FROZENNODE = JCR_PREFIX + "frozenNode";

    public static final String JCR_FROZEN_PRIMARY_TYPE = JCR_PREFIX + "frozenPrimaryType";

    public static final String JCR_PRIMARY_TYPE = JCR_PREFIX + "primaryType";

    public static final String JCR_DATA = JCR_PREFIX + "data";

    /**
     * Magnolia content node.
     */
    public static final String NT_CONTENTNODE = MGNL_PREFIX + "contentNode";

    public static final String USER = MGNL_PREFIX + "user";

    public static final String ROLE = MGNL_PREFIX + "role";

    public static final String GROUP = MGNL_PREFIX + "group";

    public static final String SYSTEM = MGNL_PREFIX + "reserve";

    public static final String JCR_CONTENT = JCR_PREFIX + "content";
}
