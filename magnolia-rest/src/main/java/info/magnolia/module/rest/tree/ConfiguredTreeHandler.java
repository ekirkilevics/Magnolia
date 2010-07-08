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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.rest.json.AbsolutePath;
import info.magnolia.module.rest.tree.commands.TreeCommand;
import info.magnolia.module.rest.tree.config.JsonTreeColumn;
import info.magnolia.module.rest.tree.config.JsonTreeConfiguration;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ConfiguredTreeHandler implements TreeHandler {

    private String name;
    private String repository;
    private String rootPath;
    private JsonTreeConfiguration configuration;
    private List<String> itemTypes = new ArrayList<String>();
    private Set<String> strictItemTypes = new HashSet<String>();
    private Comparator sortComparator;

    private String i18nBaseName;
    private Messages messages;

    private Content configNode;

    // called by content2bean
    public void setContent(Content configNode) {
        this.configNode = configNode;
    }

    // called by content2bean
    public void init() {

        initMessages();
    }

    private void initMessages() {

        messages = MessagesManager.getMessages();
        if (StringUtils.isNotEmpty(i18nBaseName))
            messages = MessagesUtil.chain(i18nBaseName, messages);

        if (configuration != null)
            configuration.initMessages(messages);
    }

    public TreeNodeList getChildren(String path) throws RepositoryException {

        Content content = getContent(getAbsolutePath(path));

        if (content == null)
            return null;

        return marshallTreeNodeChildren(content);
    }

    public JsonTreeConfiguration getConfiguration() {
        return configuration;
    }

    public Object executeCommand(String path, String commandName, Map parameters) throws Exception {

        TreeCommand command = createCommandObject(path, commandName, parameters);

        // Invoke the command
        Object result = command.execute();

        // Return a suitable response

        // When we create a node we should return a TreeNodeList of its parent (the client needs to see how the new node is ordered among its siblings)

        // When we update a node its enough to return just that node

        // When we move a node around we need to return a TreeNodeList of its new parent (the client also needs to know that it's not still there)

        // When we copy a node we need to return a TreeNodeList of the newly created nodes parent (the client needs to see it in order with its siblings)

        // Will also need to return messages (AlertUtil equivalent)

        // TODO this is temporary code, see the comments above about what should be returned...
        return marshallTreeNodeChildren((Content) result);
    }

    private TreeCommand createCommandObject(String path, String commandName, Map parameters) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, RepositoryException, Content2BeanException {

        // TODO: should use this.configNode once the ObservedManager is written
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        String commandPath = "/modules/rest/genuine-trees/" + name + "/commands/" + commandName;
        if (!hm.isExist(commandPath)) {
            throw new IllegalArgumentException("Unknown command [" + commandName + "]");
        }

        TreeCommand command = (TreeCommand) Content2BeanUtil.toBean(hm.getContent(commandPath));

        // Common parameters
        command.setRepository(this.repository);
        command.setPath(getAbsolutePath(path));

        // Request parameters are set using reflection
        for (Object parameterName : parameters.keySet()) {
            String parameter = getFirstParameter(parameters, (String) parameterName);
            BeanUtils.setProperty(command, (String) parameterName, parameter);
        }

        return command;
    }

    private AbsolutePath getAbsolutePath(String relative) {
        return new AbsolutePath(this.getRootPath(), relative);
    }

    private String getFirstParameter(Map parameters, String name) {

        // Since we're using HttpServletRequest.getParameters() ... temporary

        Object o = parameters.get(name);
        if (o instanceof String)
            return (String) o;
        if (o instanceof String[]) {
            String[] array = (String[]) o;
            if (array.length > 0)
                return array[0];
        }
        return null;
    }

    private TreeNodeList marshallTreeNodeChildren(Content content) throws RepositoryException {
        TreeNodeList nodes = new TreeNodeList();
        for (Content child : this.getChildren(content)) {
            nodes.addChild(marshallTreeNode(child));
        }
        return nodes;
    }

    private TreeNode marshallTreeNode(Content content) throws RepositoryException {

        AbsolutePath repositoryPath = new AbsolutePath(content.getHandle());
        AbsolutePath treeRootPath = new AbsolutePath(getRootPath());
        AbsolutePath treePath = repositoryPath.relativeTo(treeRootPath);

        TreeNode treeNode = new TreeNode();
        treeNode.setName(content.getName());
        treeNode.setType(content.getNodeTypeName());
        treeNode.setUuid(content.getUUID());
        treeNode.setPath(treePath.path());

        treeNode.setHasChildren(hasChildren(content));

        treeNode.setColumnValues(readColumnValues(configuration, content));

        if (getItemTypes().contains(Tree.ITEM_TYPE_NODEDATA)) {
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
                for (JsonTreeColumn column : configuration.getColumns()) {
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

    private Content getContent(AbsolutePath path) throws RepositoryException {

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(getRepository());

        if (!hierarchyManager.isExist(path.path()))
            return null;

        return hierarchyManager.getContent(path.path());
    }

    private boolean hasChildren(Content content) throws RepositoryException {
        return !getChildren(content).isEmpty();
    }

    public List<Content> getChildren(Content content) throws RepositoryException {
        List<Content> children = new ArrayList<Content>();
        // loop the children of the different item types
        for (int i = 0; i < getItemTypes().size(); i++) {
            String type = getItemTypes().get(i);
            if (hasSub(content, type)) {
                children.addAll(getChildrenOfOneType(content, type));
            }
        }
        return children;
    }

    protected boolean hasSub(Content content, String itemType) {
        return content.getChildren(itemType).size() > 0;
    }

    public List<Content> getChildrenOfOneType(Content parentNode, String itemType) throws RepositoryException {
        List<Content> children = new ArrayList<Content>();
        Iterator it = collectRenderedItems(parentNode, itemType);
        while (it.hasNext()) {
            Object maybeContent = it.next();
            if (maybeContent instanceof Content) {
                Content c = (Content) maybeContent;
                // ensure no subtypes of strict types are included by mistake
                if (getStrictItemTypes().contains(itemType) && !c.getItemType().getSystemName().equals(itemType)) {
                    continue;
                }
                children.add(c);
            }
        }
        return children;
    }

    protected Iterator collectRenderedItems(Content parentNode, String itemType) {
        List<Content> nodes = new ArrayList<Content>(parentNode.getChildren(itemType));
        Comparator comparator = getSortComparator();
        if (comparator != null) {
            Collections.sort(nodes, comparator);
        }
        return nodes.iterator();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public List<String> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<String> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public Set<String> getStrictItemTypes() {
        return strictItemTypes;
    }

    public void setStrictItemTypes(Set<String> strictItemTypes) {
        this.strictItemTypes = strictItemTypes;
    }

    public Comparator getSortComparator() {
        return sortComparator;
    }

    public void setSortComparator(Comparator sortComparator) {
        this.sortComparator = sortComparator;
    }

    public String getI18nBaseName() {
        return i18nBaseName;
    }

    public void setI18nBaseName(String i18nBaseName) {
        this.i18nBaseName = i18nBaseName;
    }

    public void addItemType(String itemType) {
        this.itemTypes.add(itemType);
    }

    public void addStrictItemType(String strictItemType) {
        this.strictItemTypes.add(strictItemType);
    }

    public void setConfiguration(JsonTreeConfiguration configuration) {
        this.configuration = configuration;
    }
}
