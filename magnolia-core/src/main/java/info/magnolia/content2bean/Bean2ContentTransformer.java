/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
