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
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.admininterface.TreeHandlerManager;
import info.magnolia.module.admininterface.trees.TemplateColumn;
import info.magnolia.module.rest.tree.TreeNode;
import info.magnolia.module.rest.tree.TreeNodeData;
import info.magnolia.module.rest.tree.TreeNodeList;
import info.magnolia.module.rest.tree.config.*;
import info.magnolia.objectfactory.Classes;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/tree")
public class TreeJsonEndpoint {

    @GET
    @Path("/{treeName}")
    public TreeNodeList getNode(
            @PathParam("treeName") String treeName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws RepositoryException {
        return getNode(treeName, "", request, response);
    }

    @GET
    @Path("/{treeName}/{path:(.)*}")
    public TreeNodeList getNode(
            @PathParam("treeName") String treeName,
            @PathParam("path") String path,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws RepositoryException {

        JsonTreeConfiguration treeConfiguration = getTreeConfiguration(treeName, request, response);

        Content content = getContent(treeConfiguration, path);

        return marshallTreeNodeChildren(treeConfiguration, content);
    }

    @POST
    @Path("/{treeName}/config")
    public JsonTreeConfiguration getConfiguration(
            @PathParam("treeName") String treeName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws RepositoryException {

        return getTreeConfiguration(treeName, request, response);
    }

    @POST
    @Path("/{treeName}/command")
    public Object executeCommand(
            @PathParam("treeName") String treeName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws RepositoryException {

        return "";
    }

    private TreeNodeList marshallTreeNodeChildren(JsonTreeConfiguration treeConfiguration, Content content) throws RepositoryException {
        TreeNodeList nodes = new TreeNodeList();

        Collection<Content> children = content.getChildren(ItemType.CONTENT);
        for (Content child : children) {
            nodes.addChild(marshallTreeNode(treeConfiguration, child));
        }

        return nodes;
    }

    private TreeNode marshallTreeNode(JsonTreeConfiguration treeConfiguration, Content content) throws RepositoryException {

        AbsolutePath nodePath = new AbsolutePath(content.getHandle());
        AbsolutePath rootPath = new AbsolutePath(treeConfiguration.getRootPath());
        AbsolutePath treePath = nodePath.relativeTo(rootPath);

        TreeNode treeNode = new TreeNode();
        treeNode.setName(content.getName());
        treeNode.setType(content.getNodeTypeName());
        treeNode.setUuid(content.getUUID());
        treeNode.setPath(treePath.path());

        // TODO: this isnt enough, we have to use the registered itemtypes
        treeNode.setHasChildren(!content.getChildren(ItemType.CONTENT).isEmpty());

        treeNode.setColumnValues(readColumnValues(treeConfiguration, content));

        Collection<NodeData> nodeDatas = content.getNodeDataCollection();
        for (NodeData nodeData : nodeDatas) {
            TreeNodeData data = new TreeNodeData();
            data.setName(nodeData.getName());
            data.setType(NodeDataUtil.getTypeName(nodeData));

            for (JsonTreeColumn column : treeConfiguration.getColumns()) {
                data.addNodeData(column.getValue(content, nodeData));
            }
            treeNode.addNodeData(data);
        }

        return treeNode;
    }

    private List<Object> readColumnValues(JsonTreeConfiguration c, Content content) throws RepositoryException {
        List<Object> values = new ArrayList<Object>();
        for (JsonTreeColumn column : c.getColumns()) {
            values.add(column.getValue(content));
        }
        return values;
    }

    private JsonTreeConfiguration getTreeConfiguration(String treeName, HttpServletRequest request, HttpServletResponse response) {

        AdminTreeMVCHandler treeHandler = TreeHandlerManager.getInstance().getTreeHandler(treeName, request, response);

        Tree tree = Classes.quietNewInstance(treeHandler.getTreeClass(), treeHandler.getName(), treeHandler.getRepository());
        if (tree == null) {
            tree = Classes.quietNewInstance(treeHandler.getTreeClass(), treeHandler.getName(), treeHandler.getRepository(), treeHandler.getRequest());
        }
        tree.setRootPath(treeHandler.getRootPath());

        boolean browseMode = false;

        treeHandler.getConfiguration().prepareTree(tree, browseMode, request);
        treeHandler.getConfiguration().prepareContextMenu(tree, browseMode, request);
        treeHandler.getConfiguration().prepareFunctionBar(tree, browseMode, request);

        JsonTreeConfiguration c = new JsonTreeConfiguration();

        c.setFlatMode(!tree.getDrawShifter());
        c.setRepository(tree.getRepository());
        c.setRootPath(tree.getRootPath());

        JsonContextMenu contextMenu = new JsonContextMenu();
        List<ContextMenuItem> mis = tree.getMenu().getMenuItems();
        for (ContextMenuItem mi : mis) {
            if (mi == null) continue; // no seperators yet
            JsonContextMenuItem menuItem = new JsonContextMenuItem();
            menuItem.setName(mi.getName());
            menuItem.setLabel(mi.getLabel());
            menuItem.setIcon(mi.getIcon());
            contextMenu.addItem(menuItem);
        }
        c.setContextMenu(contextMenu);

        JsonFunctionMenu functionMenu = new JsonFunctionMenu();
        List<FunctionBarItem> fis = tree.getFunctionBar().getMenuItems();
        for (FunctionBarItem fi : fis) {
            if (fi == null) continue; // no seperators yet
            JsonFunctionMenuItem menuItem = new JsonFunctionMenuItem();
            menuItem.setName(fi.getName());
            menuItem.setLabel(fi.getLabel());
            menuItem.setIcon(fi.getIcon());
            functionMenu.addItem(menuItem);
        }
        c.setFunctionMenu(functionMenu);

        List<TreeColumn> columns = tree.getColumns();
        for (TreeColumn column : columns) {

            if (column instanceof TemplateColumn) {
                JsonTreeColumnTemplate col = new JsonTreeColumnTemplate();
                col.setTitle(column.getTitle());
                col.setWidth(column.getWidth());
                c.addColumn(col);
            } else if (column.getIsMeta()) {
                JsonTreeColumnMetaData col = new JsonTreeColumnMetaData();
                col.setName(column.getName());
                col.setTitle(column.getTitle());
                col.setWidth(column.getWidth());
                col.setDateFormat(column.getDateFormat());
                col.setReadOnly(column.getHtmlEdit() != null);
                c.addColumn(col);
            } else if (column.getIsLabel()) {
                JsonTreeColumnLabel col = new JsonTreeColumnLabel();
                col.setTitle(column.getTitle());
                col.setWidth(column.getWidth());
                c.addColumn(col);
            } else if (column.getIsIcons()) {
                JsonTreeColumnIcon col = new JsonTreeColumnIcon();
                col.setTitle(column.getTitle());
                col.setWidth(column.getWidth());
                col.setIconsActivation(column.getIconsActivation());
                col.setIconsPermission(column.getIconsPermission());
                c.addColumn(col);
            } else if (column.getIsNodeDataType()) {
                JsonTreeColumnNodeDataType col = new JsonTreeColumnNodeDataType();
                col.setTitle(column.getTitle());
                col.setWidth(column.getWidth());
                c.addColumn(col);
            } else if (column.getIsNodeDataValue()) {
                JsonTreeColumnNodeDataValue col = new JsonTreeColumnNodeDataValue();
                col.setTitle(column.getTitle());
                col.setWidth(column.getWidth());
                c.addColumn(col);
            } else {
                JsonTreeColumnNodeData col = new JsonTreeColumnNodeData();
                col.setName(column.getName());
                col.setTitle(column.getTitle());
                col.setWidth(column.getWidth());
                col.setDateFormat(column.getDateFormat());
                col.setReadOnly(column.getHtmlEdit() != null);
                c.addColumn(col);
            }
        }

        return c;
    }

    private Content getContent(JsonTreeConfiguration treeConfiguration, String path) throws RepositoryException {

        AbsolutePath p = new AbsolutePath(treeConfiguration.getRootPath(), path);

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(treeConfiguration.getRepository());

        if (!hierarchyManager.isExist(p.path()))
            return null;

        return hierarchyManager.getContent(p.path());
    }
}
