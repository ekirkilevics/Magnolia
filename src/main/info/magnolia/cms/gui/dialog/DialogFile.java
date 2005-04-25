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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.File;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.i18n.MessagesManager;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogFile extends DialogBox {

    private static Logger log = Logger.getLogger(DialogFile.class);

    private List imageExtensions = new ArrayList();

    /**
     * Empty constructor should only be used by DialogFactory.
     */
    protected DialogFile() {
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        initImageExtensions();
        initIconExtensions();
    }

    public List getImageExtensions() {
        return this.imageExtensions;
    }

    public void setImageExtensions(List l) {
        this.imageExtensions = l;
    }

    public void initImageExtensions() {
        this.getImageExtensions().add("jpg");
        this.getImageExtensions().add("jpeg");
        this.getImageExtensions().add("gif");
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        File control = new File(this.getName(), this.getWebsiteNode());
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING));
        control.setSaveInfo(false); // set manualy below
        control.setCssClass(CssConstants.CSSCLASS_FILE);
        control.setCssClassFileName(CssConstants.CSSCLASS_EDIT);
        control.setCssStyles("width", this.getConfigValue("width", "100%"));

        this.drawHtmlPre(out);

        String width = this.getConfigValue("width", "100%");
        boolean showImage = false;
        if (this.getImageExtensions().contains(control.getExtension().toLowerCase())) {
            showImage = true;
        }
        String htmlControlBrowse = control.getHtmlBrowse();
        StringBuffer htmlControlFileName = new StringBuffer();
        htmlControlFileName.append("<span class=\"" + CssConstants.CSSCLASS_DESCRIPTION + "\">" + MessagesManager.get(this.getRequest(),"dialog.file.filename") + "</span>");
        htmlControlFileName.append(Spacer.getHtml(1, 1));
        htmlControlFileName.append(control.getHtmlFileName()
            + "<span id=\""
            + this.getName()
            + "_fileNameExtension\">."
            + control.getExtension()
            + "</span>");
        String htmlContentEmpty = htmlControlBrowse + Spacer.getHtml(0, 0) + htmlControlFileName;
        out.write("<div id=\"" + this.getName() + "_contentDiv\" style=\"width:100%;\">");
        boolean exists = false;

        if (this.getWebsiteNode() != null) {
            exists = this.getWebsiteNode().getNodeData(this.getName()).isExist();
        }

        if (!exists) {
            out.write(htmlContentEmpty);
            out.write("</div>");
        }
        else {
            if (showImage) {
                out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"" + width + "\">");
                out.write("<tr><td class=\"" + CssConstants.CSSCLASS_FILEIMAGE + "\">");
                // todo: image thumbnail template
                // out.write("<img src=\""+ this.getRequest().getContextPath()
                // +THUMB_PATH+"?src="+control.getHandle()+"\"
                // class=\""+CSSCLASS_FILEIMAGE+"\">");
                // tmp workaround: resize in html ...
                out.write("<img width=\"150\" src=\""
                    + this.getRequest().getContextPath()
                    + control.getHandle()
                    + "\" class=\""
                    + CssConstants.CSSCLASS_FILEIMAGE
                    + "\">");
                out.write("</td><td>");
            }
            out.write(htmlControlFileName.toString());
            if (!showImage) {
                String iconPath = this.getIconPath(control.getExtension());
                /*
                 * String iconPath=ICONS_PATH+ICONS_GENERAL; if
                 * (this.getIconExtensions().containsKey(control.getExtension().toLowerCase())) { iconPath=(String)
                 * this.getIconExtensions().get(control.getExtension().toLowerCase()); if (iconPath.equals(""))
                 * iconPath=ICONS_PATH+control.getExtension().toLowerCase()+".gif"; }
                 */
                out.write(Spacer.getHtml(0, 0));
                out.write("<a href=" + control.getPath() + " target=\"_blank\">");
                out.write("<img src=\""
                    + this.getRequest().getContextPath()
                    + iconPath
                    + "\" class=\""
                    + CssConstants.CSSCLASS_FILEICON
                    + "\" border=\"0\">");
                out.write(control.getFileName() + "." + control.getExtension() + "</a>");
            }
            out.write(Spacer.getHtml(12, 12));
            out.write(control.getHtmlRemove("mgnlDialogFileRemove('" + this.getName() + "');"));
            if (showImage) {
                out.write("</td></tr></table>");
            }
            out.write("</div>");
            out.write("<div style=\"position:absolute;top:-500;left:-500;visibility:hidden;\"><textarea id=\""
                + this.getName()
                + "_contentEmpty\">"
                + htmlContentEmpty
                + "</textarea></div>");
        }
        control.setSaveInfo(true);
        out.write(control.getHtmlSaveInfo());
        control.setNodeDataTemplate(this.getConfigValue("nodeDataTemplate", null));
        out.write(control.getHtmlNodeDataTemplate());
        this.drawHtmlPost(out);
    }
}
