/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.File;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.Spacer;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogFile extends DialogBox {

    private List imageExtensions = new ArrayList();

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#init(HttpServletRequest, HttpServletResponse, Content, Content)
     */
    @Override
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        initImageExtensions();
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
        this.getImageExtensions().add("png"); //$NON-NLS-1$
        this.getImageExtensions().add("bpm"); //$NON-NLS-1$
        this.getImageExtensions().add("swf"); //$NON-NLS-1$
    }

    @Override
    public void drawHtml(Writer out) throws IOException {
        File control = getFileControl();
        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING)); //$NON-NLS-1$
        control.setSaveInfo(false); // set manualy below
        control.setCssClass(CssConstants.CSSCLASS_FILE);
        control.setCssClassFileName(CssConstants.CSSCLASS_EDIT);
        control.setCssStyles("width", this.getConfigValue("width", "100%")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        this.drawHtmlPre(out);

        String width = this.getConfigValue("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$


        final boolean preview = Boolean.valueOf(getConfigValue("preview", "true")).booleanValue();
        final boolean extensionIsDisplayableImage = this.getImageExtensions().contains(control.getExtension().toLowerCase());
        final boolean showImage = extensionIsDisplayableImage && preview;

        StringBuffer htmlControlFileName = getHtmlControlFileName(control);
        String htmlContentEmpty = control.getHtmlBrowse() + Spacer.getHtml(0, 0) + htmlControlFileName;
        out.write("<div id=\"" + this.getName() + "_contentDiv\" style=\"width:100%;\">"); //$NON-NLS-1$ //$NON-NLS-2$
        boolean exists = false;

        if (this.getStorageNode() != null) {
            exists = this.getStorageNode().getNodeData(this.getName()).isExist();
        }

        if (!exists) {
            out.write(htmlContentEmpty);
            out.write("</div>"); //$NON-NLS-1$
        }
        else {
            String link = getLink(control);

            if (showImage) {

                out.write("\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"" + width + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
                out.write("<tr><td class=\"" + CssConstants.CSSCLASS_FILEIMAGE + "\">"); //$NON-NLS-1$ //$NON-NLS-2$

                if ("swf".equals(control.getExtension().toLowerCase())) {

                    // flash movie
                    out.write("<object type=\"application/x-shockwave-flash\" data=\"");
                    out.write(link);
                    out.write("\" title=\"");
                    out.write(control.getFileName());
                    out.write("\" ");
                    out.write("width=\"150\" ");
                    out.write("height=\"100\" ");
                    out.write(">");

                    out.write("<param name=\"movie\" value=\"");
                    out.write(link);
                    out.write("\"/>");

                    out.write("</object>\n");

                }
                else {
                    // standard image

                    // todo: image thumbnail template
                    // out.write("<img src=\""+ this.getRequest().getContextPath()
                    // +THUMB_PATH+"?src="+control.getHandle()+"\"
                    // class=\""+CSSCLASS_FILEIMAGE+"\">");
                    // tmp workaround: resize in html ...

                    int imgwidth = 150;
                    int imgheight = 150;

                    try{
                        imgwidth = Integer.parseInt(control.getImageWidth());
                        imgheight = Integer.parseInt(control.getImageHeight());
                    }
                    catch(NumberFormatException e){
                        // ignore (is 150)
                    }

                    // resize if to big
                    if(imgwidth > imgheight && imgwidth > 150){
                        imgheight = (int)(150.0/imgwidth * imgheight);
                        imgwidth = 150;
                    }
                    else if(imgheight > imgwidth && imgheight > 150){
                        imgwidth = (int)(150.0/imgheight * imgwidth);
                        imgheight = 150;
                    }

                    out.write("<img width=\"" + imgwidth + "\" height=\"" + imgheight + "\"src=\""); //$NON-NLS-1$
                    out.write(link);
                    out.write("\" class=\""); //$NON-NLS-1$
                    out.write(CssConstants.CSSCLASS_FILEIMAGE);
                    out.write("\" alt=\""); //$NON-NLS-1$
                    out.write(control.getFileName());
                    out.write("\" title=\""); //$NON-NLS-1$
                    out.write(control.getFileName());
                    out.write("\" />\n"); //$NON-NLS-1$

                    if (StringUtils.isNotEmpty(control.getImageWidth())) {
                        out.write("<em style='white-space:nowrap'>"); //$NON-NLS-1$

                        out.write("width: "); //$NON-NLS-1$
                        out.write(control.getImageWidth());

                        out.write(" height: "); //$NON-NLS-1$
                        out.write(control.getImageHeight());

                        out.write("</em>\n"); //$NON-NLS-1$
                    }

                }

                out.write("</td><td>"); //$NON-NLS-1$
            }
            writeInnerHtml(out, showImage, control, htmlControlFileName, link);

            out.write(Spacer.getHtml(12, 12));
            out.write(control.getHtmlRemove("mgnlDialogFileRemove('" + this.getName() + "');")); //$NON-NLS-1$ //$NON-NLS-2$
            if (showImage) {
                out.write("</td></tr></table>"); //$NON-NLS-1$
            }
            out.write("</div>\n"); //$NON-NLS-1$
            out.write("<div style=\"position:absolute;top:-500px;left:-500px;visibility:hidden;\">\n<textarea id=\""); //$NON-NLS-1$
            out.write(this.getName());
            out.write("_contentEmpty\">");//$NON-NLS-1$
            out.write(htmlContentEmpty);

            // @todo should be escaped, but we need to test it
            // out.write(StringEscapeUtils.escapeXml(htmlContentEmpty));
            out.write("</textarea>\n</div>\n"); //$NON-NLS-1$
        }
        control.setSaveInfo(true);
        out.write(control.getHtmlSaveInfo());
        control.setNodeDataTemplate(this.getConfigValue("nodeDataTemplate", null)); //$NON-NLS-1$
        out.write(control.getHtmlNodeDataTemplate());
        this.drawHtmlPost(out);
    }

    protected String getLink(File control) {
        String link = this.getRequest().getContextPath() + getFileURI(control);
        if (!StringUtils.isEmpty(control.getExtension())) {
            link += "." + control.getExtension();
        }
        return link;
    }

    protected void writeInnerHtml(Writer out, final boolean showImage, File control, StringBuffer htmlControlFileName, String link) throws IOException {
        out.write(htmlControlFileName.toString());
        if (!showImage) {
            String iconPath = MIMEMapping.getMIMETypeIcon(control.getExtension());

            out.write(Spacer.getHtml(0, 0));

            out.write("<a href=");
            out.write(link);
            out.write(" target=\"_blank\">"); //$NON-NLS-1$ //$NON-NLS-2$

            out.write("<img src=\"" //$NON-NLS-1$
                + this.getRequest().getContextPath()
                + iconPath
                + "\" class=\"" //$NON-NLS-1$
                + CssConstants.CSSCLASS_FILEICON
                + "\" border=\"0\">"); //$NON-NLS-1$
            out.write(control.getFileName() + "." + control.getExtension() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    protected StringBuffer getHtmlControlFileName(File control) {
        StringBuffer htmlControlFileName = new StringBuffer();
        htmlControlFileName.append("<span class=\"" //$NON-NLS-1$
            + CssConstants.CSSCLASS_DESCRIPTION
            + "\">" //$NON-NLS-1$
            + getMessage("dialog.file.filename") //$NON-NLS-1$
            + "</span>"); //$NON-NLS-1$
        htmlControlFileName.append(Spacer.getHtml(1, 1));
        htmlControlFileName.append(control.getHtmlFileName() + "<span id=\"" //$NON-NLS-1$
            + this.getName()
            + "_fileNameExtension\">." //$NON-NLS-1$
            + control.getExtension()
            + "</span>"); //$NON-NLS-1$
        return htmlControlFileName;
    }

    @Override
    public boolean validate() {
        if (isRequired()) {
            // if we have a form, then this is going to the database
            MultipartForm form = (MultipartForm) getRequest().getAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME);
            if (form != null) {
                Document doc = form.getDocument(getName());
                if (doc != null) { // we're submitting a document for this required field
                    return true;
                }
                // we're removing the document
                // for this required field but
                // not uploading one
                if (form.getParameter(getName() + "_" + File.REMOVE) != null) {
                    setValidationMessage("dialogs.validation.required");
                    return false;
                }
            }
            // we are not uploading or removing
            // check if there is a binary stored
            if(this.getStorageNode() == null || !getStorageNode().getNodeData(getName()).isExist()){
                setValidationMessage("dialogs.validation.required");
                return false;
            }
        }
        return true;
    }

    /**
     * Get the uri of the file (used to show images)
     * @param control
     * @return
     */
    protected String getFileURI(File control) {
        return control.getHandle();
    }

    /**
     * Configures the inner file upload control
     */
    protected File getFileControl() {
        File control = new File(this.getName(), this.getStorageNode());
        return control;
    }
}
