/**
 * This file Copyright (c) 2010-2010 Magnolia International
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
package info.magnolia.module.admininterface.setup.for4_3;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Check for each module in the config repository if dialogs are of the incorrect type <em>mgnl:content</em> 
 * and attempts to replace them with the correct one <em>mgnl:contentNode<em>.<br>
 * See also jira MAGNOLIA-2810
 * @author fgrilli
 * @version $Id$
 *
 */
public class ReplaceWrongDialogNodeTypeTask extends AbstractRepositoryTask {
    
    private final static Logger log = LoggerFactory.getLogger(ReplaceWrongDialogNodeTypeTask.class);
    
    public ReplaceWrongDialogNodeTypeTask() {
        super("Replace incorrect dialog node types", "Checks for each module in the config repository if dialogs are of the incorrect type mgnl:content and replaces them with the correct one mgnl:contentNode");
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final Collection<Content> dialogs = QueryUtil.query(ContentRepository.CONFIG, "modules/*/dialogs", Query.XPATH);
        List<Content> dialogNodes = new ArrayList<Content>();
        
        for (Iterator<Content> iterator = dialogs.iterator(); iterator.hasNext();) {
            collectDialogNodes(iterator.next(), dialogNodes);
        }
        log.debug("Found {} dialog(s)", dialogNodes.size());
        
        for (Content srcNode : dialogNodes) {
            final String handle = srcNode.getHandle();
            try {
                log.debug("Checking if {} needs to be replaced due to incorrect dialog type...", handle);
                if(!srcNode.isNodeType(ItemType.CONTENTNODE.getSystemName())){
                    ContentUtil.changeNodeType(srcNode, ItemType.CONTENTNODE, false);
                }
            }
            catch (RepositoryException e) {
                installContext.error("Can't replace " + handle, e);
            }
        }
    }

    //the following private methods were copied from DialogHandlerManager 
    private void collectDialogNodes(Content current, List<Content> dialogNodes) throws RepositoryException {
        if(isDialogNode(current)){
            dialogNodes.add(current);
            return;
        }
        for (Content child : ContentUtil.getAllChildren(current)) {
            collectDialogNodes(child, dialogNodes);
        }
    }

    private boolean isDialogNode(Content node) throws RepositoryException{
        if(isDialogControlNode(node)){
            return false;
        }

        // if leaf
        if(ContentUtil.getAllChildren(node).isEmpty()){
            return true;
        }

        // if has node datas
        if(!node.getNodeDataCollection().isEmpty()){
            return true;
        }

        // if one subnode is a control
        for (Content child : node.getChildren(ItemType.CONTENTNODE)) {
            if (isDialogControlNode(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDialogControlNode(Content node) throws RepositoryException{
        return node.hasNodeData("controlType") || node.hasNodeData("reference");
    }

}
