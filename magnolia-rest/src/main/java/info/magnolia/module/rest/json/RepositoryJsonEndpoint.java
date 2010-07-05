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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.rest.tree.RepositoryNode;
import info.magnolia.module.rest.tree.RepositoryNodeData;
import info.magnolia.module.rest.tree.TreeNode;
import info.magnolia.module.rest.tree.TreeNodeData;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.ws.rs.*;
import java.util.Collection;

@Path("/repositories")
public class RepositoryJsonEndpoint {

    @GET
    @Path("/{repositoryName}/{path:(.)*}")
    public RepositoryNode getNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) throws RepositoryException {

        HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryName);
        Content content = hm.getContent("/" + path);

        return marshallContent(content);
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/create")
    public RepositoryNode createNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @QueryParam("itemType") String itemType) throws RepositoryException {

        // depending on the itemType we will need to find a suitable command

        // itemType (content, contentnode, user, role, group... but likely not metaData and nodeData)
        // optional name? defaults to?
        //

        // we need to provide a default name and unique name generation too...

        HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryName);

        String parentPath = StringUtils.substringBeforeLast(path, "/"); //$NON-NLS-1$
        String nodeName = StringUtils.substringAfterLast(path, "/"); //$NON-NLS-1$
        if (StringUtils.isEmpty(parentPath))
            parentPath = "/";

        Content parentNode = hm.getContent(parentPath);
        Content newNode = parentNode.createContent(nodeName, new ItemType(itemType));

        synchronized (ExclusiveWrite.getInstance()) {
            parentNode.save();
        }

        return marshallContent(newNode);
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/setNodeData")
    public RepositoryNode setNodeData(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @QueryParam("name") String name,
            @QueryParam("value") String value,
            @QueryParam("type") String type) throws RepositoryException {

        HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryName);
        Content content = hm.getContent("/" + path);

        Value nodeData = NodeDataUtil.createValue(value, Integer.parseInt(type));
        content.setNodeData(name, nodeData);
        synchronized (ExclusiveWrite.getInstance()) {
            content.save();
        }

        return marshallContent(content);
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/removeNodeData")
    public RepositoryNode removeNodeData(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @QueryParam("name") String name) throws RepositoryException {

        HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryName);
        Content content = hm.getContent("/" + path);

        content.deleteNodeData(name);

        synchronized (ExclusiveWrite.getInstance()) {
            content.save();
        }

        return marshallContent(content);
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/delete")
    public RepositoryNode deleteNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) throws RepositoryException {

        // depending on the itemType might need to do different things

        HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryName);
        Content current = hm.getContent("/" + path);

        Content parent = current.getParent();
        current.delete();
        synchronized (ExclusiveWrite.getInstance()) {
            parent.save();
        }

        return new RepositoryNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/rename")
    public RepositoryNode renameNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @QueryParam("name") String name) throws RepositoryException {

        // depending on the itemType might need to do different things

        HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryName);
        Content current = hm.getContent("/" + path);

        ContentUtil.rename(current, name);
        synchronized (ExclusiveWrite.getInstance()) {
            current.getParent().save();
        }

        return marshallContent(current);
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/move")
    public TreeNode moveNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @PathParam("newPath") String newPath) {

        // place before/after

        // some itemTypes cannot be moved, i.e. users

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/copy")
    public TreeNode copyNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path,
            @PathParam("newPath") String newPath) {

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/activate")
    public TreeNode activateNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) {

        return new TreeNode();
    }

    @POST
    @Path("/{repositoryName}/{path:(.)*}/deactivate")
    public TreeNode deactivateNode(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("path") String path) {

        return new TreeNode();
    }

    private RepositoryNode marshallContent(Content content) throws RepositoryException {
        RepositoryNode treeNode = new RepositoryNode();
        treeNode.setName(content.getName());
        treeNode.setPath(content.getHandle());
        treeNode.setUuid(content.getUUID());
        treeNode.setType(content.getNodeTypeName());

        Collection<NodeData> nodeDatas = content.getNodeDataCollection();

        for (NodeData nodeData : nodeDatas) {

            RepositoryNodeData data = new RepositoryNodeData();
            data.setName(nodeData.getName());
            data.setType(String.valueOf(nodeData.getType()));
            data.setValue(NodeDataUtil.getValueString(nodeData));

            treeNode.getNodeData().add(data);
        }

        for (Content child : ContentUtil.getAllChildren(content)) {
            treeNode.addChild(child.getName());
        }

        return treeNode;
    }
}
