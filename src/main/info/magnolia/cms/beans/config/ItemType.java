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
package info.magnolia.cms.beans.config;

/**
 * @author Sameer Charles
 */
public class ItemType {

    /**
     * Basic node types
     */
    public static final String NT_BASE = "nt:base";

    public static final String NT_UNSTRUCTRUED = "nt:unstructured";

    public static final String NT_HIERARCHY = "nt:hierarchyNode"; // changed from "item" to "node"

    public static final String NT_FOLDER = "nt:folder";

    public static final String NT_FILE = "nt:file";

    /**
     * Basic mixin types
     */
    public static final String MIX_AccessControllable = "mix:accessControllable";

    public static final String MIX_Referenceable = "mix:referenceable";

    public static final String MIX_Versionable = "mix:versionable";

    /**
     * magnolia specific basic node types
     */
    public static final String NT_CONTENT = "mgnl:content";

    public static final String NT_CONTENTNODE = "mgnl:contentNode";

    public static final String NT_NODEDATA = "mgnl:nodeData";

    /**
     * internal magnolia item type values
     */
    public static final int MAGNOLIA_NODE_DATA = 0;

    public static final int MAGNOLIA_CONTENT_NODE = 1;

    public static final int MAGNOLIA_PAGE = 2;

    /**
     * magnolia item types
     */
    public static final String MAGNOLIA_SYSTEM_DETAILS = "magnoliaSystem";

    public static final String MAGNOLIA_INTERNAL_TYPE = "internalType";

    /**
     * Basic child node types
     */
    public static String JCR_CONTENT = "jcr:content";

    /**
     * @param id (id is a use defined name for the JCR Item type)
     */
    public static String getSystemName(String id) {
        return id;
    }
}
