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
package info.magnolia.module.rest.tree;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.admininterface.TreeHandlerManager;
import info.magnolia.module.admininterface.trees.TemplateColumn;
import info.magnolia.module.rest.tree.commands.*;
import info.magnolia.module.rest.tree.config.*;
import info.magnolia.objectfactory.Classes;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Bootstraps a ConfiguredTreeHandler into repository from a configured AdminTreeMVCHandler. Test code to get something in the repo to test with.
 */
public class LegacyTreeHandlerFactory {

    public boolean bootstrapLegacyTreeHandler(String treeName) throws RepositoryException, Content2BeanException {

        HttpServletRequest request = MgnlContext.getWebContext().getRequest();
        HttpServletResponse response = MgnlContext.getWebContext().getResponse();

        ConfiguredTreeHandler configuration = createLegacyConfiguration(treeName, request, response);

        if (configuration == null)
            return false;

        writeConfigurationToRepository(configuration);

        return true;
    }

    private ConfiguredTreeHandler createLegacyConfiguration(String treeName, HttpServletRequest request, HttpServletResponse response) {

        AdminTreeMVCHandler treeHandler = TreeHandlerManager.getInstance().getTreeHandler(treeName, request, response);

        Tree tree = Classes.quietNewInstance(treeHandler.getTreeClass(), treeHandler.getName(), treeHandler.getRepository());
        if (tree == null) {
            tree = Classes.quietNewInstance(treeHandler.getTreeClass(), treeHandler.getName(), treeHandler.getRepository(), treeHandler.getRequest());
        }

        if (tree == null)
            return null;

        tree.setRootPath(treeHandler.getRootPath());

        boolean browseMode = false;

        treeHandler.getConfiguration().prepareTree(tree, browseMode, request);
        treeHandler.getConfiguration().prepareContextMenu(tree, browseMode, request);
        treeHandler.getConfiguration().prepareFunctionBar(tree, browseMode, request);

        Map<String, String> icons = (Map<String, String>) reflectiveGetField(tree, "icons");
        List<String> itemTypes = tree.getItemTypes();
        Set<String> strictItemTypes = (Set<String>) reflectiveGetField(tree, "strictTypes");
        Comparator sortComparator = tree.getSortComparator();

        ConfiguredTreeHandler handler = new ConfiguredTreeHandler();
        handler.setName(tree.getName());
        handler.setSortComparator(sortComparator);
        handler.setItemTypes(itemTypes);
        handler.setStrictItemTypes(strictItemTypes);
        handler.setRepository(tree.getRepository());
        handler.setRootPath(tree.getRootPath());

        JsonTreeConfiguration c = new JsonTreeConfiguration();
        c.setFlatMode(!tree.getDrawShifter());

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
            c.addContextMenuItem(menuItem);
        }

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
            c.addFunctionMenuItem(menuItem);
        }

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
        handler.setConfiguration(c);
        return handler;
    }

    private void writeConfigurationToRepository(ConfiguredTreeHandler handler) throws RepositoryException {

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        String configPath = "/modules/rest/genuine-trees/";

        Content configParent = hierarchyManager.getContent(configPath, true, ItemType.CONTENT);
        configParent.getParent().save();

        Content handlerNode = configParent.createContent(handler.getName(), ItemType.CONTENTNODE);
        handlerNode.setNodeData("repository", handler.getRepository());
        handlerNode.setNodeData("rootPath", handler.getRootPath());
        handlerNode.setNodeData("i18nBaseName", handler.getI18nBaseName());

        writeStringCollection(handlerNode, "itemTypes", handler.getItemTypes());
        writeStringCollection(handlerNode, "strictItemTypes", handler.getStrictItemTypes());

        Comparator sortComparator = handler.getSortComparator();
        if (sortComparator != null)
            handlerNode.createContent("sortComparator", ItemType.CONTENTNODE).setNodeData("class", sortComparator.getClass().getName());

        writeConfiguration(handlerNode, handler.getConfiguration());

        writeCommands(handlerNode, handler.getName());

        configParent.save();
    }

    private void writeCommands(Content handlerNode, String name) throws RepositoryException {
        Content commandsNode = handlerNode.createContent("commands", ItemType.CONTENTNODE);
        if (name.equals("website")) {
            Content content = commandsNode.createContent("create", ItemType.CONTENTNODE);
            content.setNodeData("class", CreateWebsiteNodeCommand.class.getName());
            content.setNodeData("itemType", ItemType.CONTENT.getSystemName());
        } else {
            Content content = commandsNode.createContent("create", ItemType.CONTENTNODE);
            content.setNodeData("class", CreateNodeCommand.class.getName());
            content.setNodeData("itemType", ItemType.CONTENT.getSystemName());
        }
        commandsNode.createContent("delete", ItemType.CONTENTNODE).setNodeData("class", DeleteNodeCommand.class.getName());
        commandsNode.createContent("setMetaData", ItemType.CONTENTNODE).setNodeData("class", SetMetaDataCommand.class.getName());
        commandsNode.createContent("setNodeData", ItemType.CONTENTNODE).setNodeData("class", SetNodeDataCommand.class.getName());
    }

    private void writeConfiguration(Content handlerNode, JsonTreeConfiguration cfg) throws RepositoryException {

        Content configNode = handlerNode.createContent("configuration", ItemType.CONTENTNODE);

        configNode.setNodeData("class", cfg.getClass().getName());
        configNode.setNodeData("flatMode", cfg.isFlatMode());

        writeMenu(configNode, cfg.getContextMenuItems(), "contextMenuItems");

        writeMenu(configNode, cfg.getFunctionMenuItems(), "functionMenuItems");

        Content columnsNode = configNode.createContent("columns", ItemType.CONTENTNODE);
        int i = 0;
        for (JsonTreeColumn column : cfg.getColumns()) {
            Content columnNode = columnsNode.createContent(String.valueOf(i++), ItemType.CONTENTNODE);
            columnNode.setNodeData("title", column.getTitle());
            columnNode.setNodeData("width", column.getWidth());
            columnNode.setNodeData("class", column.getClass().getName());

            if (column instanceof JsonTreeColumnLabel) {
                JsonTreeColumnLabel x = (JsonTreeColumnLabel) column;
                writeStringMap(columnNode, "icons", x.getIcons());
            } else if (column instanceof JsonTreeColumnMetaData) {
                JsonTreeColumnMetaData x = (JsonTreeColumnMetaData) column;
                columnNode.setNodeData("readOnly", x.isReadOnly());
                columnNode.setNodeData("name", x.getName());
                columnNode.setNodeData("dateFormat", x.getDateFormat());
            } else if (column instanceof JsonTreeColumnNodeData) {
                JsonTreeColumnNodeData x = (JsonTreeColumnNodeData) column;
                columnNode.setNodeData("readOnly", x.isReadOnly());
                columnNode.setNodeData("name", x.getName());
                columnNode.setNodeData("dateFormat", x.getDateFormat());
            } else if (column instanceof JsonTreeColumnNodeDataType) {
            } else if (column instanceof JsonTreeColumnNodeDataValue) {
                JsonTreeColumnNodeDataValue x = (JsonTreeColumnNodeDataValue) column;
                columnNode.setNodeData("dateFormat", x.getDateFormat());
            } else if (column instanceof JsonTreeColumnTemplate) {
            }
        }
    }

    private void writeStringCollection(Content parentNode, String name, Collection<String> collection) throws RepositoryException {
        Content node = parentNode.createContent(name, ItemType.CONTENTNODE);
        int j = 0;
        for (String s : collection) {
            node.setNodeData(String.valueOf(j++), s);
        }
    }

    private void writeStringMap(Content parentNode, String name, Map<String, String> map) throws RepositoryException {
        Content mapNode = parentNode.createContent(name, ItemType.CONTENTNODE);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            mapNode.setNodeData(entry.getKey().replace(':', '-'), entry.getValue());
        }
    }

    private void writeMenu(Content parentNode, List<JsonMenuItem> menuItems, String name) throws RepositoryException {
        Content menuNode = parentNode.createContent(name, ItemType.CONTENTNODE);
        int j = 0;
        for (JsonMenuItem item : menuItems) {
            Content itemNode = menuNode.createContent(StringUtils.defaultString(item.getName(), String.valueOf(j++)), ItemType.CONTENTNODE);
            itemNode.setNodeData("class", item.getClass().getName());
            itemNode.setNodeData("label", item.getLabel());
            itemNode.setNodeData("icon", item.getIcon());
            itemNode.setNodeData("separator", item.isSeparator());
            itemNode.setNodeData("command", item.getCommand());
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
}
