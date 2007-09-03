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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.gui.misc.CssConstants;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class TreeColumn extends ControlImpl {

    public static final String EDIT_CSSCLASS_EDIT = CssConstants.CSSCLASS_EDIT;

    public static final String EDIT_CSSCLASS_SELECT = CssConstants.CSSCLASS_SELECT;

    public static final String EDIT_NAMEADDITION = "_EditNodeData"; //$NON-NLS-1$

    public static final String EDIT_JSSAVE = ".saveNodeData(this.value);"; //$NON-NLS-1$

    public static final String EMPTY = "-"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(TreeColumn.class);

    boolean isMeta;

    boolean isLabel;

    boolean isIcons;

    boolean iconsActivation;

    boolean iconsPermission;

    boolean isNodeDataValue;

    boolean isNodeDataType;

    String dateFormat;

    String title = StringUtils.EMPTY;

    boolean permissionWrite;

    String htmlEdit = StringUtils.EMPTY;

    TreeColumnHtmlRenderer htmlRenderer;

    private String javascriptTree = StringUtils.EMPTY;

    private int width = 1;

    // if EMPTY changes, it has to be updated also in tree.js; note: if you use html tags, use upper-case letters (and
    // test it well ;)
    private Map keyValue = new Hashtable();

    public TreeColumn() {
        this.setCssClass("mgnlTreeText"); //$NON-NLS-1$
        // default delegate
        this.setHtmlRenderer(new TreeColumnHtmlRendererImpl());

    }

    /**
     * Constructor.
     * @param javascriptTree name of the js variable
     * @param request http request, needed for context path
     * @deprecated use the empty constuctor
     */
    public TreeColumn(String javascriptTree, HttpServletRequest request) {
        this();
        this.setJavascriptTree(javascriptTree);
        this.setRequest(request);
    }

    public void setIsMeta(boolean b) {
        this.isMeta = b;
    }

    public boolean getIsMeta() {
        return this.isMeta;
    }

    public void setIsIcons(boolean b) {
        this.isIcons = b;
    }

    public boolean getIsIcons() {
        return this.isIcons;
    }

    public void setIconsActivation(boolean b) {
        this.iconsActivation = b;
    }

    public boolean getIconsActivation() {
        return this.iconsActivation;
    }

    public void setIconsPermission(boolean b) {
        this.iconsPermission = b;
    }

    public boolean getIconsPermission() {
        return this.iconsPermission;
    }

    public void setIsLabel(boolean b) {
        this.isLabel = b;
    }

    public boolean getIsLabel() {
        return this.isLabel;
    }

    public void setIsNodeDataValue(boolean b) {
        this.isNodeDataValue = b;
    }

    public boolean getIsNodeDataValue() {
        return this.isNodeDataValue;
    }

    public void setIsNodeDataType(boolean b) {
        this.isNodeDataType = b;
    }

    public boolean getIsNodeDataType() {
        return this.isNodeDataType;
    }

    public void setDateFormat(String s) {
        this.dateFormat = s;
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public String getTitle() {
        return this.title;
    }

    /**
     * <p>
     * set the name relative width of the column; default is 1
     * </p>
     */
    public void setWidth(int i) {
        this.width = i;
    }

    public int getWidth() {
        return this.width;
    }

    public void setPermissionWrite(boolean b) {
        this.permissionWrite = b;
    }

    public boolean getPermissionWrite() {
        return this.permissionWrite;
    }

    /**
     * <p>
     * set the name of the javascript tree object
     * </p>
     * @param variableName
     */
    public void setJavascriptTree(String variableName) {
        this.javascriptTree = variableName;
    }

    public String getJavascriptTree() {
        return this.javascriptTree;
    }

    public void setKeyValue(Map h) {
        this.keyValue = h;
    }

    public Map getKeyValue() {
        return this.keyValue;
    }

    public String getKeyValue(String key) {
        return (String) this.getKeyValue().get(key);
    }

    public void addKeyValue(String key, String value) {
        this.getKeyValue().put(key, value);
    }

    public void setHtmlEdit(String s) {
        this.htmlEdit = s;
    }

    public String getHtmlEdit() {
        return this.htmlEdit;
    }

    public void setHtmlEdit() {
        Edit edit = new Edit();
        edit.setName(this.getJavascriptTree() + EDIT_NAMEADDITION);
        edit.setSaveInfo(false);
        edit.setCssClass(EDIT_CSSCLASS_EDIT);
        edit.setEvent("onkeydown", this.getJavascriptTree() + ".editNodeDataKeydown(event,this);"); //$NON-NLS-1$ //$NON-NLS-2$
        edit.setEvent("onblur", this.getJavascriptTree() + EDIT_JSSAVE); //$NON-NLS-1$
        // edit.setCssStyles("width","100%");
        this.setHtmlEdit(edit.getHtml());
    }

    public String getHtml() {
        String html = null;
        try {
            html = getHtmlRenderer().renderHtml(this, this.getWebsiteNode());
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        // "" not clickable!
        if (StringUtils.isEmpty(html)) {
            html = TreeColumn.EMPTY;
        }
        return html;
    }

    /**
     * @return Returns the htmlRenderer.
     */
    public TreeColumnHtmlRenderer getHtmlRenderer() {
        return htmlRenderer;
    }

    /**
     * @param htmlRenderer Set the delecate Object which will render the html column for each row
     */
    public void setHtmlRenderer(TreeColumnHtmlRenderer htmlRenderer) {
        this.htmlRenderer = htmlRenderer;
    }

    public static TreeColumn createColumn(Tree tree, String title) {
        TreeColumn treeColumn = new TreeColumn();
        treeColumn.setJavascriptTree(tree.getJavascriptTree());
        treeColumn.setTitle(title);
        treeColumn.setWidth(8);
        return treeColumn;
    }

    public static TreeColumn createColumn(Tree tree, String title, TreeColumnHtmlRenderer htmlRenderer) {
        TreeColumn treeColumn = createColumn(tree, title);
        if (htmlRenderer != null) {
        	treeColumn.setHtmlRenderer(htmlRenderer);
        }
        return treeColumn;
    }
    
    public static TreeColumn createLabelColumn(Tree tree, String title, boolean editable) {
        TreeColumn treeColumn = createColumn(tree, title);
        treeColumn.setIsLabel(true);
        if(editable){
            treeColumn.setHtmlEdit();
        }
        return treeColumn;
    }

    public static TreeColumn createNodeDataColumn(Tree tree, String title, String nodeDataName, boolean editable) {
        TreeColumn treeColumn = createColumn(tree, title);
        treeColumn.setName(nodeDataName);
        if(editable){
            treeColumn.setHtmlEdit();
        }
        return treeColumn;
    }

    public static TreeColumn createNodeDataColumn(Tree tree, String title, String nodeDataName, String dateFormat) {
        TreeColumn treeColumn = createColumn(tree, title);
        treeColumn.setName(nodeDataName);
        treeColumn.setDateFormat(dateFormat);
        return treeColumn;
    }

    public static TreeColumn createMetaDataColumn(Tree tree, String title, String name, String dateFormat) {
        TreeColumn treeColumn = createColumn(tree, title);
        treeColumn.setIsMeta(true);
        treeColumn.setName(name);
        if (dateFormat != null) {
        	treeColumn.setDateFormat(dateFormat);
        }
        return treeColumn;
    }

    public static TreeColumn createIconColumn(Tree tree, String title, TreeColumnHtmlRenderer htmlRenderer) {
        TreeColumn treeColumn = createColumn(tree, title, htmlRenderer);
        treeColumn.setIsIcons(true);
        treeColumn.setWidth(1);
        return treeColumn;
    }

	public static TreeColumn createActivationColumn(Tree tree) {
        TreeColumn columnActivation = TreeColumn.createIconColumn(tree, "", null);
        columnActivation.setIconsActivation(true);
        return columnActivation;
	}

}
