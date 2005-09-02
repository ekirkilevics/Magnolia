/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */

package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.servlets.MVCServletHandlerImpl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * this class wrapes the tree control. The AdminInterfaceServlet instantiates a subclass. To build your own tree you
 * have to override the prepareTree() method
 * @author philipp
 * @author Fabrizio Giustina
 */

public abstract class AdminTreeMVCHandler extends MVCServletHandlerImpl {

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
     * name of the tree (not the repository)
     */
    private Tree tree;

    private String path;

    private String pathOpen;

    private String pathSelected;

    /**
     * Used to pass the saved value to the view
     */
    private String displayValue;

    private String newPath;

    /**
     * Used to display the same tree in the linkbrowser
     */
    private boolean browseMode;

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
        path = request.getParameter("path"); //$NON-NLS-1$
        if (StringUtils.isEmpty(path)) {
            path = "/"; //$NON-NLS-1$
        }

        pathOpen = request.getParameter("pathOpen"); //$NON-NLS-1$
        pathSelected = request.getParameter("pathSelected"); //$NON-NLS-1$

        this.setBrowseMode(StringUtils.equals(request.getParameter("browseMode"), "true"));
    }

    /**
     * Depending on the request it is generating a logical command name
     * @return name of the command
     */
    public String getCommand() {

        // actions returned from the tree (pased through treeAction)
        if (StringUtils.isNotEmpty(request.getParameter("treeAction"))) { //$NON-NLS-1$
            int treeAction = Integer.parseInt(request.getParameter("treeAction")); //$NON-NLS-1$

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

            return request.getParameter("treeAction"); //$NON-NLS-1$
        }

        // other actions depending other informations
        if (request.getParameter("createItemType") != null) { //$NON-NLS-1$
            return COMMAND_CREATE_NODE;
        }

        if (request.getParameter("deleteNode") != null) { //$NON-NLS-1$
            return COMMAND_DELETE_NODE;
        }

        // editet any value directly in the columns?
        if (request.getParameter("saveName") != null //$NON-NLS-1$
            // value to save is a node data's value (config admin)
            || "true".equals(request.getParameter("isNodeDataValue")) //$NON-NLS-1$ //$NON-NLS-2$
            // value to save is a node data's type (config admin)
            || "true".equals(request.getParameter("isNodeDataType"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return COMMAND_SAVE_VALUE;
        }
        return COMMAND_SHOW_TREE;
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
        String createItemType = ItemType.NT_NODEDATA;
        if (request.getParameter("createItemType") != null) { //$NON-NLS-1$
            createItemType = request.getParameter("createItemType"); //$NON-NLS-1$
        }

        getTree().setPath(path);
        getTree().createNode(createItemType);
        return VIEW_TREE;
    }

    /**
     * Copy a node
     */
    public String copy() {
        return copyOrMove(Tree.ACTION_COPY);
    }

    /**
     * Move a node
     */
    public String move() {
        return copyOrMove(Tree.ACTION_MOVE);
    }

    /**
     * @param action
     * @return
     */
    private String copyOrMove(int action) {
        String pathClipboard = request.getParameter("pathClipboard"); //$NON-NLS-1$
        int pasteType = Integer.parseInt(request.getParameter("pasteType")); //$NON-NLS-1$

        newPath = getTree().pasteNode(pathClipboard, pathSelected, pasteType, action);
        if (pasteType == Tree.PASTETYPE_SUB) {
            pathOpen = pathSelected;
        }
        else {
            // open parent path of destination path
            pathOpen = pathSelected.substring(0, pathSelected.lastIndexOf("/")); //$NON-NLS-1$
        }

        pathSelected = null;
        return VIEW_COPY_MOVE;
    }

    public String delete() {
        String deleteNode = request.getParameter("deleteNode"); //$NON-NLS-1$
        getTree().deleteNode(path, deleteNode);
        return VIEW_TREE;
    }

    public String activate() {
        boolean recursive = (request.getParameter("recursive") != null); //$NON-NLS-1$
        // by default every CONTENTNODE under the specified CONTENT node is activated
        getTree().activateNode(pathSelected, recursive, true);
        return VIEW_TREE;
    }

    public String deactivate() {
        getTree().deActivateNode(pathSelected);
        return VIEW_TREE;
    }

    /**
     * Saves a value edited directly inside the tree. This can also be a lable
     * @return name of the view
     */
    public String saveValue() {
        String saveName = request.getParameter("saveName"); //$NON-NLS-1$
        Tree tree = getTree();

        // value to save is a node data's value (config admin)
        boolean isNodeDataValue = "true".equals(request.getParameter("isNodeDataValue")); //$NON-NLS-1$ //$NON-NLS-2$

        // value to save is a node data's type (config admin)
        boolean isNodeDataType = "true".equals(request.getParameter("isNodeDataType")); //$NON-NLS-1$ //$NON-NLS-2$

        String value = StringUtils.defaultString(request.getParameter("saveValue")); //$NON-NLS-1$
        displayValue = StringUtils.EMPTY;
        // value to save is a content's meta information
        boolean isMeta = "true".equals(request.getParameter("isMeta")); //$NON-NLS-1$ //$NON-NLS-2$
        // value to save is a label (name of page, content node or node data)
        boolean isLabel = "true".equals(request.getParameter("isLabel")); //$NON-NLS-1$ //$NON-NLS-2$

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
            displayValue = tree.saveNodeDataType(saveName, type);
        }
        else {
            displayValue = tree.saveNodeData(saveName, value, isMeta);
        }

        // if there was a displayValue passed show it instead of the written value
        displayValue = StringUtils.defaultString(request.getParameter("displayValue"), value); //$NON-NLS-1$

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
        return getTree().renameNode(value);
    }

    /**
     * Render the tree depending on the view name.
     * @param view
     * @return
     * @throws IOException
     */
    public void renderHtml(String view) throws IOException {
        StringBuffer html = new StringBuffer(500);

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
        response.getWriter().print(html);
    }

    /**
     * Override this method to configure the tree control (define the columns, ...)
     * @param tree
     * @param request
     */
    protected abstract void prepareTree(Tree tree, HttpServletRequest request);

    /**
     * Prepare the context menu of the tree. This is called during renderTree
     * @param tree
     * @param request
     */
    protected abstract void prepareContextMenu(Tree tree, HttpServletRequest request);

    /**
     * Create the html for the tree. Calls tree.getHtml after calling prepareTree.
     * @param html
     */
    protected void renderTree(StringBuffer html) {
        String mode = StringUtils.defaultString(request.getParameter("treeMode")); //$NON-NLS-1$
        boolean snippetMode = mode.equals("snippet"); //$NON-NLS-1$
        Tree tree = getTree();

        tree.setJavascriptTree("mgnlTreeControl"); //$NON-NLS-1$
        tree.setBrowseMode(this.isBrowseMode());

        if (!snippetMode) {
            html.append("<html><head>"); //$NON-NLS-1$
            html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); //$NON-NLS-1$
            renderHeaderIncludes(html);
            html.append("<title>Magnolia</title>"); //$NON-NLS-1$
            html.append("</head>"); //$NON-NLS-1$
            html.append("<body class=\"mgnlBgDark\" onload=\"" + tree.getJavascriptTree() + ".resizeOnload();\" >"); //$NON-NLS-1$ //$NON-NLS-2$
            html.append(Spacer.getHtml(20, 20));
        }

        tree.setSnippetMode(snippetMode);
        tree.setHeight(50);

        tree.setPath(path);

        prepareTree(tree, request);
        prepareContextMenu(getTree(), request);

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
        html.append(new Sources(request.getContextPath()).getHtmlJs());
        html.append(new Sources(request.getContextPath()).getHtmlCss());
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

}