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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.admininterface.TreeHandlerManager;
import info.magnolia.module.admininterface.trees.TemplateColumn;
import info.magnolia.module.rest.tree.TreeNode;
import info.magnolia.module.rest.tree.TreeNodeData;
import info.magnolia.module.rest.tree.TreeNodeList;
import info.magnolia.module.rest.tree.config.*;
import info.magnolia.objectfactory.Classes;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.lang.reflect.Field;
import java.util.*;

@Path("/tree")
public class TreeJsonEndpoint {

    @GET
    @Path("/{treeName}")
    public TreeNodeList getNode(
            @PathParam("treeName") String treeName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {
        return getNode(treeName, "", request, response);
    }

    @GET
    @Path("/{treeName}/{path:(.)*}")
    public TreeNodeList getNode(
            @PathParam("treeName") String treeName,
            @PathParam("path") String path,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {

        JsonTreeConfiguration treeConfiguration = getTreeConfiguration(treeName, request, response);

        Content content = getContent(treeConfiguration, path);

        return marshallTreeNodeChildren(treeConfiguration, content);
    }

    @POST
    @Path("/{treeName}/config")
    public JsonTreeConfiguration getConfiguration(
            @PathParam("treeName") String treeName,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {

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
        for (Content child : this.getChildren(treeConfiguration, content)) {
            nodes.addChild(marshallTreeNode(treeConfiguration, child));
        }
        return nodes;
    }

    private TreeNode marshallTreeNode(JsonTreeConfiguration treeConfiguration, Content content) throws RepositoryException {

        AbsolutePath repositoryPath = new AbsolutePath(content.getHandle());
        AbsolutePath treeRootPath = new AbsolutePath(treeConfiguration.getRootPath());
        AbsolutePath treePath = repositoryPath.relativeTo(treeRootPath);

        TreeNode treeNode = new TreeNode();
        treeNode.setName(content.getName());
        treeNode.setType(content.getNodeTypeName());
        treeNode.setUuid(content.getUUID());
        treeNode.setPath(treePath.path());

        treeNode.setHasChildren(hasChildren(treeConfiguration, content));

        treeNode.setColumnValues(readColumnValues(treeConfiguration, content));

        if (treeConfiguration.getItemTypes().contains(Tree.ITEM_TYPE_NODEDATA)) {
            List<NodeData> nodeDatas = new ArrayList<NodeData>(content.getNodeDataCollection());

            Collections.sort(nodeDatas, new Comparator<NodeData>() {

                public int compare(NodeData arg0, NodeData arg1) {
                    return arg0.getName().compareTo(arg1.getName());
                }
            });

            for (NodeData nodeData : nodeDatas) {
                TreeNodeData data = new TreeNodeData();
                data.setName(nodeData.getName());
                data.setType(NodeDataUtil.getTypeName(nodeData));
                for (JsonTreeColumn column : treeConfiguration.getColumns()) {
                    data.addNodeData(column.getValue(content, nodeData));
                }
                treeNode.addNodeData(data);
            }
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

    private JsonTreeConfiguration getTreeConfiguration(String treeName, HttpServletRequest request, HttpServletResponse response) throws RepositoryException, Content2BeanException {

        JsonTreeConfiguration configuration = readConfigurationFromRepository(treeName);

        if (configuration == null) {
            configuration = createLegacyConfiguration(treeName, request, response);
            if (configuration == null)
                return null;
            writeConfigurationToRepository(configuration);
            configuration = readConfigurationFromRepository(treeName);
        }
        return configuration;
    }

    private JsonTreeConfiguration readConfigurationFromRepository(String treeName) throws RepositoryException, Content2BeanException {
        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        String configPath = "/modules/rest/genuine-trees/" + treeName;
        if (hierarchyManager.isExist(configPath)) {
            Content content = hierarchyManager.getContent(configPath);
            return (JsonTreeConfiguration) Content2BeanUtil.toBean(content, true, JsonTreeConfiguration.class);
        }
        return null;
    }

    private JsonTreeConfiguration createLegacyConfiguration(String treeName, HttpServletRequest request, HttpServletResponse response) {

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

        Map<String, String> icons = (Map<String, String>) reflectiveGetField(tree, "icons");
        List<String> itemTypes = tree.getItemTypes();
        Set<String> strictItemTypes = (Set<String>) reflectiveGetField(tree, "strictTypes");
        Comparator sortComparator = tree.getSortComparator();

        JsonTreeConfiguration c = new JsonTreeConfiguration();
        c.setName(tree.getName());
        c.setSortComparator(sortComparator);
        c.setItemTypes(itemTypes);
        c.setStrictItemTypes(strictItemTypes);
        c.setFlatMode(!tree.getDrawShifter());
        c.setRepository(tree.getRepository());
        c.setRootPath(tree.getRootPath());

        JsonMenu contextMenu = new JsonMenu();
        List<ContextMenuItem> mis = tree.getMenu().getMenuItems();
        for (ContextMenuItem mi : mis) {
            JsonMenuItem menuItem = new JsonMenuItem();
            if (mi == null) {
                menuItem.setSeparator(true);
            } else {
                menuItem.setName(mi.getName());
                menuItem.setLabel(mi.getLabel());
                menuItem.setIcon(mi.getIcon());
            }
            contextMenu.addItem(menuItem);
        }
        c.setContextMenu(contextMenu);

        JsonMenu functionMenu = new JsonMenu();
        List<FunctionBarItem> fis = tree.getFunctionBar().getMenuItems();
        for (FunctionBarItem fi : fis) {
            JsonMenuItem menuItem = new JsonMenuItem();
            if (fi == null) {
                menuItem.setSeparator(true);
            } else {
                menuItem.setName(fi.getName());
                menuItem.setLabel(fi.getLabel());
                menuItem.setIcon(fi.getIcon());
            }
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
                col.setIcons(icons);
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
                col.setDateFormat(column.getDateFormat());
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

    private void writeConfigurationToRepository(JsonTreeConfiguration c) throws RepositoryException {

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        String configPath = "/modules/rest/genuine-trees/";

        Content content = hierarchyManager.getContent(configPath, true, ItemType.CONTENT);
        content.getParent().save();

        Content tnode = content.createContent(c.getName(), ItemType.CONTENTNODE);
        tnode.setNodeData("flatMode", c.isFlatMode());
        tnode.setNodeData("repository", c.getRepository());
        tnode.setNodeData("rootPath", c.getRootPath());
        tnode.setNodeData("i18nBaseName", c.getI18nBaseName());

        if (c.getContextMenu() != null) {
            writeMenu(tnode, c.getContextMenu(), "contextMenu");
        }

        if (c.getFunctionMenu() != null) {
            writeMenu(tnode, c.getFunctionMenu(), "functionMenu");
        }

        Content cnode = tnode.createContent("columns", ItemType.CONTENTNODE);
        int i = 0;
        for (JsonTreeColumn column : c.getColumns()) {
            Content cc = cnode.createContent(String.valueOf(i++), ItemType.CONTENTNODE);
            cc.setNodeData("title", column.getTitle());
            cc.setNodeData("width", column.getWidth());
            cc.setNodeData("class", column.getClass().getName());

            if (column instanceof JsonTreeColumnLabel) {
                JsonTreeColumnLabel x = (JsonTreeColumnLabel) column;
                writeStringMap(cc, "icons", x.getIcons());
            } else if (column instanceof JsonTreeColumnMetaData) {
                JsonTreeColumnMetaData x = (JsonTreeColumnMetaData) column;
                cc.setNodeData("readOnly", x.isReadOnly());
                cc.setNodeData("name", x.getName());
                cc.setNodeData("dateFormat", x.getDateFormat());
            } else if (column instanceof JsonTreeColumnNodeData) {
                JsonTreeColumnNodeData x = (JsonTreeColumnNodeData) column;
                cc.setNodeData("readOnly", x.isReadOnly());
                cc.setNodeData("name", x.getName());
                cc.setNodeData("dateFormat", x.getDateFormat());
            } else if (column instanceof JsonTreeColumnNodeDataType) {
            } else if (column instanceof JsonTreeColumnNodeDataValue) {
                JsonTreeColumnNodeDataValue x = (JsonTreeColumnNodeDataValue) column;
                cc.setNodeData("dateFormat", x.getDateFormat());
            } else if (column instanceof JsonTreeColumnTemplate) {
            }
        }

        Comparator sortComparator = c.getSortComparator();
        if (sortComparator != null)
            tnode.createContent("sortComparator", ItemType.CONTENTNODE).setNodeData("class", sortComparator.getClass().getName());

        writeStringCollection(tnode, "itemTypes", c.getItemTypes());
        writeStringCollection(tnode, "strictItemTypes", c.getStrictItemTypes());

        content.save();
    }

    private void writeStringCollection(Content tnode, String name, Collection<String> cc) throws RepositoryException {
        Content nn = tnode.createContent(name, ItemType.CONTENTNODE);
        int j = 0;
        for (String s : cc) {
            nn.setNodeData(String.valueOf(j++), s);
        }
    }

    private void writeStringMap(Content tnode, String name, Map<String, String> cc) throws RepositoryException {
        Content nn = tnode.createContent(name, ItemType.CONTENTNODE);
        for (Map.Entry<String, String> entry : cc.entrySet()) {
            nn.setNodeData(entry.getKey().replace(':', '-'), entry.getValue());
        }
    }

    private void writeMenu(Content tnode, JsonMenu menu, String name) throws RepositoryException {
        Content mnode = tnode.createContent(name, ItemType.CONTENTNODE);
        mnode.setNodeData("class", menu.getClass().getName());
        Content inode = mnode.createContent("items", ItemType.CONTENTNODE);
        int j = 0;
        for (JsonMenuItem item : menu.getItems()) {
            Content ii = inode.createContent(StringUtils.defaultString(item.getName(), String.valueOf(j++)), ItemType.CONTENTNODE);
            ii.setNodeData("class", item.getClass().getName());
            ii.setNodeData("label", item.getLabel());
            ii.setNodeData("icon", item.getIcon());
            ii.setNodeData("separator", item.isSeparator());
            ii.setNodeData("command", item.getCommand());
        }
    }

    private Object reflectiveGetField(Object instance, String fieldName) {
        // temporary, didnt wanna add getters in adminInterface...
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private Content getContent(JsonTreeConfiguration treeConfiguration, String path) throws RepositoryException {

        AbsolutePath p = new AbsolutePath(treeConfiguration.getRootPath(), path);

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(treeConfiguration.getRepository());

        if (!hierarchyManager.isExist(p.path()))
            return null;

        return hierarchyManager.getContent(p.path());
    }

    private boolean hasChildren(JsonTreeConfiguration treeConfiguration, Content content) throws RepositoryException {
        return !getChildren(treeConfiguration, content).isEmpty();
    }

    public List<Content> getChildren(JsonTreeConfiguration treeConfiguration, Content content) throws RepositoryException {
        List<Content> children = new ArrayList<Content>();
        // loop the children of the different item types
        for (int i = 0; i < treeConfiguration.getItemTypes().size(); i++) {
            String type = treeConfiguration.getItemTypes().get(i);
            if (hasSub(content, type)) {
                children.addAll(getChildrenOfOneType(treeConfiguration, content, type));
            }
        }
        return children;
    }

    protected boolean hasSub(Content content, String itemType) {
        return content.getChildren(itemType).size() > 0;
    }

    public List<Content> getChildrenOfOneType(JsonTreeConfiguration treeConfiguration, Content parentNode, String itemType) throws RepositoryException {
        List<Content> children = new ArrayList<Content>();
        Iterator it = collectRenderedItems(treeConfiguration, parentNode, itemType);
        while (it.hasNext()) {
            Object maybeContent = it.next();
            if (maybeContent instanceof Content) {
                Content c = (Content) maybeContent;
                // ensure no subtypes of strict types are included by mistake
                if (treeConfiguration.getStrictItemTypes().contains(itemType) && !c.getItemType().getSystemName().equals(itemType)) {
                    continue;
                }
                children.add(c);
            }
        }
        return children;
    }

    protected Iterator collectRenderedItems(JsonTreeConfiguration treeConfiguration, Content parentNode, String itemType) {
        List<Content> nodes = new ArrayList<Content>(parentNode.getChildren(itemType));
        Comparator comparator = treeConfiguration.getSortComparator();
        if (comparator != null) {
            Collections.sort(nodes, comparator);
        }
        return nodes.iterator();
    }
}
