/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.jcr;

/**
 * Keeps constants for frequently used nodeTypes and mixin names (both - mgnl or jcr defined).
 * All mixins are prefixed with MIX.
 *
 * Does not use {@link org.apache.jackrabbit.JcrConstants} from Jackrabbit's common-jar as these
 * are jcr 1.0 and are to be replaced by the extended forms placed on the jcr interfaces (https://issues.apache.org/jira/browse/JCR-3059).
 */
public final class MgnlNodeTypeNames {

    /**
     * Prefix for mgnl nodetypes or properties.
     */
    public static final String MGNL_PREFIX = "mgnl:";

    /**
     * Prefix for jcr defined mixins.
     */
    public static final String MIX_PREFIX = "mix:";

    /**
     * Prefix for jcr defined nodeTypes.
     */
    public static final String NT_PREFIX = "nt:";

    // jcr defined nodetypes
    public static final String BASE = NT_PREFIX + "base";
    public static final String UNSTRUCTURED = NT_PREFIX + "unstructured";
    public static final String NT_ = NT_PREFIX + "hierarchyNode";
    public static final String FOLDER = NT_PREFIX + "folder";
    public static final String FILE = NT_PREFIX + "file";
    public static final String RESOURCE = NT_PREFIX + "resource";
    public static final String FROZEN_NODE = NT_PREFIX + "frozenNode";

    // mgnl defined nodetypes
    public static final String CONTENT = MGNL_PREFIX + "content";
    public static final String CONTENT_NODE = MGNL_PREFIX + "contentNode";
    public static final String NODE_DATA = MGNL_PREFIX + "nodeData";

    public static final String PAGE = MGNL_PREFIX + "page";
    public static final String AREA = MGNL_PREFIX + "area";
    public static final String COMPONENT = MGNL_PREFIX + "component";

    public static final String USER = MGNL_PREFIX + "user";
    public static final String ROLE = MGNL_PREFIX + "role";
    public static final String GROUP = MGNL_PREFIX + "group";
    public static final String SYSTEM = MGNL_PREFIX + "reserve";

    public static final String METADATA = MGNL_PREFIX + "metaData";

    // mgnl mixins
    public static final String MIX_DELETED = MGNL_PREFIX + "deleted";
    public static final String MIX_ACTIVATABLE = MGNL_PREFIX + "activatable";
    public static final String MIX_RENDERABLE = MGNL_PREFIX + "renderable";
    public static final String MIX_CREATED = MGNL_PREFIX + "created";

    // jcr mixins.
    public static final String MIX_REFERENCEABLE = MIX_PREFIX + "referenceable";
    public static final String MIX_VERSIONABLE = MIX_PREFIX + "versionable";
    public static final String MIX_LOCKABLE = MIX_PREFIX + "lockable";
    public static final String MIX_ACCESS_CONTROLLABLE = MIX_PREFIX + "accessControllable";
}
