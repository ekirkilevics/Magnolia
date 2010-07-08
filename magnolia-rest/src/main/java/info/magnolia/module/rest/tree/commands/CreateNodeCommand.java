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
package info.magnolia.module.rest.tree.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.rest.json.AbsolutePath;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;

public class CreateNodeCommand extends AbstractTreeCommand {

    private String nodeName;
    private String itemType;

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemType() {
        return itemType;
    }

    public Content execute() throws RepositoryException {

        // nodeName is optional
        String name = StringUtils.defaultIfEmpty(this.nodeName, "untitled");

        AbsolutePath requestedPath = getPath().appendSegment(name);

        AbsolutePath uniquePath = getUniquePath(requestedPath);

        return createNode(uniquePath, itemType);
    }

    private AbsolutePath getUniquePath(AbsolutePath path) {

        String uniqueName = Path.getUniqueLabel(getHierarchyManager(), path.parentPath(), path.name());

        return path.parent().appendSegment(uniqueName);
    }

    private Content createNode(AbsolutePath path, String itemType) throws RepositoryException {

        Content parentNode = getHierarchyManager().getContent(path.parentPath());

        Content newNode = parentNode.createContent(path.name(), itemType);
        newNode.getMetaData().setAuthorId(MgnlContext.getUser().getName());
        newNode.getMetaData().setCreationDate();
        newNode.getMetaData().setModificationDate();

        synchronized (ExclusiveWrite.getInstance()) {
            parentNode.save();
        }

        return newNode;
    }
}
