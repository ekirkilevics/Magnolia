/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.servlets.CommandBasedMVCServletHandler;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.commands.BaseActivationCommand;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * this class wrapes the tree control. The AdminInterfaceServlet instantiates a subclass. To build your own tree you
 * have to override the prepareTree() method
 * @author philipp
 * @author Fabrizio Giustina
 */

public class AdminTreeMVCHandler extends CommandBasedMVCServletHandler {

    /**
     * this are the used actions
     */
    protected static final String COMMAND_SHOW_TREE = "show"; //$NON-NLS-1$

    protected static final String COMMAND_COPY_NODE = "copy"; //$NON-NLS-1$

    protected static final String COMMAND_MOVE_NODE = "move"; //$NON-NLS-1$

    protected static final String COMMAND_ACTIVATE = "activate"; //$NON-NLS-1$

    protected static final String COMMAND_DEACTIVATE = "deactivate"; //$NON-NLS-1$

    protected static final String COMMAND_CREATE_NODE = "createNode"; //$NON-NLS-1$

    protected static final String COMMAND_DELETE_NODE = "delete"; //$NON-NLS-1$

    protected static final String COMMAND_SAVE_VALUE = "saveValue"; //$NON-NLS-1$

    /**
     * The view names
     */

    protected static final String VIEW_TREE = "tree"; //$NON-NLS-1$

    protected static final String VIEW_CREATE = "create"; //$NON-NLS-1$

    protected static final String VIEW_VALUE = "value"; //$NON-NLS-1$

    protected static final String VIEW_NOTHING = "nothing"; //$NON-NLS-1$

    protected static final String VIEW_COPY_MOVE = "copymove"; //$NON-NLS-1$

    /**
     * Log
     */
    private static Logger log = LoggerFactory.getLogger(AdminTreeMVCHandler.class);

    /**
     * name of the tree (not the repository)
     */
    protected Tree tree;

    /**
     * The class to instantiate a tree control
     */
    private String treeClass = Tree.class.getName();

    /**
     * The class used to instantiate a AdminTreeConfiguration if not provided. This can get configured in the trees
     * configuration node
     */
    private String configurationClass;

    /**
     * The configuration used to configure the tree
     */
    protected AdminTreeConfiguration configuration;

    protected String path;

    protected String pathOpen;

    protected String pathSelected;

    /**
     * Used to pass the saved value to the view
     */
    protected String displayValue;

    protected String newPath;

    private String repository;

    private String i18nBasename;

    /**
     * Used to display the same tree in the linkbrowser
     */
    protected boolean browseMode;

    /**
     * Override this method if you are not using the same name for the tree and the repository
     * @return name of the repository
     */
    public String getRepository() {
        if (repository == null) {
            repository = this.getName();
        }
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * @return the current HierarchyManager
     */
    public HierarchyManager getHierarchyManager() {
        return MgnlContext.getHierarchyManager(this.getRepository());
    }

    public AdminTreeMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public void init() {
        super.init();

        path = this.getRequest().getParameter("path"); //$NON-NLS-1$
        if (StringUtils.isEmpty(path)) {
            path = "/"; //$NON-NLS-1$
        }

        pathOpen = this.getRequest().getParameter("pathOpen"); //$NON-NLS-1$
        pathSelected = this.getRequest().getParameter("pathSelected"); //$NON-NLS-1$

        this.setBrowseMode(StringUtils.equals(this.getRequest().getParameter("browseMode"), "true"));
    }

    /**
     * Depending on the request it is generating a logical command name
     * @return name of the command
     */
    public String getCommand() {
        // if an explicit action was called
        if (StringUtils.isNotEmpty(super.getCommand())) {
            return super.getCommand();
        }

        // actions returned from the tree (pased through treeAction)
        if (StringUtils.isNotEmpty(this.getRequest().getParameter("treeAction"))) { //$NON-NLS-1$
            int treeAction = Integer.parseInt(this.getRequest().getParameter("treeAction")); //$NON-NLS-1$

            if (treeAction == Tree.ACTION_COPY) {
                return COMMAND_COPY_NODE;
            }
            if (treeAction == Tree.ACTION_MOVE) {
                return COMMAND_MOVE_NODE;
            }
            if (treeAction == Tree.ACTION_ACTIVATE) {
                return COMMAND_ACTIVATE;
            }
            if (treeAction == Tree.ACTION_DEACTIVATE) {
                return COMMAND_DEACTIVATE;
            }

            return this.getRequest().getParameter("treeAction"); //$NON-NLS-1$
        }

        // other actions depending other informations
        if (this.getRequest().getParameter("createItemType") != null) { //$NON-NLS-1$
            return COMMAND_CREATE_NODE;
        }

        if (this.getRequest().getParameter("deleteNode") != null) { //$NON-NLS-1$
            return COMMAND_DELETE_NODE;
        }

        // editet any value directly in the columns?
        if (this.getRequest().getParameter("saveName") != null //$NON-NLS-1$
            || this.getRequest().getParameter("saveValue") != null
            // value to save is a node data's value (config admin)
            || "true".equals(this.getRequest().getParameter("isNodeDataValue")) //$NON-NLS-1$ //$NON-NLS-2$
            // value to save is a node data's type (config admin)
            || "true".equals(this.getRequest().getParameter("isNodeDataType"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return COMMAND_SAVE_VALUE;
        }

        return COMMAND_SHOW_TREE;
    }

    /**
     * TODO: this is a temporary solution
     */
    protected Context getCommandContext(String commandName) {
        Context context = MgnlContext.getInstance();

        // set general parameters (repository, path, ..)
        context.put(Context.ATTRIBUTE_REPOSITORY, this.getRepository());
        if (this.pathSelected != null) {
            // pathSelected is null in case of delete operation, it should be the responsibility of the caller
            // to set the context attributes properly
            context.put(Context.ATTRIBUTE_PATH, this.pathSelected);
        }

        if (commandName.equals("activate")) {
            context.put(BaseActivationCommand.ATTRIBUTE_SYNDICATOR, getActivationSyndicator(this.pathSelected));
        }

        return context;
    }

    /**
     * Allow default catalogue
     */
    protected Command findCommand(String commandName) {
        Command cmd = super.findCommand(commandName);
        if (cmd == null) {
            cmd = CommandsManager.getInstance().getCommand(CommandsManager.DEFAULT_CATALOG, commandName);
        }
        return cmd;
    }

    /**
     * Show the tree after execution of a command
     */
    protected String getViewNameAfterExecution(String commandName, Context ctx) {
        return VIEW_TREE;
    }

    /**
     * Show the tree
     */
    public String show() {
        return VIEW_TREE;
    }

    /**
     * Create a new node and show the tree
     * @return newly created content node
     */
    public String createNode() {
        String createItemType = Tree.ITEM_TYPE_NODEDATA;
        if (this.getRequest().getParameter("createItemType") != null) { //$NON-NLS-1$
            createItemType = this.getRequest().getParameter("createItemType"); //$NON-NLS-1$
        }

        getTree().setPath(path);
        synchronized (ExclusiveWrite.getInstance()) {
            getTree().createNode(createItemType);
        }
        return VIEW_TREE;
    }

    /**
     * Copy a node
     */
    public String copy() {
        try {
            synchronized (ExclusiveWrite.getInstance()) {
                copyOrMove(Tree.ACTION_COPY);
            }
        }
        catch (Exception e) {
            log.error("can't copy", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.copy") + " " + AlertUtil.getExceptionMessage(e));
        }
        return VIEW_COPY_MOVE;
    }

    /**
     * Move a node
     */
    public String move() {
        try {
            synchronized (ExclusiveWrite.getInstance()) {
                copyOrMove(Tree.ACTION_MOVE);
            }
        }
        catch (Exception e) {
            log.error("can't move", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.move") + " " + AlertUtil.getExceptionMessage(e));
        }
        return VIEW_COPY_MOVE;

    }

    /**
     * @param action
     * @throws RepositoryException
     * @throws ExchangeException
     */
    private void copyOrMove(int action) throws ExchangeException, RepositoryException {
        String pathClipboard = this.getRequest().getParameter("pathClipboard"); //$NON-NLS-1$
        int pasteType = Integer.parseInt(this.getRequest().getParameter("pasteType")); //$NON-NLS-1$
        newPath = pasteNode(pathClipboard, pathSelected, pasteType, action);

        if (pasteType == Tree.PASTETYPE_SUB) {
            pathOpen = pathSelected;
        }
        else {
            // open parent path of destination path
            pathOpen = pathSelected.substring(0, pathSelected.lastIndexOf("/")); //$NON-NLS-1$
        }

        pathSelected = null;
    }

    public void deleteNode(String parentPath, String label) throws ExchangeException, RepositoryException {
        Content parentNode = getHierarchyManager().getContent(parentPath);
        String path;
        if (!parentPath.equals("/")) { //$NON-NLS-1$
            path = parentPath + "/" + label; //$NON-NLS-1$
        }
        else {
            path = "/" + label; //$NON-NLS-1$
        }
        this.deactivateNode(path);
        parentNode.delete(label);
        parentNode.save();
    }

    public void deleteNode(String path) throws Exception {
        String parentPath = StringUtils.substringBeforeLast(path, "/"); //$NON-NLS-1$
        String label = StringUtils.substringAfterLast(path, "/"); //$NON-NLS-1$
        deleteNode(parentPath, label);
    }

    public String delete() {
        String deleteNode = this.getRequest().getParameter("deleteNode"); //$NON-NLS-1$
        try {
            synchronized (ExclusiveWrite.getInstance()) {
                deleteNode(path, deleteNode);
            }
        }
        catch (Exception e) {
            log.error("can't delete", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.delete") + " " + AlertUtil.getExceptionMessage(e));
        }
        return VIEW_TREE;
    }

    /**
     * Create the <code>Syndicator</code> to activate the specified path. method implementation will make sure that
     * proper node collection Rule and Sysdicator is used
     * @param path node path to be activated
     * @return the <code>Syndicator</code> used to activate
     */
    public Syndicator getActivationSyndicator(String path) {
        // use command configuration
        return null;
    }

    /**
     * Execute the deactivation command
     * @param path
     * @throws ExchangeException
     * @throws RepositoryException
     */
    public void deactivateNode(String path) throws ExchangeException, RepositoryException {
        if (MgnlContext.getHierarchyManager(this.getRepository()).isNodeData(path)) {
            return;
        }
        CommandsManager cm = CommandsManager.getInstance();

        Command cmd = cm.getCommand(this.getName(), "deactivate");
        if (cmd == null) {
            cmd = cm.getCommand(CommandsManager.DEFAULT_CATALOG, "deactivate");
        }

        if (cmd == null) {
            log.error("deactivate command not found, deactivation will not be performed");
            return;
        }

        Context ctx = this.getCommandContext("deactivate");
        // override/set path for deactivation
        // Path is set to "/" if this is called via delete command and not directly through the tree handler
        ctx.setAttribute(Context.ATTRIBUTE_PATH, path, Context.LOCAL_SCOPE);
        try {
            cmd.execute(ctx);
        }
        catch (Exception e) {
            throw new ExchangeException(e);
        }

    }

    public Content copyMoveNode(String source, String destination, boolean move) throws ExchangeException,
        RepositoryException {
        // todo: ??? generic -> RequestInterceptor.java
        if (getHierarchyManager().isExist(destination)) {
            String parentPath = StringUtils.substringBeforeLast(destination, "/"); //$NON-NLS-1$
            String label = StringUtils.substringAfterLast(destination, "/"); //$NON-NLS-1$
            label = Path.getUniqueLabel(getHierarchyManager(), parentPath, label);
            destination = parentPath + "/" + label; //$NON-NLS-1$
        }
        if (move) {
            if (destination.indexOf(source + "/") == 0) { //$NON-NLS-1$
                // todo: disable this possibility in javascript
                // move source into destinatin not possible
                return null;
            }
            this.deactivateNode(source);
            try {
                getHierarchyManager().moveTo(source, destination);
            }
            catch (Exception e) {
                // try to move below node data
                return null;
            }
        }
        else {
            // copy
            getHierarchyManager().copyTo(source, destination);
        }
        Content newContent = getHierarchyManager().getContent(destination);
        try {
            newContent.updateMetaData();
            newContent.getMetaData().setUnActivated();
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        newContent.save();
        return newContent;
    }

    public void moveNode(String source, String destination) throws ExchangeException, RepositoryException {
        this.copyMoveNode(source, destination, true);
    }

    public void copyNode(String source, String destination) throws ExchangeException, RepositoryException {
        this.copyMoveNode(source, destination, false);
    }

    public String renameNode(String newLabel) throws AccessDeniedException, ExchangeException, PathNotFoundException,
        RepositoryException {
        String returnValue;
        String parentPath = StringUtils.substringBeforeLast(this.getPath(), "/"); //$NON-NLS-1$
        newLabel = Path.getValidatedLabel(newLabel);

        // don't rename if it uses the same name as the current
        if (this.getPath().endsWith("/" + newLabel)) {
            return newLabel;
        }

        String dest = parentPath + "/" + newLabel; //$NON-NLS-1$
        if (getHierarchyManager().isExist(dest)) {
            newLabel = Path.getUniqueLabel(getHierarchyManager(), parentPath, newLabel);
            dest = parentPath + "/" + newLabel; //$NON-NLS-1$
        }
        this.deactivateNode(this.getPath());

        if (log.isInfoEnabled()) {
            log.info("Moving node from " + this.getPath() + " to " + dest); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (getHierarchyManager().isNodeData(this.getPath())) {
            Content parentPage = getHierarchyManager().getContent(parentPath);
            NodeData newNodeData = parentPage.createNodeData(newLabel);
            NodeData existingNodeData = getHierarchyManager().getNodeData(this.getPath());
            newNodeData.setValue(existingNodeData.getString());
            existingNodeData.delete();
            dest = parentPath;
        }
        else {
            // we can't rename a node. we must move
            // we must place the node at the same position
            Content current = getHierarchyManager().getContent(this.getPath());
            Content parent = current.getParent();
            String placedBefore = null;
            for (Iterator iter = parent.getChildren(current.getNodeTypeName()).iterator(); iter.hasNext();) {
                Content child = (Content) iter.next();
                if (child.getHandle().equals(this.getPath())) {
                    if (iter.hasNext()) {
                        child = (Content) iter.next();
                        placedBefore = child.getName();
                    }
                }
            }

            getHierarchyManager().moveTo(this.getPath(), dest);

            // now set at the same place as before
            if (placedBefore != null) {
                parent.orderBefore(newLabel, placedBefore);
                parent.save();
            }
        }

        Content newPage = getHierarchyManager().getContent(dest);
        returnValue = newLabel;
        newPage.updateMetaData();
        newPage.save();

        return returnValue;
    }

    /**
     * Saves a value edited directly inside the tree. This can also be a lable
     * @return name of the view
     */
    public String saveValue() {
        String saveName = this.getRequest().getParameter("saveName"); //$NON-NLS-1$
        Tree tree = getTree();

        // value to save is a node data's value (config admin)
        boolean isNodeDataValue = "true".equals(this.getRequest().getParameter("isNodeDataValue")); //$NON-NLS-1$ //$NON-NLS-2$

        // value to save is a node data's type (config admin)
        boolean isNodeDataType = "true".equals(this.getRequest().getParameter("isNodeDataType")); //$NON-NLS-1$ //$NON-NLS-2$

        String value = StringUtils.defaultString(this.getRequest().getParameter("saveValue")); //$NON-NLS-1$
        displayValue = StringUtils.EMPTY;
        // value to save is a content's meta information
        boolean isMeta = "true".equals(this.getRequest().getParameter("isMeta")); //$NON-NLS-1$ //$NON-NLS-2$
        // value to save is a label (name of page, content node or node data)
        boolean isLabel = "true".equals(this.getRequest().getParameter("isLabel")); //$NON-NLS-1$ //$NON-NLS-2$

        if (isNodeDataValue || isNodeDataType) {
            tree.setPath(StringUtils.substringBeforeLast(path, "/")); //$NON-NLS-1$
            saveName = StringUtils.substringAfterLast(path, "/"); //$NON-NLS-1$
        }
        else {
            // "/modules/templating/Templates/x"
            tree.setPath(path);
        }

        if (isLabel) {
            displayValue = rename(value);
        }
        else if (isNodeDataType) {
            int type = Integer.valueOf(value).intValue();
            synchronized (ExclusiveWrite.getInstance()) {
                displayValue = tree.saveNodeDataType(saveName, type);
            }
        }
        else {
            synchronized (ExclusiveWrite.getInstance()) {
                displayValue = tree.saveNodeData(saveName, value, isMeta);
            }
        }

        // if there was a displayValue passed show it instead of the written value
        displayValue = StringUtils.defaultString(this.getRequest().getParameter("displayValue"), value); //$NON-NLS-1$

        return VIEW_VALUE;
    }

    /**
     * Called during a renaming of a node. First is the action saveValue called
     * @param value the new name
     * @return return the new name (can change if there were not allowed characters passed)
     */
    protected String rename(String value) {
        try {
            synchronized (ExclusiveWrite.getInstance()) {
                return renameNode(value);
            }
        }
        catch (Exception e) {
            log.error("can't rename", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.rename") + " " + AlertUtil.getExceptionMessage(e));
        }
        return StringUtils.EMPTY;
    }

    public String pasteNode(String pathOrigin, String pathSelected, int pasteType, int action)
        throws ExchangeException, RepositoryException {
        boolean move = false;
        if (action == Tree.ACTION_MOVE) {
            move = true;
        }
        String label = StringUtils.substringAfterLast(pathOrigin, "/"); //$NON-NLS-1$
        String slash = "/"; //$NON-NLS-1$
        if (pathSelected.equals("/")) { //$NON-NLS-1$
            slash = StringUtils.EMPTY;
        }
        String destination = pathSelected + slash + label;
        if (pasteType == Tree.PASTETYPE_SUB && action != Tree.ACTION_COPY && destination.equals(pathOrigin)) {
            // drag node to parent node: move to last position
            pasteType = Tree.PASTETYPE_LAST;
        }
        if (pasteType == Tree.PASTETYPE_SUB) {
            destination = pathSelected + slash + label;
            Content touchedContent = this.copyMoveNode(pathOrigin, destination, move);
            if (touchedContent == null) {
                return StringUtils.EMPTY;
            }
            return touchedContent.getHandle();

        }
        else if (pasteType == Tree.PASTETYPE_LAST) {
            // LAST only available for sorting inside the same directory
            try {
                Content touchedContent = getHierarchyManager().getContent(pathOrigin);
                return touchedContent.getHandle();
            }
            catch (RepositoryException re) {
                return StringUtils.EMPTY;
            }
        }
        else {
            try {
                // PASTETYPE_ABOVE | PASTETYPE_BELOW
                String nameSelected = StringUtils.substringAfterLast(pathSelected, "/"); //$NON-NLS-1$
                String nameOrigin = StringUtils.substringAfterLast(pathOrigin, "/"); //$NON-NLS-1$
                Content tomove = getHierarchyManager().getContent(pathOrigin);
                Content selected = getHierarchyManager().getContent(pathSelected);
                // ordering inside a node?
                // do not check the uuid since this is not working on the root node !!
                if (tomove.getParent().getHandle().equals(selected.getParent().getHandle())) {
                    // if move just set the new ordering
                    if (move) {
                        tomove.getParent().orderBefore(nameOrigin, nameSelected);
                        // deactivate
                        this.deactivateNode(pathOrigin);
                        tomove.getParent().save();
                    }
                    else {
                        Content newNode = this.copyMoveNode(pathOrigin, pathOrigin, move);
                        tomove.getParent().orderBefore(newNode.getName(), nameSelected);
                        tomove.getParent().save();
                    }

                }
                else {
                    String newOrigin = selected.getParent().getHandle() + "/" + nameOrigin;
                    // clean the newOrigin if we move/copy to the root
                    if (newOrigin.startsWith("//")) {
                        newOrigin = StringUtils.removeStart(newOrigin, "/");
                    }
                    Content newNode;
                    if (move) {
                        getHierarchyManager().moveTo(pathOrigin, newOrigin);
                        newNode = getHierarchyManager().getContent(newOrigin);
                    }
                    else {
                        newNode = this.copyMoveNode(pathOrigin, newOrigin, move);
                    }

                    if (pasteType == Tree.PASTETYPE_ABOVE) {
                        newNode.getParent().orderBefore(newNode.getName(), nameSelected);
                        newNode.getParent().save();
                    }
                }
                return tomove.getHandle();
            }
            catch (RepositoryException re) {
                re.printStackTrace();
                log.error("Problem when pasting node", re);
                return StringUtils.EMPTY;
            }
        }
    }

    /**
     * Render the tree depending on the view name.
     * @param view
     * @throws IOException
     */
    public void renderHtml(String view) throws IOException {
        StringBuffer html = new StringBuffer(500);

        // an alert can happen if there were deactivation problems during a renaming
        if (AlertUtil.isMessageSet()) {
            html.append("<input type=\"hidden\" id=\"mgnlMessage\" value=\"");
            html.append(AlertUtil.getMessage());
            html.append("\" />");
        }

        if (VIEW_TREE.equals(view) || VIEW_CREATE.equals(view) || VIEW_COPY_MOVE.equals(view)) {
            // if there was a node created we have not to set the pathes
            if (!view.equals(VIEW_CREATE)) {
                getTree().setPathOpen(pathOpen);
                getTree().setPathSelected(pathSelected);
            }

            // after moving or copying
            if (view.equals(VIEW_COPY_MOVE)) {
                // pass new path to tree.js for selecting the newly created node
                // NOTE: tree.js checks for this pattern; adapt it there, if any changes are made here
                html.append("<input type=\"hidden\" id=\"mgnlSelectNode\" value=\"");
                html.append(newPath);
                html.append("\" />");
            }

            renderTree(html);
        }

        // after saving a column value
        else if (view.equals(VIEW_VALUE)) {
            html.append(displayValue);
        }
        this.getResponse().getWriter().print(html);
    }

    /**
     * Create the html for the tree. Calls tree.getHtml after calling prepareTree.
     * @param html
     */
    protected void renderTree(StringBuffer html) {
        String mode = StringUtils.defaultString(this.getRequest().getParameter("treeMode")); //$NON-NLS-1$
        boolean snippetMode = mode.equals("snippet"); //$NON-NLS-1$
        Tree tree = getTree();

        tree.setJavascriptTree("mgnlTreeControl"); //$NON-NLS-1$
        tree.setBrowseMode(this.isBrowseMode());

        if (!snippetMode) {
            html.append("<html><head>"); //$NON-NLS-1$
            html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); //$NON-NLS-1$
            renderHeaderIncludes(html);
            html.append("<title>Magnolia</title>"); //$NON-NLS-1$
            html.append("<body class=\"mgnlBgDark\">");
        }

        tree.setSnippetMode(snippetMode);
        tree.setHeight(50);

        tree.setPath(path);

        this.getConfiguration().prepareTree(tree, this.isBrowseMode(), this.getRequest());
        this.getConfiguration().prepareContextMenu(tree, this.isBrowseMode(), this.getRequest());
        this.getConfiguration().prepareFunctionBar(tree, this.isBrowseMode(), this.getRequest());

        if (!snippetMode) {
            html.append("<div id=\"");
            html.append(tree.getJavascriptTree());
            html.append("_DivSuper\" style=\"display:block;\">");
        }
        html.append(tree.getHtml());
        if (!snippetMode) {
            html.append("</div>"); //$NON-NLS-1$
        }

        if (!snippetMode) {
            html.append("</body></html>"); //$NON-NLS-1$
        }
    }

    /**
     * @param html
     */
    protected void renderHeaderIncludes(StringBuffer html) {
        html.append(new Sources(this.getRequest().getContextPath()).getHtmlJs());
        html.append(new Sources(this.getRequest().getContextPath()).getHtmlCss());
    }

    protected void setTree(Tree tree) {
        this.tree = tree;
    }

    protected Tree getTree() {
        if (tree == null) {
            tree = (Tree) FactoryUtil.newInstanceWithoutDiscovery(this.getTreeClass(), new Object[]{
                getName(),
                getRepository()});

            if (tree == null) {
                // try to get the Tree with the deprecated constructor !
                log.warn("The {} Tree class is probably using the deprecated (String name, String repository, HttpServletRequest request) constructor. Please use the (String name, String repository) constructor instead.", getTreeClass());
                tree = (Tree) FactoryUtil.newInstanceWithoutDiscovery(this.getTreeClass(), new Object[]{
                getName(),
                getRepository(),
                getRequest()});
            }
        }
        return tree;
    }

    protected String getPath() {
        return path;
    }

    protected String getPathSelected() {
        return pathSelected;
    }

    /**
     * @return Returns the browseMode.
     */
    public boolean isBrowseMode() {
        return browseMode;
    }

    /**
     * @param browseMode The browseMode to set.
     */
    public void setBrowseMode(boolean browseMode) {
        this.browseMode = browseMode;
    }

    /**
     * @return Returns the configuration.
     */
    public AdminTreeConfiguration getConfiguration() {
        if (this.configuration == null) {
            setConfiguration((AdminTreeConfiguration) FactoryUtil.newInstanceWithoutDiscovery(this
                .getConfigurationClass(), new Object[]{}));
        }
        return this.configuration;
    }

    /**
     * @param configuration The configuration to set.
     *
     * @deprecated don't set this manually !
     */
    public void setConfiguration(AdminTreeConfiguration configuration) {
        this.configuration = configuration;
        final Messages messages = MessagesUtil.chainWithDefault(getI18nBasename());
        configuration.setMessages(messages);
    }

    public String getConfigurationClass() {
        return configurationClass;
    }

    public void setConfigurationClass(String configClass) {
        this.configurationClass = configClass;
    }

    public String getTreeClass() {
        return treeClass;
    }

    public void setTreeClass(String treeClass) {
        this.treeClass = treeClass;
    }

    public String getI18nBasename() {
        return i18nBasename;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }
}
