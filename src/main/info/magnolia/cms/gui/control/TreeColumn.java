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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.i18n.TemplateMessages;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class TreeColumn extends ControlSuper {

    public static final String EDIT_CSSCLASS_EDIT = CssConstants.CSSCLASS_EDIT;

    public static final String EDIT_CSSCLASS_SELECT = CssConstants.CSSCLASS_SELECT;

    public static final String EDIT_NAMEADDITION = "_EditNodeData";

    public static final String EDIT_JSSAVE = ".saveNodeData(this.value);";

    public static final String EMPTY = "-";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(TreeColumn.class);

    boolean isMeta;

    boolean isLabel;

    boolean isIcons;

    boolean iconsActivation;

    boolean iconsPermission;

    boolean isNodeDataValue;

    boolean isNodeDataType;
    
    String dateFormat;

    String title = "";

    boolean permissionWrite;

    String htmlEdit = "";
    
    TreeColumnHtmlRenderer htmlRenderer;

    private String javascriptTree = "";

    private int width = 1;

    // if EMPTY changes, it has to be updated also in tree.js; note: if you use html tags, use upper-case letters (and
    // test it well ;)
    private Map keyValue = new Hashtable();

    /**
     * Constructor.
     * @param javascriptTree name of the js variable
     * @param request http request, needed for context path
     */
    public TreeColumn(String javascriptTree, HttpServletRequest request) {
        this.setJavascriptTree(javascriptTree);
        this.setCssClass("mgnlTreeText");
        this.setRequest(request);
        // default delegate
        this.setHtmlRenderer(new TreeColumnHtmlRendererImpl());
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
        edit.setEvent("onkeydown", this.getJavascriptTree() + ".editNodeDataKeydown(event,this);");
        edit.setEvent("onblur", this.getJavascriptTree() + EDIT_JSSAVE);
        // edit.setCssStyles("width","100%");
        this.setHtmlEdit(edit.getHtml());
    }

    public String getHtml() {
        String html = "";
        try {
            html = htmlRenderer.renderHtml(this, this.getWebsiteNode());
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        // "" not clickable!
        if (html.equals("")) {
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
}
