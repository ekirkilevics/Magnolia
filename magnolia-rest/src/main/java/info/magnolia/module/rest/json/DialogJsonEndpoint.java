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
package info.magnolia.module.rest.json;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.rest.dialog.Dialog;
import info.magnolia.module.rest.dialog.DialogRegistry;
import info.magnolia.module.rest.dialog.ValidationResult;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/dialogs")
public class DialogJsonEndpoint {

    private static Logger log = LoggerFactory.getLogger(DialogJsonEndpoint.class);

    @GET
    @Path("/{dialogName}")
    public Dialog getDialog(
            @PathParam("dialogName") String dialogName,
            @QueryParam("mgnlRepository") String repository,
            @QueryParam("mgnlPath") String path,
            @QueryParam("mgnlNodeCollectionName") String nodeCollection,
            @QueryParam("mgnlNode") String node) throws RepositoryException {

        Dialog dialog = DialogRegistry.getInstance().getDialogProvider(dialogName).create();

        Content storageNode = getStorageNode(repository, path, nodeCollection, node);

        dialog.bind(storageNode);

        return dialog;
    }

    @POST
    @Path("/{dialogName}/create")
    public ValidationResult create(
            @PathParam("dialogName") String dialogName,
            @QueryParam("mgnlRepository") String repository,
            @QueryParam("mgnlPath") String path,
            @QueryParam("mgnlNodeCollectionName") String nodeCollection,
            @QueryParam("mgnlNode") String node,
            @Context UriInfo uriInfo) throws Exception {

        Dialog dialog = DialogRegistry.getInstance().getDialogProvider(dialogName).create();

        dialog.bind(uriInfo.getQueryParameters());

        ValidationResult validationResult = new ValidationResult();
        dialog.validate(validationResult);
        if (validationResult.isSuccess()) {
            synchronized (ExclusiveWrite.getInstance()) {

                HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

                Content rootNode = hm.getContent(path);

                Content storageNode = createStorageNode(rootNode, nodeCollection, node);

                dialog.save(storageNode);

                storageNode.getMetaData().setTemplate("samplesHowToJSP");

                rootNode.save();
            }
        }

        return validationResult;
    }

    @POST
    @Path("/{dialogName}/update")
    public ValidationResult update(
            @PathParam("dialogName") String dialogName,
            @QueryParam("mgnlRepository") String repository,
            @QueryParam("mgnlPath") String path,
            @QueryParam("mgnlNodeCollectionName") String nodeCollection,
            @QueryParam("mgnlNode") String node,
            @Context UriInfo uriInfo) throws Exception {

        Dialog dialog = DialogRegistry.getInstance().getDialogProvider(dialogName).create();

        Content storageNode = getStorageNode(repository, path, nodeCollection, node);

        dialog.bind(storageNode);

        dialog.bind(uriInfo.getQueryParameters());

        ValidationResult validationResult = new ValidationResult();
        dialog.validate(validationResult);
        if (validationResult.isSuccess()) {
            synchronized (ExclusiveWrite.getInstance()) {
                dialog.save(storageNode);
                storageNode.save();
            }
        }

        return validationResult;
    }

    // Snippet taken from DialogSaveHandlerImpl and modified to not use mgnlNew as nodeName placeholder
    private Content createStorageNode(Content rootNode, String nodeCollectionName, String nodeName) throws RepositoryException {

        Content nodeCollection = null;
        if (StringUtils.isNotEmpty(nodeCollectionName)) {
            try {
                nodeCollection = rootNode.getContent(nodeCollectionName);
            }
            catch (RepositoryException re) {
                // nodeCollection does not exist -> create
                nodeCollection = rootNode.createContent(nodeCollectionName, ItemType.CONTENTNODE);
                log.debug("Create - {}" + nodeCollection.getHandle()); //$NON-NLS-1$
            }
        } else {
            nodeCollection = rootNode;
        }

        Content node;

        if (StringUtils.isNotEmpty(nodeName)) {
            node = nodeCollection.createContent(nodeName, ItemType.CONTENTNODE.getSystemName());
        } else {
            nodeName = info.magnolia.cms.core.Path.getUniqueLabel(rootNode.getHierarchyManager(), nodeCollection.getHandle(), "0"); //$NON-NLS-1$
            node = nodeCollection.createContent(nodeName, ItemType.CONTENTNODE.getSystemName());
        }

        return node;
    }

    // This is DialogMVCHandler.getStorageNode()
    private Content getStorageNode(String repository, String path, String nodeCollectionName, String nodeName) {

        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);

        Content storageNode = null;

        try {
            if (path == null) {
//                log.debug("No path defined for a dialog called by the url [{}]", this.getRequest().getRequestURL());
                return null;
            }
            Content parentContent = hm.getContent(path);
            if (StringUtils.isEmpty(nodeName)) {
                if (StringUtils.isEmpty(nodeCollectionName)) {
                    storageNode = parentContent;
                } else {
                    storageNode = parentContent.getContent(nodeCollectionName);
                }
            } else {
                if (StringUtils.isEmpty(nodeCollectionName)) {
                    storageNode = parentContent.getContent(nodeName);

                } else {
                    storageNode = parentContent.getContent(nodeCollectionName).getContent(nodeName);

                }
            }
        }
        catch (AccessDeniedException ade) {
            log.error("can't read content to edit", ade);
        }
        catch (RepositoryException re) {
            // content does not exist yet
            log.debug("can't read content or it does not exist yet", re);
        }

        return storageNode;
    }
}
