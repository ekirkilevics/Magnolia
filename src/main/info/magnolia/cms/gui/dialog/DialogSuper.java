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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.control.Hidden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogSuper implements DialogInterface {

    public static final String SESSION_ATTRIBUTENAME_DIALOGOBJECT = "mgnlSessionAttribute";

    public static final String SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE = "mgnlSessionAttributeRemove";

    public static final String CSSCLASS_INTERCOL = "mgnlDialogInterCol";

    public static final String CSSCLASS_EDIT = "mgnlDialogControlEdit";

    public static final String CSSCLASS_EDITWITHBUTTON = "mgnlDialogEditWithButton";

    public static final String CSSCLASS_BOXLABEL = "mgnlDialogBoxLabel";

    public static final String CSSCLASS_EDITSMALL = "mgnlDialogControlEditSmall";

    public static final String CSSCLASS_BOXINPUT = "mgnlDialogBoxInput";

    public static final String CSSCLASS_BOXLINE = "mgnlDialogBoxLine";

    public static final String CSSCLASS_SELECT = "mgnlDialogControlSelect";

    public static final String CSSCLASS_SELECTSMALL = "mgnlDialogControlSelectSmall";

    public static final String CSSCLASS_DESCRIPTION = "mgnlDialogDescription";

    public static final String CSSCLASS_SMALL = "mgnlDialogSmall";

    public static final String CSSCLASS_WEBDAVIFRAME = "mgnlDialogWebDAVIFrame";

    public static final String CSSCLASS_RICHEIFRAME = "mgnlDialogRichEIFrame";

    public static final String CSSCLASS_RICHETOOLBOXLABEL = "mgnlDialogRichEToolboxLabel";

    public static final String CSSCLASS_RICHETOOLBOXSUBLABEL = "mgnlDialogRichEToolboxSubLabel";

    public static final String CSSCLASS_TINYVSPACE = "mgnlDialogSpacer";

    public static final String CSSCLASS_TAB = "mgnlDialogTab";

    public static final String CSSCLASS_TABSETBUTTONBAR = "mgnlDialogTabsetButtonBar";

    public static final String CSSCLASS_TABSETSAVEBAR = "mgnlDialogTabsetSaveBar";

    public static final String CSSCLASS_BUTTONSETLABEL = "mgnlDialogButtonsetLabel";

    public static final String CSSCLASS_BUTTONSETBUTTON = "mgnlDialogButtonsetButton";

    public static final String CSSCLASS_BUTTONSETINTERCOL = "mgnlDialogButtonsetInterCol";

    public static final String CSSCLASS_FILE = "mgnlDialogControlFile";

    public static final String CSSCLASS_FILEIMAGE = "mgnlDialogFileImage";

    public static final String CSSCLASS_FILEICON = "mgnlDialogFileIcon";

    public static final String CSSCLASS_BGALT = "mgnlDialogBgAlt";

    public static final String NULLGIF = "/admindocroot/0.gif";

    public static final String ICONS_PATH = "/admindocroot/fileIcons/";

    public static final String ICONS_GENERAL = "general.gif";

    public static final String ICONS_FOLDER = "folder.gif";

    public static final String ICONS_FOLDERUP = "folder_up.gif";

    public static final int ICONS_HEIGHT = 16;

    public static final int ICONS_WIDTH = 23;

    public static final String THUMB_PATH = "/.magnolia/dialogs/fileThumbnail.jpg";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogSuper.class);

    private HttpServletRequest request;

    private PageContext pageContext;

    /**
     * content data.
     */
    private Content websiteNode;

    /**
     * config structure/data (dialog.xml).
     */
    private Content configNode;

    /**
     * config data (written from dialog.xml to config).
     */
    private Map config = new Hashtable();

    /**
     * Sub controls (written from dialog.xml to subs).
     */
    private List subs = new ArrayList();

    /**
     * options (radio, checkbox...).
     */
    private List options = new ArrayList();

    private String id = "mgnlControl";

    private String value;

    /**
     * multiple values, e.g. checkbox.
     */
    private List values = new ArrayList();

    private DialogSuper parent;

    private DialogSuper topParent;

    private Map iconExtensions = new Hashtable();

    public DialogSuper() {
    }

    public DialogSuper(ContentNode configNode, Content websiteNode) throws RepositoryException {
        // constructor for all controls except DialogDialog
        this.setConfigNode(configNode);
        this.setWebsiteNode(websiteNode);
        // following uses values from above -> so set as lasts
        this.setConfig(configNode);
    }

    public DialogSuper(Content configNode, Content websiteNode, HttpServletRequest request, PageContext pageContext)
        throws RepositoryException {
        // constructor for DialogDialog
        this.setConfigNode(configNode);
        this.setWebsiteNode(websiteNode);
        this.setRequest(request);
        this.setPageContext(pageContext);
        // following uses values from above -> set as last
        this.setConfig(configNode);
    }

    public void drawHtml(JspWriter out) throws IOException {
        this.drawHtmlPreSubs(out);
        this.drawSubs(out);
        this.drawHtmlPostSubs(out);
    }

    public void drawSubs(JspWriter out) throws IOException {
        Iterator it = this.getSubs().iterator();
        int i = 0;
        while (it.hasNext()) {
            String dsId = this.getId() + "_" + i; // use underscore (not divis)! could be used as js variable names
            DialogSuper ds = (DialogSuper) it.next();
            ds.setId(dsId);
            ds.setParent(this);
            if (this.getParent() == null) {
                this.setTopParent(this);
            }
            ds.setTopParent(this.getTopParent());
            ds.drawHtml(out);
            i++;
        }
    }

    public void setParent(DialogSuper parent) {
        this.parent = parent;
    }

    public DialogSuper getParent() {
        return this.parent;
    }

    public void setTopParent(DialogSuper top) {
        this.topParent = top;
    }

    public DialogSuper getTopParent() {
        return this.topParent;
    }

    public void drawHtmlPreSubs(JspWriter out) throws IOException {
    }

    public void drawHtmlPostSubs(JspWriter out) throws IOException {
    }

    public void setSubs(List subs) {
        this.subs = subs;
    }

    public void addSub(Object o) {
        this.getSubs().add(o);
    }

    public List getSubs() {
        return this.subs;
    }

    public Map getConfig() {
        return this.config;
    }

    public void setConfig(Map config) {
        this.config = config;
    }

    public void setConfig(String key, String value) {
        if (value != null) {
            this.getConfig().put(key, value);
        }
    }

    public void setConfig(String key, boolean value) {
        if (value) {
            this.getConfig().put(key, "true");
        }
        else {
            this.getConfig().put(key, "false");
        }
    }

    public void setConfig(String key, int value) {
        this.getConfig().put(key, Integer.toString(value));
    }

    public String getConfigValue(String key, String nullValue) {
        if (this.getConfig().containsKey(key)) {
            return (String) this.getConfig().get(key);
        }

        return nullValue;
    }

    public String getConfigValue(String key) {
        return this.getConfigValue(key, "");
    }

    public void setConfig(Content configNodeParent) throws RepositoryException {
        // create config and subs out of dialog structure (xml)
        Map config = new Hashtable();
        // get properties -> to this.config
        Iterator itProps = configNodeParent.getChildren(ItemType.NT_NODEDATA).iterator();
        while (itProps.hasNext()) {
            NodeData data = (NodeData) itProps.next();
            String name = data.getName();
            String value = data.getString();
            config.put(name, value);
        }
        this.setConfig(config);
        // String parentType=configNodeParent.getNodeData("type").getString();
        Iterator it = configNodeParent.getChildren(ItemType.NT_CONTENTNODE).iterator();
        while (it.hasNext()) {
            ContentNode configNode = (ContentNode) it.next();
            String controlType = configNode.getNodeData("controlType").getString();
            if (controlType.equals("edit")) {
                DialogEdit dialogControl = new DialogEdit(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("richEdit")) {
                DialogRichedit dialogControl = new DialogRichedit(configNode, this.getWebsiteNode());
                dialogControl.setOptionsToolboxLinkTargets(configNode);
                dialogControl.setOptionsToolboxLinkCssClasses(configNode);
                dialogControl.setOptionsToolboxStyleCssClasses(configNode);
                this.addSub(dialogControl);
            }
            else if (controlType.equals("fckEdit")) {
                DialogRichedit dialogControl = new DialogRichedit(configNode, this.getWebsiteNode());
                dialogControl.setOptionsToolboxLinkTargets(configNode);
                dialogControl.setOptionsToolboxLinkCssClasses(configNode);
                dialogControl.setOptionsToolboxStyleCssClasses(configNode);
                this.addSub(dialogControl);
            }
            else if (controlType.equals("tab")) {
                DialogTab dialogControl = new DialogTab(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("buttonSet")) {
                DialogButtonSet dialogControl = new DialogButtonSet(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("button")) {
                DialogButton dialogControl = new DialogButton(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("static")) {
                DialogStatic dialogControl = new DialogStatic(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("file")) {
                DialogFile dialogControl = new DialogFile(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("link")) {
                DialogLink dialogControl = new DialogLink(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("date")) {
                DialogDate dialogControl = new DialogDate(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("radio")) {
                DialogButtonSet dialogControl = new DialogButtonSet(configNode, this.getWebsiteNode());
                dialogControl.setButtonType(ControlSuper.BUTTONTYPE_RADIO);
                dialogControl.setOptions(configNode, true);
                this.addSub(dialogControl);
            }
            else if (controlType.equals("checkbox") || controlType.equals("checkboxSwitch")) {
                DialogButtonSet dialogControl = new DialogButtonSet(configNode, this.getWebsiteNode());
                dialogControl.setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX);
                if (controlType.equals("checkbox")) {
                    dialogControl.setOptions(configNode, false);
                    dialogControl.setConfig("valueType", "multiple");
                }
                else {
                    dialogControl.setOption(configNode);
                }
                this.addSub(dialogControl);
            }
            else if (controlType.equals("select")) {
                DialogSelect dialogControl = new DialogSelect(configNode, this.getWebsiteNode());
                dialogControl.setOptions(configNode);
                this.addSub(dialogControl);
            }
            else if (controlType.equals("password")) {
                DialogPassword dialogControl = new DialogPassword(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("include")) {
                DialogInclude dialogControl = new DialogInclude(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
            else if (controlType.equals("webDAV")) {
                DialogWebDAV dialogControl = new DialogWebDAV(configNode, this.getWebsiteNode());
                this.addSub(dialogControl);
            }
        }
    }

    public void setOptions(List options) {
        this.options = options;
    }

    public void addOption(Object o) {
        this.getOptions().add(o);
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletRequest getRequest() {
        return this.request;
    }

    public void setPageContext(PageContext p) {
        this.pageContext = p;
    }

    public PageContext getPageContext() {
        return this.pageContext;
    }

    public void setWebsiteNode(Content websiteNode) {
        this.websiteNode = websiteNode;
    }

    public Content getWebsiteNode() {
        return this.websiteNode;
    }

    public void setConfigNode(Content configNode) {
        this.configNode = configNode;
    }

    public Content getConfigNode() {
        return this.configNode;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String s) {
        this.getConfig().put("label", s);
    }

    public String getLabel() {
        return this.getConfigValue("label", "");
    }

    public void setDescription(String s) {
        this.getConfig().put("description", s);
    }

    public String getDescription() {
        return this.getConfigValue("description", "");
    }

    public String getHtmlDescription() {
        String html = "";
        // use div to force a new line
        if (!this.getDescription().equals("")) {
            html = "<div class=\"" + CSSCLASS_DESCRIPTION + "\">" + this.getDescription() + "</div>";
        }
        return html;
    }

    public String getControlType() {
        return (String) this.getConfig().get("controlType");
    }

    public List getOptions() {
        return this.options;
    }

    public void setValue(String s) {
        this.value = s;
    }

    public String getValue() {
        if (this.value != null) {
            return this.value;
        }
        else if (this.getWebsiteNode() != null) {
            return this.getWebsiteNode().getNodeData(this.getName()).getString();
        }
        else {
            return "";
        }
    }

    public String getValue(String lineBreak) {
        if (this.value != null) {
            return this.value.replaceAll("\n", "<br>");
        }
        else if (this.getWebsiteNode() != null) {
            return this.getWebsiteNode().getNodeData(this.getName()).getString(lineBreak);
        }
        else {
            return "";
        }
    }

    public void setValues(List l) {
        this.values = l;
    }

    public List getValues() {
        if (this.getWebsiteNode() == null) {
            return this.values;
        }

        try {
            Iterator it = this
                .getWebsiteNode()
                .getContentNode(this.getName())
                .getChildren(ItemType.NT_NODEDATA)
                .iterator();
            List l = new ArrayList();
            while (it.hasNext()) {
                NodeData data = (NodeData) it.next();
                l.add(data.getString());
            }
            return l;
        }
        catch (RepositoryException re) {
            return this.values;
        }

    }

    public String getValue(boolean notFromWebsiteNode) {
        return this.value;
    }

    public void setName(String s) {
        this.setConfig("name", s);
    }

    public String getName() {
        return this.getConfigValue("name");
    }

    public void setSaveInfo(boolean b) {
        this.setConfig("saveInfo", b);
    }

    public String getSaveInfo() {
        return this.getConfigValue("saveInfo");
    }

    public void setSessionAttribute() {
        String name = SESSION_ATTRIBUTENAME_DIALOGOBJECT + "_" + this.getName() + "_" + new Date().getTime();
        this.setConfig(SESSION_ATTRIBUTENAME_DIALOGOBJECT, name);
        HttpServletRequest request = this.getRequest();
        if (request == null) {
            request = this.getTopParent().getRequest();
        }
        try {
            HttpSession session = request.getSession();
            session.setAttribute(name, this);
        }
        catch (Exception e) {
            log.error("setSessionAttribute() for " + name + " failed because this.request is null");
        }
    }

    public String getHtmlSessionAttributeRemoveControl() {
        return new Hidden(SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE, this
            .getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT), false).getHtml();
    }

    public void removeSessionAttribute() {
        String name = this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT);
        HttpServletRequest request = this.getRequest();
        if (request == null) {
            request = this.getTopParent().getRequest();
        }
        try {
            HttpSession session = request.getSession();
            session.removeAttribute(name);
        }
        catch (Exception e) {
            log.debug("removeSessionAttribute() for \"+name+\" failed because this.request is null");
        }
    }

    public Map getIconExtensions() {
        return this.iconExtensions;
    }

    public void setIconExtensions(Map t) {
        this.iconExtensions = t;
    }

    public void setIconExtensions(String extension, String iconPath) {
        this.getIconExtensions().put(extension, iconPath);
    }

    public void initIconExtensions() {
        this.getIconExtensions().put("doc", "");
        this.getIconExtensions().put("eps", "");
        this.getIconExtensions().put("gif", "");
        this.getIconExtensions().put("jpg", "");
        this.getIconExtensions().put("jpeg", ICONS_PATH + "jpg.gif");
        this.getIconExtensions().put("pdf", "");
        this.getIconExtensions().put("ppt", "");
        this.getIconExtensions().put("tif", "");
        this.getIconExtensions().put("tiff", ICONS_PATH + "tif.gif");
        this.getIconExtensions().put("xls", "");
        this.getIconExtensions().put("zip", "");
    }

    public String getIconPath(String name) {
        // name might be name (e.g. "bla.gif") or extension (e.g. "gif")
        String iconPath = ICONS_PATH + ICONS_GENERAL;
        String ext = "";
        if (name.indexOf(".") != -1) {
            ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        }
        else {
            ext = name;
        }
        if (this.getIconExtensions().containsKey(ext)) {
            iconPath = (String) this.getIconExtensions().get(ext);
            if (iconPath.equals("")) {
                iconPath = ICONS_PATH + ext + ".gif";
            }
        }
        return iconPath;
    }
}
