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

import info.magnolia.jcr.MgnlNodeTypeNames;
import info.magnolia.jcr.MgnlPropertyNames;

/**
 * Keeps constants for node-types used within Magnolia. {@link org.apache.jackrabbit.JcrConstants} is not used as it only contains jcr 1.0 constants and is to be
 * replaced by the extended forms hosted on the jcr 2.0 interfaces (e.g. NodeType).
 *
 * @deprecated since 5.0 use {@link info.magnolia.jcr.MgnlNodeTypeNames} for mgnl node types or {@link info.magnolia.jcr.MgnlPropertyNames} for mgnl property names.
 */
public final class MgnlNodeType {

    /**
     * Prefix for our nodeTypes.
     */
    public static final String NT_PREFIX = MgnlNodeTypeNames.NT_PREFIX;

    /**
     * Prefix for jcr mixin's.
     */
    public static final String MIX_PREFIX = MgnlNodeTypeNames.MIX_PREFIX;

    /**
     * Prefix for mgnl-properties.
     */
    public static final String MGNL_PREFIX = MgnlNodeTypeNames.MGNL_PREFIX;

    /**
     * Prefix for jcr-properties.
     */
    public static final String JCR_PREFIX = MgnlPropertyNames.JCR_PREFIX;

    /**
     * Node type: base.
     */
    public static final String NT_BASE = MgnlNodeTypeNames.BASE;

    /**
     * Node type: unstructured.
     */
    public static final String NT_UNSTRUCTURED = MgnlNodeTypeNames.UNSTRUCTURED;

    /**
     * Node type: hierarchyNode.
     */
    public static final String NT_HIERARCHY = MgnlNodeTypeNames.HIERARCHY_NODE;

    /**
     * Node type: folder.
     */
    public static final String NT_FOLDER = MgnlNodeTypeNames.FOLDER;

    /**
     * Node type: file.
     */
    public static final String NT_FILE = MgnlNodeTypeNames.FILE;

    /**
     * Node type: resource.
     */
    public static final String NT_RESOURCE = MgnlNodeTypeNames.RESOURCE;

    /**
     * Node type: metadata.
     */
    public static final String NT_METADATA = MgnlNodeTypeNames.METADATA;

    /**
     * Node type: content.
     */
    public static final String NT_CONTENT = MgnlNodeTypeNames.CONTENT;

    /**
     * Node type: content node.
     */
    public static final String NT_CONTENTNODE = MgnlNodeTypeNames.CONTENT_NODE;

    public static final String NT_PAGE = MgnlNodeTypeNames.PAGE;
    public static final String NT_AREA = MgnlNodeTypeNames.AREA;
    public static final String NT_COMPONENT = MgnlNodeTypeNames.COMPONENT;

    public static final String MGNL_NODE_DATA = MgnlNodeTypeNames.NODE_DATA;

    public static final String NT_FROZENNODE = MgnlNodeTypeNames.FROZEN_NODE;

    public static final String USER = MgnlNodeTypeNames.USER;

    public static final String ROLE = MgnlNodeTypeNames.ROLE;

    public static final String GROUP = MgnlNodeTypeNames.GROUP;

    public static final String SYSTEM = MgnlNodeTypeNames.SYSTEM;

    // Mixins

    /**
     * Mixin: node has access control.
     */
    public static final String MIX_ACCESSCONTROLLABLE = MgnlNodeTypeNames.MIX_ACCESS_CONTROLLABLE;

    /**
     * Mixin: node can be referenced.
     */
    public static final String MIX_REFERENCEABLE = MgnlNodeTypeNames.MIX_REFERENCEABLE;

    /**
     * Mixin: node can be versioned.
     */
    public static final String MIX_VERSIONABLE = MgnlNodeTypeNames.MIX_VERSIONABLE;

    public static final String MIX_LOCKABLE = MgnlNodeTypeNames.MIX_LOCKABLE;

    public static final String MIX_DELETED = MgnlNodeTypeNames.MIX_DELETED;

    // JCR properties.

    public static final String JCR_FROZENNODE = MgnlPropertyNames.FROZEN_NODE;

    public static final String JCR_FROZEN_PRIMARY_TYPE = MgnlPropertyNames.FROZEN_PRIMARY_TYPE;

    public static final String JCR_PRIMARY_TYPE = MgnlPropertyNames.PRIMARY_TYPE;

    public static final String JCR_DATA = MgnlPropertyNames.DATA;

    public static final String JCR_CONTENT = MgnlPropertyNames.CONTENT;

}
