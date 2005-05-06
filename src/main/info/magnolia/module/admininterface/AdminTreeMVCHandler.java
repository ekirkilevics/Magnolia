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

import java.io.IOException;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.servlets.MVCServletHandlerImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author philipp this class wrapes the tree control. The AdminInterfaceServlet instantiates a subclass. To build your
 * own tree you have to override the prepareTree() method
 */

abstract public class AdminTreeMVCHandler extends MVCServletHandlerImpl {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(AdminTreeMVCHandler.class);

    /**
     * this are the used actions
     */
    private static final String COMMAND_SHOW_TREE = "show";

    private static final String COMMAND_COPY_NODE = "copy";

    private static final String COMMAND_MOVE_NODE = "move";

    private static final String COMMAND_ACTIVATE = "activate";

    private static final String COMMAND_DEACTIVATE = "deactivate";

    private static final String COMMAND_CREATE_NODE = "createNode";

    private static final String COMMAND_DELETE_NODE = "delete";

    private static final String COMMAND_SAVE_VALUE = "saveValue";

    /**
     * The view names
     */

    private static final String VIEW_TREE = "tree";

    private static final String VIEW_CREATE = "create";

    private static final String VIEW_VALUE = "value";

    private static final String VIEW_NOTHING = "nothing";

    private static final String VIEW_COPY_MOVE = "copymove";

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
     * Override this method if you are not using the same name for the tree and the repository
     * @return name of the repository
     */
    protected String getRepository() {
        return getName();
    }

    public AdminTreeMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        tree = new Tree(getRepository(), request);
        path = request.getParameter("path");
        if (StringUtils.isEmpty(path)) {
            path = "/";
        }

        pathOpen = request.getParameter("pathOpen");
        pathSelected = request.getParameter("pathSelected");

    }

    /**
     * Depending on the request it is generating a logical command name
     * @return name of the command
     */
    public String getCommand() {

        // actions returned from the tree (pased through treeAction)
        if (StringUtils.isNotEmpty(request.getParameter("treeAction"))) {
            int treeAction = Integer.parseInt(request.getParameter("treeAction"));

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

            return request.getParameter("treeAction");
        }

        // other actions depending other informations
        if (request.getParameter("createItemType") != null) {
            return COMMAND_CREATE_NODE;
        }

        if (request.getParameter("deleteNode") != null)
            return COMMAND_DELETE_NODE;

        // editet any value directly in the columns?
        if (request.getParameter("saveName") != null
        // value to save is a node data's value (config admin)
            || "true".equals(request.getParameter("isNodeDataValue"))
            // value to save is a node data's type (config admin)
            || "true".equals(request.getParameter("isNodeDataType"))) {
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
        if (request.getParameter("createItemType") != null) {
            createItemType = request.getParameter("createItemType");
        }

        tree.setPath(path);
        tree.createNode(createItemType);
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
        String pathClipboard = request.getParameter("pathClipboard");
        int pasteType = Integer.parseInt(request.getParameter("pasteType"));

        newPath = tree.pasteNode(pathClipboard, pathSelected, pasteType, action);
        if (pasteType == Tree.PASTETYPE_SUB) {
            pathOpen = pathSelected;
        }
        else {
            // open parent path of destination path
            pathOpen = pathSelected.substring(0, pathSelected.lastIndexOf("/"));
        }

        pathSelected = null;
        return VIEW_COPY_MOVE;
    }

    public String delete() {
        String deleteNode = request.getParameter("deleteNode");
        tree.deleteNode(path, deleteNode);
        return VIEW_TREE;
    }

    public String activate() {
        boolean recursive = (request.getParameter("recursive") != null);
        tree.activateNode(pathSelected, recursive);
        return VIEW_TREE;
    }

    public String deactivate() {
        tree.deActivateNode(pathSelected);
        return VIEW_TREE;
    }

    /**
     * Saves a value edited directly inside the tree. This can also be a lable
     * @return name of the view
     */
    public String saveValue() {
        String saveName = request.getParameter("saveName");

        // value to save is a node data's value (config admin)
        boolean isNodeDataValue = "true".equals(request.getParameter("isNodeDataValue"));

        // value to save is a node data's type (config admin)
        boolean isNodeDataType = "true".equals(request.getParameter("isNodeDataType"));

        String value = StringUtils.defaultString(request.getParameter("saveValue"));
        displayValue = "";
        // value to save is a content's meta information
        boolean isMeta = "true".equals(request.getParameter("isMeta"));
        // value to save is a label (name of page, content node or node data)
        boolean isLabel = "true".equals(request.getParameter("isLabel"));

        if (isNodeDataValue || isNodeDataType) {
            tree.setPath(StringUtils.substringBeforeLast(path, "/"));
            saveName = StringUtils.substringAfterLast(path, "/");
        }
        else {
            // "/modules/templating/Templates/x"
            tree.setPath(path);
        }

        if (isLabel) {
            displayValue = tree.renameNode(value);
        }
        else if (isNodeDataType) {
            int type = Integer.valueOf(value).intValue();
            displayValue = tree.saveNodeDataType(saveName, type);
        }
        else {
            displayValue = tree.saveNodeData(saveName, value, isMeta);
        }

        // if there was a displayValue passed show it instead of the written value
        displayValue = StringUtils.defaultString(request.getParameter("displayValue"), value);

        // @todo should be handled in a better way but, at the moment, this is better than nothing
        if (path.startsWith("/modules/templating/Templates/")) {
            Template.reload();
        }
        else if (path.startsWith("/modules/templating/Paragraphs/")) {
            Paragraph.reload();
        }
        else if (path.startsWith("/subscribers/")) {
            Subscriber.reload();
        }
        else if (path.startsWith("/server/MIMEMapping")) {
            MIMEMapping.reload();
        }

        return VIEW_VALUE;
    }

    /**
     * Render the tree depending on the view name.
     * @param view
     * @return
     * @throws IOException
     */
    public void renderHtml(String view) throws IOException {
        StringBuffer html = new StringBuffer(500);

        if (view == VIEW_TREE || view == VIEW_CREATE || view == VIEW_COPY_MOVE) {
            // if there was a node created we have not to set the pathes
            if (view != VIEW_CREATE) {
                tree.setPathOpen(pathOpen);
                tree.setPathSelected(pathSelected);
            }

            // after moving or copying
            if (view == VIEW_COPY_MOVE) {
                // pass new path to tree.js for selecting the newly created node
                // NOTE: tree.js checks for this pattern; adapt it there, if any changes are made here
                html.append("<input type=\"hidden\" id=\"mgnlSelectNode\" value=\"" + newPath + "\" />");
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
    abstract protected void prepareTree(Tree tree, HttpServletRequest request);

    /**
     * Create the html for the tree. Calls tree.getHtml after calling prepareTree.
     * @param html
     */
    private void renderTree(StringBuffer html) {
        String mode = StringUtils.defaultString(request.getParameter("treeMode"));
        boolean snippetMode = mode.equals("snippet");

        if (!snippetMode) {
            html.append("<html><head>");
            html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
            html.append(new Sources(request.getContextPath()).getHtmlJs());
            html.append(new Sources(request.getContextPath()).getHtmlCss());
            html.append("<title>Magnolia</title>");
            html.append("</head>");
            html.append("<body class=\"mgnlBgDark\" onload=\"mgnlTree.resizeOnload();\" >");
            html.append(Spacer.getHtml(20, 20));
        }
        tree.setJavascriptTree("mgnlTree");
        tree.setSnippetMode(snippetMode);
        tree.setHeight(50);

        tree.setPath(path);

        prepareTree(tree, request);

        if (!snippetMode) {
            html.append("<div id=\"" + tree.getJavascriptTree() + "_DivSuper\" style=\"display:block;\">");
        }
        html.append(tree.getHtml());
        if (!snippetMode) {
            html.append("</div>");
        }

        if (!snippetMode) {
            html.append("</body></html>");
        }
    }
}