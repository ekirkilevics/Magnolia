/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;

/**
 * A task to move and rename properties, taking default values into account. If the property
 * existed with original default, it is replaced by the new default, otherwise its value is kept.
 * If the new property exists, its value is replaced by either the new default of the original
 * property's value. This permits bootstrapping complex nodes, and migrating a few simple properties
 * when necessary.
 * Override the modifyCurrentValue() method to provide special behaviour when replacing existing
 * values.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MoveAndRenamePropertyTask extends AbstractRepositoryTask {
    private final String originalParentNode;
    private final String originalPropertyName;
    private final String originalDefaultValue;

    private final String newParentNode;
    private final String newPropertyName;
    private final String newDefaultValue;

    public MoveAndRenamePropertyTask(String name, String originalParentNode, String originalPropertyName, String newParentNode, String newPropertyName) {
        this(name, originalParentNode, originalPropertyName, null, newParentNode, newPropertyName, null);
    }

    public MoveAndRenamePropertyTask(String name, String originalParentNode, String originalPropertyName, String originalDefaultValue, String newParentNode, String newPropertyName, String newDefaultValue) {
        super(name, "The property at " + originalParentNode + "/" + originalPropertyName +
                " is now at " + newParentNode + "/" + newPropertyName +
                (originalDefaultValue != null ? (", and its default value is now \"" + newDefaultValue + "\" instead of \"" + originalDefaultValue + "\"") : "")
                + ".");
        this.originalParentNode = originalParentNode;
        this.originalPropertyName = originalPropertyName;
        this.originalDefaultValue = originalDefaultValue;
        this.newParentNode = newParentNode;
        this.newPropertyName = newPropertyName;
        this.newDefaultValue = newDefaultValue;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getConfigHierarchyManager();
        if (!hm.isExist(originalParentNode)) {
            ctx.warn("Can't find node at " + originalParentNode + ". Please create its replacement at " + newParentNode + " with a property named " + newPropertyName + " and a value of " + newDefaultValue);
            return;
        }
        final Content originalNode = hm.getContent(originalParentNode);
        final NodeData originalProp = originalNode.getNodeData(originalPropertyName);
        final String currentValue = originalProp.getString();
        if (originalProp.isExist()) {
            originalProp.delete();
        }

        final String newValue;
        if (StringUtils.isEmpty(currentValue) || currentValue.equals(originalDefaultValue)) {
            newValue = newDefaultValue;
        } else {
            newValue = modifyCurrentValue(currentValue);
        }

        if (!hm.isExist(newParentNode)) {
            ctx.warn("Can't find node at " + newParentNode + ". Please create it with a property named " + newPropertyName + " and a value of " + newDefaultValue);
            return;
        }
        final Content newNode = hm.getContent(newParentNode);
        if (newNode.hasNodeData(newPropertyName)) {
            final NodeData newProp = newNode.getNodeData(newPropertyName);
            ctx.info("Replacing property " + newPropertyName + " at " + newParentNode + " with value " + newValue + ". Previous value was " + newProp.getString());
            newProp.setValue(newValue);
        } else {
            newNode.createNodeData(newPropertyName, newValue);
        }
    }

    protected String modifyCurrentValue(String currentValue) {
        return currentValue;
    }
}
