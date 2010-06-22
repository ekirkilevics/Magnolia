/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.rest.dialogx;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.ExtendingContentWrapper;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.objectfactory.Components;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.jcr.RepositoryException;
import java.util.*;

public class DialogRegistry extends ObservedManager {

    private static final String ND_NAME = "name";

    private Map<String, Content> dialogs = Collections.synchronizedMap(new HashMap<String, Content>());

    public Content getDialogConfigNode(String name) {
        return dialogs.get(name);
    }

    @Override
    protected void onRegister(Content node) {

        final List<Content> dialogNodes = new ArrayList<Content>();
        try {
            collectDialogNodes(node, dialogNodes);
        }
        catch (RepositoryException e) {
            log.error("Can't collect dialog nodes for [" + node.getHandle() + "]: " + ExceptionUtils.getMessage(e), e);
            throw new IllegalStateException("Can't collect dialog nodes for [" + node.getHandle() + "]: " + ExceptionUtils.getMessage(e));
        }

        for (Iterator<Content> iter = dialogNodes.iterator(); iter.hasNext();) {
            Content dialogNode = new ExtendingContentWrapper(new SystemContentWrapper(iter.next()));
            try {
                if (dialogNode.getItemType().equals(ItemType.CONTENT)) {
                    log.warn("Dialog definitions should be of type contentNode but [" + dialogNode.getHandle() + "] is of type content.");
                }
            }
            catch (RepositoryException e) {
                log.error("Can't check for node type of the dialog node [" + dialogNode.getHandle() + "]: " + ExceptionUtils.getMessage(e), e);
                throw new IllegalStateException("Can't check for node type of the dialog node [" + dialogNode.getHandle() + "]: " + ExceptionUtils.getMessage(e));
            }
            String name = dialogNode.getNodeData(ND_NAME).getString();
            if (StringUtils.isEmpty(name)) {
                name = dialogNode.getName();
            }

            registerDialog(name, dialogNode);
        }
    }

    private void registerDialog(String name, Content dialogNode) {
        this.dialogs.put(name, dialogNode);
    }

    @Override
    protected void onClear() {
        this.dialogs.clear();
    }

    public static DialogRegistry getInstance() {
        return Components.getSingleton(DialogRegistry.class);
    }

    protected void collectDialogNodes(Content current, List<Content> dialogNodes) throws RepositoryException {
        if (isDialogNode(current)) {
            dialogNodes.add(current);
            return;
        }
        for (Content child : ContentUtil.getAllChildren(current)) {
            collectDialogNodes(child, dialogNodes);
        }
    }

    protected boolean isDialogNode(Content node) throws RepositoryException {
        if (isDialogControlNode(node)) {
            return false;
        }

        // if leaf
        if (ContentUtil.getAllChildren(node).isEmpty()) {
            return true;
        }

        // if has node datas
        if (!node.getNodeDataCollection().isEmpty()) {
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

    protected boolean isDialogControlNode(Content node) throws RepositoryException {
        return node.hasNodeData("controlType") || node.hasNodeData("reference");
    }
}
