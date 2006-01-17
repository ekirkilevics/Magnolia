/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import java.io.Serializable;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public final class ItemType implements Serializable {

    /**
     * Node type: base.
     */
    public static final String NT_BASE = "nt:base"; //$NON-NLS-1$

    /**
     * Node type: unstructured.
     */
    public static final String NT_UNSTRUCTRUED = "nt:unstructured"; //$NON-NLS-1$

    /**
     * Node type: hierarchyNode.
     */
    public static final String NT_HIERARCHY = "nt:hierarchyNode"; //$NON-NLS-1$

    /**
     * Node type: folder.
     */
    public static final String NT_FOLDER = "nt:folder"; //$NON-NLS-1$

    /**
     * Node type: base.
     */
    public static final String NT_FILE = "nt:file"; //$NON-NLS-1$

    /**
     * Node type: resource
     */
    public static final String NT_RESOURCE = "nt:resource"; //$NON-NLS-1$

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

    /**
     * Magnolia content.
     * @deprecated use ItemType.CONTENT
     */
    public static final String NT_CONTENT = "mgnl:content"; //$NON-NLS-1$

    /**
     * jcr:frozenNode
     */
    public static final String JCR_FROZENNODE = "jcr:frozenNode";

    /**
     * jcr:data
     */
    public static final String JCR_DATA = "jcr:data";

    /**
     * Magnolia content node.
     * @deprecated use ItemType.CONTENTNODE
     */
    public static final String NT_CONTENTNODE = "mgnl:contentNode"; //$NON-NLS-1$

    /**
     * @deprecated
     */
    public static final String NT_NODEDATA = "mgnl:nodeData"; //$NON-NLS-1$

    /**
     * "mgnl:content"
     */
    public static final ItemType CONTENT = new ItemType("mgnl:content"); //$NON-NLS-1$

    /**
     * "mgnl:contentNode"
     */
    public static final ItemType CONTENTNODE = new ItemType("mgnl:contentNode"); //$NON-NLS-1$

    /**
     * "mgnl:contentNode"
     */
    public static final ItemType SYSTEM = new ItemType("mgnl:system"); //$NON-NLS-1$

    /**
     * "jcr:content"
     */
    public static final ItemType JCR_CONTENT = new ItemType("jcr:content"); //$NON-NLS-1$

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Node name.
     */
    private String systemName;

    /**
     * Can't be instantiated.
     * @param systemName jcr system name
     */
    private ItemType(String systemName) {
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
    public String toString() {
        return this.systemName;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.systemName.hashCode();
    }

}
