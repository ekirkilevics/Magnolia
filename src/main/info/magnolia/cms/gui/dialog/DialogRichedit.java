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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.i18n.ContextMessages;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.TemplateMessagesUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogRichedit extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogRichedit.class);

    private String richE = "";

    private String richEPaste = "";

    private List optionsToolboxStyleCssClasses = new ArrayList();

    private List optionsToolboxLinkCssClasses = new ArrayList();

    private List optionsToolboxLinkTargets = new ArrayList();

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogRichedit() {
    }

    public void setRichE(String s) {
        this.richE = s;
    }

    public String getRichE() {
        return this.richE;
    }

    public void setRichEPaste(String s) {
        this.richE = s;
    }

    public String getRichEPaste() {
        return this.richEPaste;
    }

    public void setOptionsToolboxStyleCssClasses(Content configNode) {
        List options = this.setOptionsToolbox(configNode, "optionsToolboxStyleCssClasses");
        this.setOptionsToolboxStyleCssClasses(options);
    }

    public void setOptionsToolboxStyleCssClasses(List l) {
        this.optionsToolboxStyleCssClasses = l;
    }

    public List getOptionsToolboxStyleCssClasses() {
        return this.optionsToolboxStyleCssClasses;
    }

    public void setOptionsToolboxLinkCssClasses(Content configNode) {
        List options = this.setOptionsToolbox(configNode, "optionsToolboxLinkCssClasses");
        this.setOptionsToolboxLinkCssClasses(options);
    }

    public void setOptionsToolboxLinkCssClasses(List l) {
        this.optionsToolboxLinkCssClasses = l;
    }

    public List getOptionsToolboxLinkCssClasses() {
        return this.optionsToolboxLinkCssClasses;
    }

    public void setOptionsToolboxLinkTargets(Content configNode) {
        List options = this.setOptionsToolbox(configNode, "optionsToolboxLinkTargets");
        this.setOptionsToolboxLinkTargets(options);
    }

    public void setOptionsToolboxLinkTargets(List l) {
        this.optionsToolboxLinkTargets = l;
    }

    public List getOptionsToolboxLinkTargets() {
        return this.optionsToolboxLinkTargets;
    }

    public List setOptionsToolbox(Content configNode, String nodeName) {
        List options = new ArrayList();
        try {
            Iterator it = configNode.getContentNode(nodeName).getChildren().iterator();
            while (it.hasNext()) {
                ContentNode n = (ContentNode) it.next();
                String value = n.getNodeData("value").getString();
                SelectOption option = new SelectOption(null, value);
                if (n.getNodeData("label").isExist()) {
                    String label = n.getNodeData("label").getString();
                    label = TemplateMessagesUtil.get(this, label);
                    option.setLabel(label);
                }
                if (n.getNodeData("selected").getBoolean()) {
                    option.setId("default");
                }
                options.add(option);
            }
            SelectOption lastOption = new SelectOption("", "");
            lastOption.setSelected(true);
            options.add(lastOption);
        }
        catch (PathNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Option \"" + nodeName + "\" not found");
            }
        }
        catch (RepositoryException e) {
            log.info("Exception caught: " + e.getMessage(), e);
        }
        return options;
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        setOptionsToolboxLinkTargets(configNode);
        setOptionsToolboxLinkCssClasses(configNode);
        setOptionsToolboxStyleCssClasses(configNode);
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        Messages msgs = MessagesManager.getMessages(this.getRequest());
        
        this.drawHtmlPre(out);
        if (this.getRichE().equals("true")
            || (this.getRichE().equals("") && this.getTopParent().getConfigValue("richE", "").equals("true"))) {

            DialogLine line = new DialogLine();
            this.setSessionAttribute();
            // remove <span>s by <a>s so its readable by kupu
            String value = this.getValue("<br />");
            value = value.replaceAll("<span ", "<a ");
            value = value.replaceAll("</span>", "</a>");
            this.setValue(value);
            // modification of dialogBox
            out.write("</td></tr><tr><td style=\"padding-right:12px;\">");
            // #################
            // toolboxes
            // #################
            // toolbox paste
            String toolboxPasteType = this.getRichEPaste();
            if (toolboxPasteType.equals("")) {
                toolboxPasteType = this.getTopParent().getConfigValue("richEPaste", "false");
            }
            if (this.getConfigValue("toolboxPaste", "true").equals("true") && !toolboxPasteType.equals("false")) {
                // win only; clipboard on mac is clean already
                out.write(line.getHtml("100%"));
                out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXLABEL + "\">" + msgs.get("dialog.richedit.cleancopypast") + "</div>");
                if (toolboxPasteType.equals("button")) {
                    // ie/win
                    out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXSUBLABEL + "\">");
                    out.write(msgs.get("dialog.richedit.pasteUsingThisButton"));
                    out.write("<br/><a href=javascript:mgnlDialogRichEPasteCleanHelp();>" + msgs.get("dialog.richedit.info") + "</a>");
                    out.write("</div>");
                    out.write(Spacer.getHtml(6, 6));
                    Button pastePaste = new Button();
                    pastePaste.setLabel(msgs.get("dialog.richedit.cleanpaste"));
                    pastePaste.setSmall(true);
                    pastePaste.setOnclick("mgnlDialogRichEPasteClean('" + this.getName() + "',true);");
                    out.write(pastePaste.getHtml());
                }
                else {
                    // mozilla/win
                    out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXSUBLABEL + "\">");
                    out.write(msgs.get("dialog.richedit.pastetext"));
                    out.write("<br/><a href=javascript:mgnlDialogRichEPasteCleanHelp();>" + msgs.get("dialog.richedit.info") + "</a>");
                    out.write("</div>");
                    out.write(Spacer.getHtml(3, 3));
                    out.write("<textarea class=\""
                        + CssConstants.CSSCLASS_EDIT
                        + "\" name=\""
                        + this.getName()
                        + "-paste\" rows=\"2\" style=\"width:100%;\"></textarea>");
                    out.write(Spacer.getHtml(3, 3));
                    Button pasteAppend = new Button();
                    pasteAppend.setLabel(msgs.get("dialog.richedit.append"));
                    pasteAppend.setSmall(true);
                    pasteAppend.setOnclick("mgnlDialogRichEPasteTextarea('" + this.getName() + "',true);");
                    out.write(pasteAppend.getHtml());
                    Button pasteInsert = new Button();
                    pasteInsert.setLabel(msgs.get("dialog.richedit.insert"));
                    pasteInsert.setSmall(true);
                    pasteInsert.setOnclick("mgnlDialogRichEPasteTextarea('" + this.getName() + "',false);");
                    out.write(pasteInsert.getHtml());
                }
                out.write(Spacer.getHtml(36, 36));
            }
            // END toolbox paste
            // toolbox link
            if (this.getConfigValue("toolboxLink", "true").equals("true")) {
                out.write(line.getHtml("100%"));
                out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXLABEL + "\">" + msgs.get("dialog.richedit.link")+ "</div>");
                // link: edit control (href)
                String linkEditName = "kupu-link-input";
                Edit linkEdit = new Edit(linkEditName, "");
                linkEdit.setCssClass(CssConstants.CSSCLASS_EDIT);
                linkEdit.setSaveInfo(false);
                linkEdit.setCssStyles("width", "100%");
                out.write(linkEdit.getHtml());
                out.write(Spacer.getHtml(2, 2));
                // link: button internal link browse
                Button linkButtonBrowse = new Button();
                // todo: extension
                String extension = this.getConfigValue("toolboxLinkExtension", "html");
                String repository = this.getConfigValue("toolboxLinkRepository", ContentRepository.WEBSITE);
                linkButtonBrowse.setOnclick("mgnlDialogLinkOpenBrowser('"
                    + linkEditName
                    + "','"
                    + repository
                    + "','"
                    + extension
                    + "',true);");
                linkButtonBrowse.setSmall(true);
                linkButtonBrowse.setLabel(msgs.get("dialog.richedit.internallink"));
                out.write(linkButtonBrowse.getHtml());
                // link: target
                if (this.getOptionsToolboxLinkTargets().size() > 1) {
                    out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXSUBLABEL + "\">"+msgs.get("dialog.richedit.target")+"</div>");
                    Select control = new Select();
                    control.setName("kupu-link-input-target");
                    control.setSaveInfo(false);
                    control.setCssClass(CssConstants.CSSCLASS_SELECT);
                    control.setCssStyles("width", "100%");
                    control.setOptions(this.getOptionsToolboxLinkTargets());
                    out.write(control.getHtml());
                }
                // link: css class
                if (this.getOptionsToolboxLinkCssClasses().size() > 1) {
                    out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXSUBLABEL + "\">"+msgs.get("dialog.richedit.style")+"</div>");
                    Select control = new Select();
                    control.setName("kupu-link-input-css");
                    control.setSaveInfo(false);
                    control.setCssClass(CssConstants.CSSCLASS_SELECT);
                    control.setCssStyles("width", "100%");
                    control.setOptions(this.getOptionsToolboxLinkCssClasses());
                    out.write(control.getHtml());
                }
                out.write(Spacer.getHtml(3, 3));
                // link: apply button
                Button linkButtonApply = new Button();
                linkButtonApply.setId("kupu-link-button");
                linkButtonApply.setLabel(msgs.get("dialog.richedit.applaylink"));
                linkButtonApply.setSmall(true);
                out.write(linkButtonApply.getHtml());
                // link: remove button
                Button linkButtonRemove = new Button();
                linkButtonRemove.setId("kupu-link-button-remove");
                linkButtonRemove.setLabel(msgs.get("dialog.richedit.removelink"));
                linkButtonRemove.setSmall(true);
                out.write(linkButtonRemove.getHtml());
                out.write(Spacer.getHtml(36, 36));
            }
            // END toolbox link
            // toolbox css
            if (this.getConfigValue("toolboxStyle", "false").equals("true")) {
                out.write(line.getHtml("100%"));
                out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXLABEL + "\">"+msgs.get("dialog.richedit.textstyle")+"</div>");
                if (this.getOptionsToolboxStyleCssClasses().size() > 1) {
                    Select control = new Select();
                    control.setName(this.getName() + "-css-input-css");
                    control.setSaveInfo(false);
                    control.setCssClass(CssConstants.CSSCLASS_SELECT);
                    control.setCssStyles("width", "100%");
                    control.setOptions(this.getOptionsToolboxStyleCssClasses());
                    out.write(control.getHtml());
                }
                out.write(Spacer.getHtml(3, 3));
                // css: apply button
                Button cssButtonApply = new Button();
                cssButtonApply.setId(this.getName() + "-css-button");
                cssButtonApply.setLabel(msgs.get("dialog.richedit.applaystyle"));
                cssButtonApply.setSmall(true);
                out.write(cssButtonApply.getHtml());
                // css: remove button
                Button cssButtonRemove = new Button();
                cssButtonRemove.setId(this.getName() + "-css-button-remove");
                cssButtonRemove.setLabel(msgs.get("dialog.richedit.removestyle"));
                cssButtonRemove.setSmall(true);
                out.write(cssButtonRemove.getHtml());
            }
            // END toolbox css
            // #################
            // END toolboxes
            // #################
            // modification of dialogBox
            out.write("</td><td>");
            // #################
            // toolbar
            // #################
            out.write("<div class=\"kupu-tb\" id=\"toolbar\">");
            out.write("<span id=\"kupu-tb-buttons\">");
            out.write("<span class=\"kupu-tb-buttongroup\">");
            if (this.getConfigValue("toolbarBold", "true").equals("true")) {
                out
                    .write("<button type=\"button\" class=\"kupu-bold\" title=\""+msgs.get("dialog.richedit.bold")+"\" onclick=\"kupuui.basicButtonHandler('bold');\">&nbsp;</button>");
            }
            if (this.getConfigValue("toolbarItalic", "true").equals("true")) {
                out
                    .write("<button type=\"button\" class=\"kupu-italic\" title=\""+msgs.get("dialog.richedit.italic")+"\" onclick=\"kupuui.basicButtonHandler('italic');\">&nbsp;</button>");
            }
            if (this.getConfigValue("toolbarUnderline", "false").equals("true")) {
                out
                    .write("<button type=\"button\" class=\"kupu-underline\" title=\""+msgs.get("dialog.richedit.underline")+"\" onclick=\"kupuui.basicButtonHandler('underline');\">&nbsp;</button>");
            }
            out.write("</span>");
            out.write("<span class=\"kupu-tb-buttongroup\">");
            if (this.getConfigValue("toolbarSubscript", "false").equals("true")) {
                out
                    .write("<button type=\"button\" class=\"kupu-subscript\" title=\""+msgs.get("dialog.richedit.subscript")+"\" onclick=\"kupuui.basicButtonHandler('subscript');\">&nbsp;</button>");
            }
            if (this.getConfigValue("toolbarSuperscript", "false").equals("true")) {
                out
                    .write("<button type=\"button\" class=\"kupu-superscript\" title=\""+msgs.get("dialog.richedit.superscript")+"\" onclick=\"kupuui.basicButtonHandler('superscript');\">&nbsp;</button>");
            }
            out.write("</span>");
            if (this.getConfigValue("toolbarColors", "false").equals("true")) {
                // kupu note: the event handlers are attached to these buttons dynamically, like for tools
                // mozilla (1.5) does not support font background color yet!
                out.write("<span class=\"kupu-tb-buttongroup\">");
                out
                    .write("<button type=\"button\" class=\"kupu-forecolor\" id=\"kupu-forecolor\" title=\""+msgs.get("dialog.richedit.textcolor")+"\">&nbsp;</button>");
                out
                    .write("<button type=\"button\" class=\"kupu-hilitecolor\" id=\"kupu-hilitecolor\" title=\""+msgs.get("dialog.richedit.backgroundcolor")+"\">&nbsp;</button>");
                out.write("</span>");
            }
            if (this.getConfigValue("toolbarUndo", "true").equals("true")) {
                out.write(" <span class=\"kupu-tb-buttongroup\">");
                out
                    .write("<button type=\"button\" class=\"kupu-undo\" title=\""+msgs.get("dialog.richedit.undo")+"\" onclick=\"kupuui.basicButtonHandler('undo');\">&nbsp;</button>");
                out
                    .write("<button type=\"button\" class=\"kupu-redo\" title=\""+msgs.get("dialog.richedit.redo")+"\" onclick=\"kupuui.basicButtonHandler('redo');\">&nbsp;</button>");
                out.write(" </span>");
            }
            if (this.getConfigValue("toolbarLists", "true").equals("true")) {
                out.write("<span class=\"kupu-tb-buttongroup\">");
                // kupu note: list button events are set on the list tool
                out
                    .write("<button type=\"button\" class=\"kupu-insertorderedlist\" title=\""+msgs.get("dialog.richedit.numberedlist")+"\" id=\"kupu-list-ol-addbutton\">&nbsp;</button>");
                out
                    .write("<button type=\"button\" class=\"kupu-insertunorderedlist\" title=\""+msgs.get("dialog.richedit.unorderedlist")+"\" id=\"kupu-list-ul-addbutton\">&nbsp;</button>");
                out.write("</span>");
                out.write("<select id=\"kupu-ulstyles\" class=\"" + CssConstants.CSSCLASS_SELECT + "\">");
                out.write("  <option value=\"disc\">Disc</option>");
                out.write("  <option value=\"square\">Square</option>");
                out.write("  <option value=\"circle\">Circle</option>");
                out.write("  <option value=\"none\">no bullet</option>");
                out.write("</select>");
                out.write("<select id=\"kupu-olstyles\" class=\"" + CssConstants.CSSCLASS_SELECT + "\">");
                out.write("  <option value=\"decimal\">1</option>");
                out.write("  <option value=\"upper-roman\">I</option>");
                out.write("  <option value=\"lower-roman\">i</option>");
                out.write("  <option value=\"upper-alpha\">A</option>");
                out.write("  <option value=\"lower-alpha\">a</option>");
                out.write("</select>");
            }
            out.write("</span>");
            out.write("</div>");
            // #################
            // END toolbar
            // #################
            // color palette
            out
                .write("<div id=\"kupu-colorchooser\" style=\"position: fixed; border-style: solid; border-color: #666666; border-width: 1px;\"> </div>");
            // #################
            // iframe
            // #################
            out.write("<iframe id=\"" + this.getName() + "-kupu-editor\"");
            out.write(" class=\"" + CssConstants.CSSCLASS_RICHEIFRAME + "\"");
            if (this.getConfigValue("height", null) != null) {
                out.write(" style=\"height:" + this.getConfigValue("height") + ";\"");
            }
            out.write(" frameborder=\"0\"");
            out.write(" src=\""
                + this.getRequest().getContextPath()
                + "/.magnolia/dialogs/richEIFrame.html?"
                + SESSION_ATTRIBUTENAME_DIALOGOBJECT
                + "="
                + this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT)
                + "&amp;mgnlCK="
                + new Date().getTime()
                + "\"");
            out.write(" reloadsrc=\"0\"");
            out.write(" usecss=\"1\"");
            out.write(" strict_output=\"1\"");
            out.write(" content_type=\"application/xhtml+xml\"");
            out.write(" scrolling=\"auto\"");
            out.write("></iframe>");
            out.write("<script type=\"text/javascript\">");
            out.write("mgnlRichEditors[mgnlRichEditors.length]='" + this.getName() + "';");
            out.write("</script>");
            // #################
            // END iframe
            // #################
            // #################
            // textarea to save data (data will be put into textarea on submit of form)
            // #################
            out.write("<div style=visibility:hidden;position:absolute;top:0px;left:-500px;>");
            Edit hiddenTextarea = new Edit(this.getName(), "");
            hiddenTextarea.setRows("5");
            hiddenTextarea.setIsRichEditValue(1);
            out.write(hiddenTextarea.getHtml());
            out.write("</div>");

        }
        else {
            // rich edit not supported: draw textarea
            Edit control = new Edit(this.getName(), this.getValue());
            control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING));
            if (this.getConfigValue("saveInfo").equals("false")) {
                control.setSaveInfo(false);
            }
            control.setCssClass(CssConstants.CSSCLASS_EDIT);
            control.setRows(this.getConfigValue("rows", "18"));
            control.setCssStyles("width", this.getConfigValue("width", "100%"));

            out.write(control.getHtml());

        }
        this.drawHtmlPost(out);
    }

    public void drawHtmlEditor(Writer out) throws IOException {

        out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        out.write(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");

        out.write("<html><head>");
        // headers to prevent the browser from caching, these *must* be provided,
        // either in meta-tag form or as HTTP headers
        out.write("<meta http-equiv=\"Pragma\" content=\"no-cache\" />");
        out.write("<meta http-equiv=\"Cache-Control\" content=\"no-cache, must-revalidate\" />");
        out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
        out.write("<meta name=\"Effective_date\" content=\"None\" />");
        out.write("<meta name=\"Expiration_date\" content=\"None\" />");
        out.write("<meta name=\"Type\" content=\"Document\" />");
        // out.write("<meta name=\"Format\" content=\"text/html\" />");
        out.write("<meta name=\"Language\" content=\"\" />");
        out.write("<meta name=\"Rights\" content=\"\" />");
        out.write("<style type=\"text/css\">");
        out.write("body {font-family:verdana;font-size:11px;background-color:#ffffff;}");
        out.write("</style>");
        if (this.getConfigValue("cssFile", null) != null) {
            out.write("<link href=\"" + this.getConfigValue("cssFile") + "\" rel=\"stylesheet\" type=\"text/css\"/>");
        }
        out.write("<script type=\"text/javascript\">\n");
        out.write("document.insertText=function(value)\n");
        out.write(" {\n");
        out.write(" while (value.indexOf('\\n')!=-1)\n");
        out.write(" {\n");
        out.write(" value=value.replace('\\n','<br />');\n");
        out.write(" }\n");
        out.write(" var body=document.getElementsByTagName('body');\n");
        out.write(" value=body[0].innerHTML+value;\n");
        out.write(" body[0].innerHTML=value;\n");
        out.write(" }\n");
        out.write("</script>\n");
        out.write("</head>\n");
        out.write("<body>");
        out.write(this.getValue());
        out.write("</body></html>");

    }

    public String getValue(String lineBreak) {
        String value = this.getValue();

        if (value != null) {
            return value.replaceAll("\n", "<br />");
        }
        else if (this.getWebsiteNode() != null) {
            return this.getWebsiteNode().getNodeData(this.getName()).getString(lineBreak);
        }
        else {
            return StringUtils.EMPTY;
        }
    }
}
