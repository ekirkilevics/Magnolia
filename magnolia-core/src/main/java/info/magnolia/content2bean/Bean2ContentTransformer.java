/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.content2bean;

import info.magnolia.cms.core.ItemType;


/**
 * Used to create content nodes. This is a kind of configuration for the processor.
 * @author philipp
 * @version $Id$
 */
public interface Bean2ContentTransformer {

    /**
     * The item type of the node
     */
    public ItemType resolveItemType(TransformationState state);

    /**
     * The content name to use
     */
    public ItemType resolveName(TransformationState state);

    /**
     * Set mixin types or similar. Does not create the node!
     */
    public ItemType prepareContent(TransformationState state);

    /**
     * Called after the nodedatas and child content is set.
     */
    public ItemType finishContent(TransformationState state);

    /**
     * Will this property be a node?
     */
    public boolean isNode(TransformationState state, String propertyName, Object value);

    /**
     * Will this property be a node data?
     */
    public boolean isProperty(TransformationState state, String propertyName, Object value);

    /**
     * Create/set the node data of the node
     */
    public void setNodeData(TransformationState state, String name, Object value);

    /**
     * Convert complex vlues to the basic supported types (int, boolean, .. String)
     */
    public Object convertNodeDataValue(TransformationState state, String name, Object value);
}
