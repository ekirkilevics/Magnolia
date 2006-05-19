/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */

package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.beans.runtime.WebContextImpl;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.servlets.CommandBasedMVCServletHandler;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.commands.ContextAttributes;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * this class wrapes the tree control. The AdminInterfaceServlet instantiates a subclass. To build your own tree you
 * have to override the prepareTree() method
 * @author philipp
 * @author Fabrizio Giustina
 */

public abstract class AdminTreeMVCHandler extends CommandBasedMVCServletHandler {

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

    /**
     * Used to display the same tree in the linkbrowser
     */
    protected boolean browseMode;

    /**
     * Override this method if you are not using the same name for the tree and the repository
     * @return name of the repository
     */
    protected String getRepository() {
        return getName();
    }

    public AdminTreeMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        setTree(new Tree(name, getRepository(), request));
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
        // use the current one
        // FIXME: perhaps we should wrapp it instead of useing directly
        Context context = MgnlContext.getInstance();
        
        // set some general parameters
        context.put(ContextAttributes.P_TREE, this.tree);
        context.put(ContextAttributes.P_PATH, this.pathSelected);

        populateContext(context);

        return context;
    }

    /**
     * TODO: this is a temporary solution
     */
    private void populateContext(Context context) {
        // add start date and end date
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);
        Content ct;
        try {
            ct = hm.getContent(this.pathSelected);

            Calendar cd = null;
            String date;

            // get start time
            try {
                cd = ct.getMetaData().getStartTime();
            }
            catch (Exception e) {
                log.warn("cannot get start time for node " + this.pathSelected, e);
            }
            // if (cd == null)
            // cd = Calendar.getInstance();
            SimpleDateFormat sdf = null;
            if (cd != null) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                date = sdf.format(new Date(cd.getTimeInMillis()));
                log.debug("start date = " + date);
                context.put("startDate", date);
            }

            // get end time
            try {
                cd = ct.getMetaData().getEndTime();
            }
            catch (Exception e) {
                log.warn("cannot get end time for node " + this.pathSelected, e);
            }

            if (cd != null) {
                date = sdf.format(new Date(cd.getTimeInMillis()));
                log.debug("end date = " + date);
                context.put("endDate", date);
            }
        }
        catch (Exception e) {
            log.warn("can not get start/end date for path "
                + this.pathSelected
                + ", please use sevlet FlowDef to set start/end date for node.", e);
        }

        String recursive = "false";
        if (this.request.getParameter("recursive") != null) {
            recursive = "true";
        }
        context.put(ContextAttributes.P_RECURSIVE, recursive);
    }

    /**
     * Show the tree
     */
    public String show() {
        return VIEW_TREE;
    }

    /**
     * Create a new node and show the tree
     * @return
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
        newPath = getTree().pasteNode(pathClipboard, pathSelected, pasteType, action);

        if (pasteType == Tree.PASTETYPE_SUB) {
            pathOpen = pathSelected;
        }
        else {
            // open parent path of destination path
            pathOpen = pathSelected.substring(0, pathSelected.lastIndexOf("/")); //$NON-NLS-1$
        }

        pathSelected = null;
    }

    public String delete() {
        String deleteNode = this.getRequest().getParameter("deleteNode"); //$NON-NLS-1$
        try {
            synchronized (ExclusiveWrite.getInstance()) {
                getTree().deleteNode(path, deleteNode);
            }
        }
        catch (Exception e) {
            log.error("can't delete", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.delete") + " " + AlertUtil.getExceptionMessage(e));
        }
        return VIEW_TREE;
    }

    public String activate() {
        boolean recursive = (this.getRequest().getParameter("recursive") != null); //$NON-NLS-1$
        // by default every CONTENTNODE under the specified CONTENT node is activated
        try {
            getTree().activateNode(pathSelected, recursive, true);
        }
        catch (Exception e) {
            log.error("can't activate", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.activate") + " " + AlertUtil.getExceptionMessage(e));
        }
        return VIEW_TREE;
    }

    public String deactivate() {
        try {
            getTree().deActivateNode(pathSelected);
        }
        catch (Exception e) {
            log.error("can't deactivate", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.deactivate") + " " + AlertUtil.getExceptionMessage(e));
        }
        return VIEW_TREE;
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

        // @todo should be handled in a better way but, at the moment, this is better than nothing
        if (path.startsWith("/subscribers/")) { //$NON-NLS-1$
            Subscriber.reload();
        }
        else if (path.startsWith("/server/MIMEMapping")) { //$NON-NLS-1$
            MIMEMapping.reload();
        }

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
                return getTree().renameNode(value);
            }
        }
        catch (Exception e) {
            log.error("can't rename", e);
            AlertUtil.setMessage(MessagesManager.get("tree.error.rename") + " " + AlertUtil.getExceptionMessage(e));
        }
        return StringUtils.EMPTY;
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
            html.append("<input type=\"hidden\" id=\"mgnlMessage\" value=\"" + AlertUtil.getMessage() + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (VIEW_TREE.equals(view) || VIEW_CREATE.equals(view) || VIEW_COPY_MOVE.equals(view)) {
            // if there was a node created we have not to set the pathes
            if (view != VIEW_CREATE) {
                getTree().setPathOpen(pathOpen);
                getTree().setPathSelected(pathSelected);
            }

            // after moving or copying
            if (view == VIEW_COPY_MOVE) {
                // pass new path to tree.js for selecting the newly created node
                // NOTE: tree.js checks for this pattern; adapt it there, if any changes are made here
                html.append("<input type=\"hidden\" id=\"mgnlSelectNode\" value=\"" + newPath + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
            }

            renderTree(html);
        }

        // after saving a column value
        else if (view == VIEW_VALUE) {
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
            html.append("<script>window.onresize = mgnlTreeResize;</script>"); //$NON-NLS-1$
            html.append("</head>"); //$NON-NLS-1$
            html.append("<body class=\"mgnlBgDark\" onload=\"" + tree.getJavascriptTree() + ".resizeOnload();\" >"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        tree.setSnippetMode(snippetMode);
        tree.setHeight(50);

        tree.setPath(path);

        this.getConfiguration().prepareTree(tree, this.isBrowseMode(), this.getRequest());
        this.getConfiguration().prepareContextMenu(tree, this.isBrowseMode(), this.getRequest());
        this.getConfiguration().prepareFunctionBar(tree, this.isBrowseMode(), this.getRequest());

        if (!snippetMode) {
            html.append("<div id=\"" + tree.getJavascriptTree() + "_DivSuper\" style=\"display:block;\">"); //$NON-NLS-1$ //$NON-NLS-2$
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
        return this.configuration;
    }

    /**
     * @param configuration The configuration to set.
     */
    public void setConfiguration(AdminTreeConfiguration configuration) {
        this.configuration = configuration;
    }

}