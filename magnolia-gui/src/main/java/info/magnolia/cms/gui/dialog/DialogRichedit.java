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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.*;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.LinkUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogRichedit extends DialogBox {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogRichedit.class);

    private String richE = StringUtils.EMPTY;

    private String richEPaste = StringUtils.EMPTY;

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
        List options = this.setOptionsToolbox(configNode, "optionsToolboxStyleCssClasses"); //$NON-NLS-1$
        this.setOptionsToolboxStyleCssClasses(options);
    }

    public void setOptionsToolboxStyleCssClasses(List l) {
        this.optionsToolboxStyleCssClasses = l;
    }

    public List getOptionsToolboxStyleCssClasses() {
        return this.optionsToolboxStyleCssClasses;
    }

    public void setOptionsToolboxLinkCssClasses(Content configNode) {
        List options = this.setOptionsToolbox(configNode, "optionsToolboxLinkCssClasses"); //$NON-NLS-1$
        this.setOptionsToolboxLinkCssClasses(options);
    }

    public void setOptionsToolboxLinkCssClasses(List l) {
        this.optionsToolboxLinkCssClasses = l;
    }

    public List getOptionsToolboxLinkCssClasses() {
        return this.optionsToolboxLinkCssClasses;
    }

    public void setOptionsToolboxLinkTargets(Content configNode) {
        List options = this.setOptionsToolbox(configNode, "optionsToolboxLinkTargets"); //$NON-NLS-1$
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
            Iterator it = configNode.getContent(nodeName).getChildren(ItemType.CONTENTNODE.getSystemName()).iterator();
            while (it.hasNext()) {
                Content n = (Content) it.next();
                String value = n.getNodeData("value").getString(); //$NON-NLS-1$
                SelectOption option = new SelectOption(null, value);
                if (n.getNodeData("label").isExist()) { //$NON-NLS-1$
                    String label = n.getNodeData("label").getString(); //$NON-NLS-1$
                    label = this.getMessage(label);
                    option.setLabel(label);
                }
                if (n.getNodeData("selected").getBoolean()) { //$NON-NLS-1$
                    option.setId("default"); //$NON-NLS-1$
                }
                options.add(option);
            }
            SelectOption lastOption = new SelectOption(StringUtils.EMPTY, StringUtils.EMPTY);
            lastOption.setSelected(true);
            options.add(lastOption);
        }
        catch (PathNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Option \"" + nodeName + "\" not found"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException e) {
            if (log.isDebugEnabled())
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
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
        Messages msgs = MessagesManager.getMessages();

        this.drawHtmlPre(out);
        if (this.getRichE().equals("true") //$NON-NLS-1$
                || (StringUtils.isEmpty(this.getRichE()) && this
                .getTopParent()
                .getConfigValue("richE", StringUtils.EMPTY).equals("true"))) { //$NON-NLS-1$ //$NON-NLS-2$

            DialogLine line = new DialogLine();
            this.setSessionAttribute();

            String value = this.getValue("<br />"); //$NON-NLS-1$
            // make proper links
            value = LinkUtil.convertUUIDsToAbsoluteLinks(value);

            // TODO since we do not manipulate during storing we do not have to replace them. Maybe this leads to
            // problems
            // remove <span>s by <a>s so its readable by kupu
            // value = value.replaceAll("<span ", "<a ");
            // value = value.replaceAll("</span>", "</a>");

            this.setValue(value);
            // modification of dialogBox
            out.write("</td></tr><tr><td style=\"padding-right:12px;\">"); //$NON-NLS-1$
            // #################
            // toolboxes
            // #################
            // toolbox paste
            String toolboxPasteType = this.getRichEPaste();
            if (StringUtils.isEmpty(toolboxPasteType)) {
                toolboxPasteType = this.getTopParent().getConfigValue("richEPaste", "false"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (this.getConfigValue("toolboxPaste", "true").equals("true") && !toolboxPasteType.equals("false"))
            { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                // win only; clipboard on mac is clean already
                out.write(line.getHtml("100%")); //$NON-NLS-1$
                out.write("<div class=\"" //$NON-NLS-1$
                        + CssConstants.CSSCLASS_RICHETOOLBOXLABEL
                        + "\">" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.cleancopypast") //$NON-NLS-1$
                        + "</div>"); //$NON-NLS-1$
                if (toolboxPasteType.equals("button")) { //$NON-NLS-1$
                    // ie/win
                    out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXSUBLABEL + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                    out.write(msgs.get("dialog.richedit.pasteUsingThisButton")); //$NON-NLS-1$
                    out.write("<br/><a href=javascript:mgnlDialogRichEPasteCleanHelp();>" //$NON-NLS-1$
                            + msgs.get("dialog.richedit.info") //$NON-NLS-1$
                            + "</a>"); //$NON-NLS-1$
                    out.write("</div>"); //$NON-NLS-1$
                    out.write(Spacer.getHtml(6, 6));
                    Button pastePaste = new Button();
                    pastePaste.setLabel(msgs.get("dialog.richedit.cleanpaste")); //$NON-NLS-1$
                    pastePaste.setSmall(true);
                    pastePaste.setOnclick("mgnlDialogRichEPasteClean('" + this.getName() + "',true);"); //$NON-NLS-1$ //$NON-NLS-2$
                    out.write(pastePaste.getHtml());
                } else {
                    // mozilla/win
                    out.write("<div class=\"" + CssConstants.CSSCLASS_RICHETOOLBOXSUBLABEL + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                    out.write(msgs.get("dialog.richedit.pastetext")); //$NON-NLS-1$
                    out.write("<br/><a href=javascript:mgnlDialogRichEPasteCleanHelp();>" //$NON-NLS-1$
                            + msgs.get("dialog.richedit.info") //$NON-NLS-1$
                            + "</a>"); //$NON-NLS-1$
                    out.write("</div>"); //$NON-NLS-1$
                    out.write(Spacer.getHtml(3, 3));
                    out.write("<textarea class=\"" //$NON-NLS-1$
                            + CssConstants.CSSCLASS_EDIT
                            + "\" name=\"" //$NON-NLS-1$
                            + this.getName()
                            + "-paste\" rows=\"2\" style=\"width:100%;\"></textarea>"); //$NON-NLS-1$
                    out.write(Spacer.getHtml(3, 3));
                    Button pasteAppend = new Button();
                    pasteAppend.setLabel(msgs.get("dialog.richedit.append")); //$NON-NLS-1$
                    pasteAppend.setSmall(true);
                    pasteAppend.setOnclick("mgnlDialogRichEPasteTextarea('" + this.getName() + "',true);"); //$NON-NLS-1$ //$NON-NLS-2$
                    out.write(pasteAppend.getHtml());
                    Button pasteInsert = new Button();
                    pasteInsert.setLabel(msgs.get("dialog.richedit.insert")); //$NON-NLS-1$
                    pasteInsert.setSmall(true);
                    pasteInsert.setOnclick("mgnlDialogRichEPasteTextarea('" + this.getName() + "',false);"); //$NON-NLS-1$ //$NON-NLS-2$
                    out.write(pasteInsert.getHtml());
                }
                out.write(Spacer.getHtml(36, 36));
            }
            // END toolbox paste
            // toolbox link
            if (this.getConfigValue("toolboxLink", "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write(line.getHtml("100%")); //$NON-NLS-1$
                out.write("<div class=\"" //$NON-NLS-1$
                        + CssConstants.CSSCLASS_RICHETOOLBOXLABEL
                        + "\">" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.link") //$NON-NLS-1$
                        + "</div>"); //$NON-NLS-1$
                // link: edit control (href)
                String linkEditName = "kupu-link-input"; //$NON-NLS-1$
                Edit linkEdit = new Edit(linkEditName, StringUtils.EMPTY);
                linkEdit.setCssClass(CssConstants.CSSCLASS_EDIT);
                linkEdit.setSaveInfo(false);
                linkEdit.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
                out.write(linkEdit.getHtml());
                out.write(Spacer.getHtml(2, 2));
                // link: button internal link browse
                Button linkButtonBrowse = new Button();
                // todo: extension
                String extension = this.getConfigValue("toolboxLinkExtension", "html"); //$NON-NLS-1$ //$NON-NLS-2$
                String repository = this.getConfigValue("toolboxLinkRepository", ContentRepository.WEBSITE); //$NON-NLS-1$
                linkButtonBrowse.setOnclick("mgnlDialogLinkOpenBrowser('" //$NON-NLS-1$
                        + linkEditName
                        + "','" //$NON-NLS-1$
                        + repository
                        + "','" //$NON-NLS-1$
                        + extension
                        + "',false);"); //$NON-NLS-1$
                linkButtonBrowse.setSmall(true);
                linkButtonBrowse.setLabel(msgs.get("dialog.richedit.internallink")); //$NON-NLS-1$
                out.write(linkButtonBrowse.getHtml());
                // link: target
                if (this.getOptionsToolboxLinkTargets().size() > 1) {
                    out.write("<div class=\"" //$NON-NLS-1$
                            + CssConstants.CSSCLASS_RICHETOOLBOXSUBLABEL
                            + "\">" //$NON-NLS-1$
                            + msgs.get("dialog.richedit.target") //$NON-NLS-1$
                            + "</div>"); //$NON-NLS-1$
                    Select control = new Select();
                    control.setName("kupu-link-input-target"); //$NON-NLS-1$
                    control.setSaveInfo(false);
                    control.setCssClass(CssConstants.CSSCLASS_SELECT);
                    control.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
                    control.setOptions(this.getOptionsToolboxLinkTargets());
                    out.write(control.getHtml());
                }
                // link: css class
                if (this.getOptionsToolboxLinkCssClasses().size() > 1) {
                    out.write("<div class=\"" //$NON-NLS-1$
                            + CssConstants.CSSCLASS_RICHETOOLBOXSUBLABEL
                            + "\">" //$NON-NLS-1$
                            + msgs.get("dialog.richedit.style") //$NON-NLS-1$
                            + "</div>"); //$NON-NLS-1$
                    Select control = new Select();
                    control.setName("kupu-link-input-css"); //$NON-NLS-1$
                    control.setSaveInfo(false);
                    control.setCssClass(CssConstants.CSSCLASS_SELECT);
                    control.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
                    control.setOptions(this.getOptionsToolboxLinkCssClasses());
                    out.write(control.getHtml());
                }
                out.write(Spacer.getHtml(3, 3));
                // link: apply button
                Button linkButtonApply = new Button();
                linkButtonApply.setId("kupu-link-button"); //$NON-NLS-1$
                linkButtonApply.setLabel(msgs.get("dialog.richedit.applaylink")); //$NON-NLS-1$
                linkButtonApply.setSmall(true);
                out.write(linkButtonApply.getHtml());
                // link: remove button
                Button linkButtonRemove = new Button();
                linkButtonRemove.setId("kupu-link-button-remove"); //$NON-NLS-1$
                linkButtonRemove.setLabel(msgs.get("dialog.richedit.removelink")); //$NON-NLS-1$
                linkButtonRemove.setSmall(true);
                out.write(linkButtonRemove.getHtml());
                out.write(Spacer.getHtml(36, 36));
            }
            // END toolbox link
            // toolbox css
            if (this.getConfigValue("toolboxStyle", "false").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write(line.getHtml("100%")); //$NON-NLS-1$
                out.write("<div class=\"" //$NON-NLS-1$
                        + CssConstants.CSSCLASS_RICHETOOLBOXLABEL
                        + "\">" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.textstyle") //$NON-NLS-1$
                        + "</div>"); //$NON-NLS-1$
                if (this.getOptionsToolboxStyleCssClasses().size() > 1) {
                    Select control = new Select();
                    control.setName(this.getName() + "-css-input-css"); //$NON-NLS-1$
                    control.setSaveInfo(false);
                    control.setCssClass(CssConstants.CSSCLASS_SELECT);
                    control.setCssStyles("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
                    control.setOptions(this.getOptionsToolboxStyleCssClasses());
                    out.write(control.getHtml());
                }
                out.write(Spacer.getHtml(3, 3));
                // css: apply button
                Button cssButtonApply = new Button();
                cssButtonApply.setId(this.getName() + "-css-button"); //$NON-NLS-1$
                cssButtonApply.setLabel(msgs.get("dialog.richedit.applaystyle")); //$NON-NLS-1$
                cssButtonApply.setSmall(true);
                out.write(cssButtonApply.getHtml());
                // css: remove button
                Button cssButtonRemove = new Button();
                cssButtonRemove.setId(this.getName() + "-css-button-remove"); //$NON-NLS-1$
                cssButtonRemove.setLabel(msgs.get("dialog.richedit.removestyle")); //$NON-NLS-1$
                cssButtonRemove.setSmall(true);
                out.write(cssButtonRemove.getHtml());
            }
            // END toolbox css
            // #################
            // END toolboxes
            // #################
            // modification of dialogBox
            out.write("</td><td>"); //$NON-NLS-1$
            // #################
            // toolbar
            // #################
            out.write("<div class=\"kupu-tb\" id=\"toolbar\">"); //$NON-NLS-1$
            out.write("<span id=\"kupu-tb-buttons\">"); //$NON-NLS-1$
            out.write("<span class=\"kupu-tb-buttongroup\">"); //$NON-NLS-1$
            if (this.getConfigValue("toolbarBold", "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write("<button type=\"button\" class=\"kupu-bold\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.bold") //$NON-NLS-1$
                        + "\" onclick=\"kupuui.basicButtonHandler('bold');\">&nbsp;</button>"); //$NON-NLS-1$
            }
            if (this.getConfigValue("toolbarItalic", "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write("<button type=\"button\" class=\"kupu-italic\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.italic") //$NON-NLS-1$
                        + "\" onclick=\"kupuui.basicButtonHandler('italic');\">&nbsp;</button>"); //$NON-NLS-1$
            }
            if (this.getConfigValue("toolbarUnderline", "false").equals("true"))
            { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write("<button type=\"button\" class=\"kupu-underline\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.underline") //$NON-NLS-1$
                        + "\" onclick=\"kupuui.basicButtonHandler('underline');\">&nbsp;</button>"); //$NON-NLS-1$
            }
            out.write("</span>"); //$NON-NLS-1$
            out.write("<span class=\"kupu-tb-buttongroup\">"); //$NON-NLS-1$
            if (this.getConfigValue("toolbarSubscript", "false").equals("true"))
            { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write("<button type=\"button\" class=\"kupu-subscript\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.subscript") //$NON-NLS-1$
                        + "\" onclick=\"kupuui.basicButtonHandler('subscript');\">&nbsp;</button>"); //$NON-NLS-1$
            }
            if (this.getConfigValue("toolbarSuperscript", "false").equals("true"))
            { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write("<button type=\"button\" class=\"kupu-superscript\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.superscript") //$NON-NLS-1$
                        + "\" onclick=\"kupuui.basicButtonHandler('superscript');\">&nbsp;</button>"); //$NON-NLS-1$
            }
            out.write("</span>"); //$NON-NLS-1$
            if (this.getConfigValue("toolbarColors", "false").equals("true"))
            { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                // kupu note: the event handlers are attached to these buttons dynamically, like for tools
                // mozilla (1.5) does not support font background color yet!
                out.write("<span class=\"kupu-tb-buttongroup\">"); //$NON-NLS-1$
                out.write("<button type=\"button\" class=\"kupu-forecolor\" id=\"kupu-forecolor\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.textcolor") //$NON-NLS-1$
                        + "\">&nbsp;</button>"); //$NON-NLS-1$
                out.write("<button type=\"button\" class=\"kupu-hilitecolor\" id=\"kupu-hilitecolor\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.backgroundcolor") //$NON-NLS-1$
                        + "\">&nbsp;</button>"); //$NON-NLS-1$
                out.write("</span>"); //$NON-NLS-1$
            }
            if (this.getConfigValue("toolbarUndo", "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write(" <span class=\"kupu-tb-buttongroup\">"); //$NON-NLS-1$
                out.write("<button type=\"button\" class=\"kupu-undo\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.undo") //$NON-NLS-1$
                        + "\" onclick=\"kupuui.basicButtonHandler('undo');\"></button>"); //$NON-NLS-1$
                out.write("<button type=\"button\" class=\"kupu-redo\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.redo") //$NON-NLS-1$
                        + "\" onclick=\"kupuui.basicButtonHandler('redo');\"></button>"); //$NON-NLS-1$
                out.write(" </span>"); //$NON-NLS-1$
            }
            if (this.getConfigValue("toolbarLists", "true").equals("true")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                out.write("<span class=\"kupu-tb-buttongroup\">"); //$NON-NLS-1$
                // kupu note: list button events are set on the list tool
                out.write("<button type=\"button\" class=\"kupu-insertorderedlist\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.numberedlist") //$NON-NLS-1$
                        + "\" id=\"kupu-list-ol-addbutton\"></button>"); //$NON-NLS-1$
                out.write("<button type=\"button\" class=\"kupu-insertunorderedlist\" title=\"" //$NON-NLS-1$
                        + msgs.get("dialog.richedit.unorderedlist") //$NON-NLS-1$
                        + "\" id=\"kupu-list-ul-addbutton\"></button>"); //$NON-NLS-1$
                out.write("</span>"); //$NON-NLS-1$
                out.write("<select id=\"kupu-ulstyles\" class=\"" + CssConstants.CSSCLASS_SELECT + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                out.write("  <option value=\"disc\">Disc</option>"); //$NON-NLS-1$
                out.write("  <option value=\"square\">Square</option>"); //$NON-NLS-1$
                out.write("  <option value=\"circle\">Circle</option>"); //$NON-NLS-1$
                out.write("  <option value=\"none\">no bullet</option>"); //$NON-NLS-1$
                out.write("</select>"); //$NON-NLS-1$
                out.write("<select id=\"kupu-olstyles\" class=\"" + CssConstants.CSSCLASS_SELECT + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                out.write("  <option value=\"decimal\">1</option>"); //$NON-NLS-1$
                out.write("  <option value=\"upper-roman\">I</option>"); //$NON-NLS-1$
                out.write("  <option value=\"lower-roman\">i</option>"); //$NON-NLS-1$
                out.write("  <option value=\"upper-alpha\">A</option>"); //$NON-NLS-1$
                out.write("  <option value=\"lower-alpha\">a</option>"); //$NON-NLS-1$
                out.write("</select>"); //$NON-NLS-1$
            }
            out.write("</span>"); //$NON-NLS-1$
            out.write("</div>"); //$NON-NLS-1$
            // #################
            // END toolbar
            // #################
            // color palette
            out
                    .write("<div id=\"kupu-colorchooser\" style=\"position: fixed; border-style: solid; border-color: #666666; border-width: 1px;\"> </div>"); //$NON-NLS-1$
            // #################
            // iframe
            // #################
            out.write("<iframe id=\"" + this.getName() + "-kupu-editor\""); //$NON-NLS-1$ //$NON-NLS-2$
            out.write(" class=\"" + CssConstants.CSSCLASS_RICHEIFRAME + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            if (this.getConfigValue("height", null) != null) { //$NON-NLS-1$
                out.write(" style=\"height:" + this.getConfigValue("height") + ";\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            out.write(" frameborder=\"0\""); //$NON-NLS-1$
            out.write(" src=\"" //$NON-NLS-1$
                    + this.getRequest().getContextPath()
                    + "/.magnolia/pages/richEIFrame.html?" //$NON-NLS-1$
                    + SESSION_ATTRIBUTENAME_DIALOGOBJECT
                    + "=" //$NON-NLS-1$
                    + this.getConfigValue(SESSION_ATTRIBUTENAME_DIALOGOBJECT)
                    + "&amp;mgnlCK=" //$NON-NLS-1$
                    + new Date().getTime()
                    + "\""); //$NON-NLS-1$
            out.write(" reloadsrc=\"0\""); //$NON-NLS-1$
            out.write(" usecss=\"1\""); //$NON-NLS-1$
            out.write(" strict_output=\"1\""); //$NON-NLS-1$
            out.write(" content_type=\"application/xhtml+xml\""); //$NON-NLS-1$
            out.write(" scrolling=\"auto\""); //$NON-NLS-1$
            out.write("></iframe>"); //$NON-NLS-1$
            out.write("<script type=\"text/javascript\">"); //$NON-NLS-1$
            out.write("mgnlRichEditors[mgnlRichEditors.length]='" + this.getName() + "';"); //$NON-NLS-1$ //$NON-NLS-2$
            out.write("</script>"); //$NON-NLS-1$
            // #################
            // END iframe
            // #################
            // #################
            // textarea to save data (data will be put into textarea on submit of form)
            // #################
            out.write("<div style=visibility:hidden;position:absolute;top:0px;left:-500px;>"); //$NON-NLS-1$
            Edit hiddenTextarea = new Edit(this.getName(), StringUtils.EMPTY);
            hiddenTextarea.setRows("5"); //$NON-NLS-1$
            // special handling during saving
            hiddenTextarea.setIsRichEditValue(ControlSuper.RICHEDIT_KUPU);
            out.write(hiddenTextarea.getHtml());
            out.write("</div>"); //$NON-NLS-1$

        } else {
            // rich edit not supported: draw textarea
            Edit control = new Edit(this.getName(), this.getValue());
            control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
            if (this.getConfigValue("saveInfo").equals("false")) { //$NON-NLS-1$ //$NON-NLS-2$
                control.setSaveInfo(false);
            }
            control.setCssClass(CssConstants.CSSCLASS_EDIT);
            control.setRows(this.getConfigValue("rows", "18")); //$NON-NLS-1$ //$NON-NLS-2$
            control.setCssStyles("width", this.getConfigValue("width", "100%")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            out.write(control.getHtml());

        }
        this.drawHtmlPost(out);
    }

    public void drawHtmlEditor(Writer out) throws IOException {

        out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "); //$NON-NLS-1$
        out.write(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"); //$NON-NLS-1$

        out.write("<html><head>"); //$NON-NLS-1$
        // headers to prevent the browser from caching, these *must* be provided,
        // either in meta-tag form or as HTTP headers
        out.write("<meta http-equiv=\"Pragma\" content=\"no-cache\" />"); //$NON-NLS-1$
        out.write("<meta http-equiv=\"Cache-Control\" content=\"no-cache, must-revalidate\" />"); //$NON-NLS-1$
        out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); //$NON-NLS-1$
        out.write("<meta name=\"Effective_date\" content=\"None\" />"); //$NON-NLS-1$
        out.write("<meta name=\"Expiration_date\" content=\"None\" />"); //$NON-NLS-1$
        out.write("<meta name=\"Type\" content=\"Document\" />"); //$NON-NLS-1$
        // out.write("<meta name=\"Format\" content=\"text/html\" />");
        out.write("<meta name=\"Language\" content=\"\" />"); //$NON-NLS-1$
        out.write("<meta name=\"Rights\" content=\"\" />"); //$NON-NLS-1$
        out.write("<style type=\"text/css\">"); //$NON-NLS-1$
        out.write("body {font-family:verdana;font-size:11px;background-color:#ffffff;}"); //$NON-NLS-1$
        out.write("</style>"); //$NON-NLS-1$
        if (this.getConfigValue("cssFile", null) != null) { //$NON-NLS-1$
            out.write("<link href=\"" //$NON-NLS-1$
                    + this.getConfigValue("cssFile") + "\" rel=\"stylesheet\" type=\"text/css\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        out.write("<script type=\"text/javascript\">\n"); //$NON-NLS-1$
        out.write("document.insertText=function(value)\n"); //$NON-NLS-1$
        out.write(" {\n"); //$NON-NLS-1$
        out.write(" while (value.indexOf('\\n')!=-1)\n"); //$NON-NLS-1$
        out.write(" {\n"); //$NON-NLS-1$
        out.write(" value=value.replace('\\n','<br />');\n"); //$NON-NLS-1$
        out.write(" }\n"); //$NON-NLS-1$
        out.write(" var body=document.getElementsByTagName('body');\n"); //$NON-NLS-1$
        out.write(" value=body[0].innerHTML+value;\n"); //$NON-NLS-1$
        out.write(" body[0].innerHTML=value;\n"); //$NON-NLS-1$
        out.write(" }\n"); //$NON-NLS-1$
        out.write("</script>\n"); //$NON-NLS-1$
        out.write("</head>\n"); //$NON-NLS-1$
        out.write("<body>"); //$NON-NLS-1$
        out.write(this.getValue());
        out.write("</body></html>"); //$NON-NLS-1$

    }

    public String getValue(String lineBreak) {
        String value = this.getValue();

        if (value != null) {
            return value.replaceAll("\n", "<br />"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (this.getWebsiteNode() != null) {
            return this.getWebsiteNode().getNodeData(this.getName()).getString(lineBreak);
        } else {
            return StringUtils.EMPTY;
        }
    }
}
