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
 * @version $Revision: $ ($Author: $)
 */
public final class ItemType implements Serializable {

    /**
     * Node type: base.
     */
    public static final String NT_BASE = "nt:base";

    /**
     * Node type: unstructured.
     */
    public static final String NT_UNSTRUCTRUED = "nt:unstructured";

    /**
     * Node type: hierarchyNode.
     */
    public static final String NT_HIERARCHY = "nt:hierarchyNode";

    /**
     * Node type: folder.
     */
    public static final String NT_FOLDER = "nt:folder";

    /**
     * Node type: base.
     */
    public static final String NT_FILE = "nt:file";

    /**
     * Mixin: node has access control.
     */
    public static final String MIX_ACCESSCONTROLLABLE = "mix:accessControllable";

    /**
     * Mixin: node can be referenced.
     */
    public static final String MIX_REFERENCEABLE = "mix:referenceable";

    /**
     * Mixin: node can be versioned.
     */
    public static final String MIX_VERSIONABLE = "mix:versionable";

    /**
     * Magnolia content.
     * @deprecated use ItemType.CONTENT
     */
    public static final String NT_CONTENT = "mgnl:content";

    /**
     * Magnolia content node.
     * @deprecated use ItemType.CONTENTNODE
     */
    public static final String NT_CONTENTNODE = "mgnl:contentNode";

    /**
     * @deprecated
     */
    public static final String NT_NODEDATA = "mgnl:nodeData";

    /**
     * "mgnl:content"
     */
    public static final ItemType CONTENT = new ItemType("mgnl:content");

    /**
     * "mgnl:contentNode"
     */
    public static final ItemType CONTENTNODE = new ItemType("mgnl:contentNode");

    /**
     * "jcr:content"
     */
    public static final ItemType JCR_CONTENT = new ItemType("jcr:content");

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Can't be instantiated.
     * @param systemName jcr system name
     */
    private ItemType(String systemName) {
        this.systemName = systemName;
    }

    /**
     * Node name.
     */
    private String systemName;

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
