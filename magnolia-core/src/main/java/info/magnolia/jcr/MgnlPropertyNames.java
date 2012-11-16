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
 * Defines constants for frequently used JCR property names, both those defined by JCR and the custom ones defined by Magnolia.
 */
public final class MgnlPropertyNames {

    /**
     * Prefix for JCR properties.
     */
    public static final String JCR_PREFIX = "jcr:";

    /**
     * Suffix for property names keeping user ids.
     */
    private static final String BY = "By";

    // from mgnl:created (mixin)
    public static final String CREATED = MgnlNodeTypeNames.MGNL_PREFIX + "created";
    public static final String CREATED_BY = CREATED + BY;

    // from mgnl:activatable (mixin)
    public static final String LAST_ACTIVATED = MgnlNodeTypeNames.MGNL_PREFIX + "lastActivated";
    public static final String LAST_ACTIVATED_BY = LAST_ACTIVATED + BY;
    public static final String ACTIVATION_STATUS = MgnlNodeTypeNames.MGNL_PREFIX + "activationStatus";

    // from mgnl:renderable (mixin)
    public static final String TEMPLATE = MgnlNodeTypeNames.MGNL_PREFIX + "template";

    // from nt:base
    public static final String PRIMARY_TYPE = JCR_PREFIX + "primaryType";

    // from nt:resource
    public static final String DATA = JCR_PREFIX + "data";

    // from nt:file
    public static final String CONTENT = JCR_PREFIX + "content";

    // from nt:version
    public static final String FROZEN_NODE = JCR_PREFIX + "frozenNode";

    // from nt:frozenNode
    public static final String FROZEN_PRIMARY_TYPE = JCR_PREFIX + "frozenPrimaryType";

    // from mix:lastModified
    public static final String LAST_MODIFIED = JCR_PREFIX + "lastModified";
    public static final String LAST_MODIFIED_BY = LAST_MODIFIED + BY;

    // from mgnl:deleted
    public static final String DELETED_BY = "mgnl:deletedBy";
    public static final String DELETED = "mgnl:deleted";
}
