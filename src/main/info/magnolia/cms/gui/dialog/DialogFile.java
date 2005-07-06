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
        this.getImageExtensions().add("jpg"); //$NON-NLS-1$
        this.getImageExtensions().add("jpeg"); //$NON-NLS-1$
        this.getImageExtensions().add("gif"); //$NON-NLS-1$
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogInterface#drawHtml(Writer)
     */
    public void drawHtml(Writer out) throws IOException {
        File control = getFileControl();
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
        control.setSaveInfo(false); // set manualy below
        control.setCssClass(CssConstants.CSSCLASS_FILE);
        control.setCssClassFileName(CssConstants.CSSCLASS_EDIT);
        control.setCssStyles("width", this.getConfigValue("width", "100%")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        this.drawHtmlPre(out);

        String width = this.getConfigValue("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
        boolean showImage = false;
        if (this.getImageExtensions().contains(control.getExtension().toLowerCase())) {
            showImage = true;
        }
        String htmlControlBrowse = control.getHtmlBrowse();
        StringBuffer htmlControlFileName = new StringBuffer();
        htmlControlFileName.append("<span class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_DESCRIPTION + "\">" //$NON-NLS-1$
            + MessagesManager.get(this.getRequest(), "dialog.file.filename") //$NON-NLS-1$
            + "</span>"); //$NON-NLS-1$
        htmlControlFileName.append(Spacer.getHtml(1, 1));
        htmlControlFileName.append(control.getHtmlFileName() + "<span id=\"" //$NON-NLS-1$
            + this.getName() + "_fileNameExtension\">." //$NON-NLS-1$
            + control.getExtension() + "</span>"); //$NON-NLS-1$
        String htmlContentEmpty = htmlControlBrowse + Spacer.getHtml(0, 0) + htmlControlFileName;
        out.write("<div id=\"" + this.getName() + "_contentDiv\" style=\"width:100%;\">"); //$NON-NLS-1$ //$NON-NLS-2$
        boolean exists = false;

        if (this.getWebsiteNode() != null) {
            exists = this.getWebsiteNode().getNodeData(this.getName()).isExist();
        }

        if (!exists) {
            out.write(htmlContentEmpty);
            out.write("</div>"); //$NON-NLS-1$
        }
        else {
            if (showImage) {
                out.write("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"" + width + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                out.write("<tr><td class=\"" + CssConstants.CSSCLASS_FILEIMAGE + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                // todo: image thumbnail template
                // out.write("<img src=\""+ this.getRequest().getContextPath()
                // +THUMB_PATH+"?src="+control.getHandle()+"\"
                // class=\""+CSSCLASS_FILEIMAGE+"\">");
                // tmp workaround: resize in html ...
                out.write("<img width=\"150\" src=\"" //$NON-NLS-1$
                    + this.getRequest().getContextPath() + control.getHandle() + "\" class=\"" //$NON-NLS-1$
                    + CssConstants.CSSCLASS_FILEIMAGE + "\">"); //$NON-NLS-1$
                out.write("</td><td>"); //$NON-NLS-1$
            }
            out.write(htmlControlFileName.toString());
            if (!showImage) {
                String iconPath = this.getIconPath(control.getExtension());

                out.write(Spacer.getHtml(0, 0));
                out.write("<a href=" + this.getRequest().getContextPath() + control.getPath() + " target=\"_blank\">"); //$NON-NLS-1$ //$NON-NLS-2$
                out.write("<img src=\"" //$NON-NLS-1$
                    + this.getRequest().getContextPath() + iconPath + "\" class=\"" //$NON-NLS-1$
                    + CssConstants.CSSCLASS_FILEICON + "\" border=\"0\">"); //$NON-NLS-1$
                out.write(control.getFileName() + "." + control.getExtension() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            out.write(Spacer.getHtml(12, 12));
            out.write(control.getHtmlRemove("mgnlDialogFileRemove('" + this.getName() + "');")); //$NON-NLS-1$ //$NON-NLS-2$
            if (showImage) {
                out.write("</td></tr></table>"); //$NON-NLS-1$
            }
            out.write("</div>"); //$NON-NLS-1$
            out.write("<div style=\"position:absolute;top:-500;left:-500;visibility:hidden;\"><textarea id=\"" //$NON-NLS-1$
                + this.getName() + "_contentEmpty\">" //$NON-NLS-1$
                + htmlContentEmpty + "</textarea></div>"); //$NON-NLS-1$
        }
        control.setSaveInfo(true);
        out.write(control.getHtmlSaveInfo());
        control.setNodeDataTemplate(this.getConfigValue("nodeDataTemplate", null)); //$NON-NLS-1$
        out.write(control.getHtmlNodeDataTemplate());
        this.drawHtmlPost(out);
    }

    /**
     * @return
     */
    protected File getFileControl() {
        File control = new File(this.getName(), this.getWebsiteNode());
        return control;
    }
}
