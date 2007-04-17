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
 * @author philipp
 * @version $Id$
 *
 */
public interface Bean2ContentTransformer {
    public ItemType resolveItemType(TransformationState state);
    public ItemType initContent(TransformationState state);
    public ItemType finishContent(TransformationState state);
    public boolean isNode(TransformationState state, String propertyName);
    public boolean isProperty(TransformationState state, String propertyName);
    public void setNodeData(TransformationState state, String name, Object value);
}
