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
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.util.MetaDataUtil;
import info.magnolia.cms.util.NodeDataUtil;
import java.util.Calendar;
import java.util.Hashtable;
import javax.jcr.RepositoryException;
import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class TreeColumn extends ControlSuper {

    private int width = 1;

    boolean isMeta = false;

    boolean isLabel = false;

    boolean isIcons = false;

    boolean iconsActivation = false;

    boolean iconsPermission = false;

    boolean isNodeDataValue = false;

    boolean isNodeDataType = false;

    String dateFormat = null;

    private String javascriptTree = "";

    String title = "";

    boolean permissionWrite = false;

    String htmlEdit = "";

    private static Logger log = Logger.getLogger(TreeColumn.class);

    public static final String EDIT_CSSCLASS_EDIT = DialogSuper.CSSCLASS_EDIT;

    public static final String EDIT_CSSCLASS_SELECT = DialogSuper.CSSCLASS_SELECT;

    public static final String EDIT_NAMEADDITION = "_EditNodeData";

    public static final String EDIT_JSSAVE = ".saveNodeData(this.value);";

    public static final String EMPTY = "-";

    // if EMPTY changes, it has to be updated also in tree.js; note: if you use html tags, use upper-case letters (and
    // test it well ;)
    private Hashtable keyValue = new Hashtable();

    /**
     * <p>
     * constructor
     * </p>
     * @param javascriptTree: name of the js variable
     */
    public TreeColumn(String javascriptTree) {
        this.setJavascriptTree(javascriptTree);
        this.setCssClass("mgnlTreeText");
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

    public void setKeyValue(Hashtable h) {
        this.keyValue = h;
    }

    public Hashtable getKeyValue() {
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
            if (this.getIsMeta()) {
                html = new MetaDataUtil(this.getWebsiteNode()).getPropertyValueString(this.getName(), this
                    .getDateFormat());
            }
            else if (this.getIsLabel()) {
                html = this.getWebsiteNode().getName();
            }
            else if (this.getIsIcons()) {
                html = this.getHtmlIcons();
            }
            else {
                NodeData data = this.getWebsiteNode().getNodeData(this.getName());
                html = new NodeDataUtil(data).getValueString(this.getDateFormat());
            }
            // todo: (value is not shown after saving ...)
            if (this.getKeyValue().size() != 0) {
                String value = (String) this.getKeyValue().get(html);
                if (value != null)
                    html = value;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        // "" not clickable!
        if (html.equals(""))
            html = TreeColumn.EMPTY;
        return html;
    }

    public String getHtmlIcons() throws RepositoryException {
        StringBuffer html = new StringBuffer();
        if (this.getIconsActivation()) {
            MetaData activationMetaData = this.getWebsiteNode().getMetaData(MetaData.ACTIVATION_INFO);
            MetaData generalMetaData = this.getWebsiteNode().getMetaData();
            boolean isActivated = activationMetaData.getIsActivated();
            Calendar actionDate = activationMetaData.getLastActionDate();
            Calendar lastModifiedDate = generalMetaData.getModificationDate();
            String imgSrc;
            if (isActivated) {
                if (lastModifiedDate.after(actionDate)) {
                    // node has been modified after last activation
                    imgSrc = Tree.ICONDOCROOT + "indicator_yellow.gif";
                }
                else {
                    // activated and not modified ever since
                    imgSrc = Tree.ICONDOCROOT + "indicator_green.gif";
                }
            }
            else {
                // never activated or deactivated
                imgSrc = Tree.ICONDOCROOT + "indicator_red.gif";
            }
            html.append("<img src=\"" + imgSrc + "\">");
        }
        if (this.getIconsPermission()) {
            if (!this.getWebsiteNode().isGranted(info.magnolia.cms.security.Permission.WRITE)) {
                html.append("<img src=\"" + Tree.ICONDOCROOT + "pen_blue_canceled.gif\">");
            }
        }
        return html.toString();
    }
}
