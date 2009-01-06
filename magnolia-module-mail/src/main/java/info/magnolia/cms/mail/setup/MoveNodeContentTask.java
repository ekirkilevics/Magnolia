/**
 * This file Copyright (c) 2008-2009 Magnolia International
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
package info.magnolia.cms.mail.setup;

import java.util.Iterator;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author tmiyar
 *
 */
public class MoveNodeContentTask extends AbstractRepositoryTask {
    private final String workspaceName;
    private final String src;
    private final String dest;
    private final ItemType type;
    private final boolean overwrite;

    public MoveNodeContentTask(String name, String description, String workspaceName, String src, String dest, ItemType type, boolean overwrite) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.src = src;
        this.dest = dest;
        this.overwrite = overwrite;
        this.type = type;
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = installContext.getHierarchyManager(workspaceName);
        if(hm.isExist(dest)){
            if(overwrite){
                hm.delete(dest);
            }
            else{
                installContext.error("Can't move " + src + " to " + dest + " because the target node already exists.", null);
                return;
            }
        }

        Content newNode = hm.getContent(dest, true, type);
        hm.save();

        Iterator iterator = hm.getContent(src).getChildren().iterator();

        while (iterator.hasNext()) {
            Content contentNode = (Content) iterator.next();
            if(contentNode.hasNodeData("body")) {
                contentNode.createNodeData("templateFile", contentNode.getNodeData("body").getValue().getString());
                contentNode.deleteNodeData("body");
                hm.save();
            }
            hm.save();
            hm.moveTo(contentNode.getHandle(), dest + "/" + StringUtils.substringAfterLast(contentNode.getHandle(), "/"));

        }

        hm.delete(src);
        hm.save();

    }

}
