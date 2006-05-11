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
package info.magnolia.module.dms.gui;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.control.Edit;
import info.magnolia.cms.gui.control.File;
import info.magnolia.cms.gui.dialog.DialogFile;
import info.magnolia.cms.gui.misc.CssConstants;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.dms.beans.Document;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * Different rendering than in the normal upload control.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSDialogFileControl extends DialogFile {

    private Document doc;

    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        this.doc = Document.getCurrent(request);
    }

    public void drawHtml(Writer out) throws IOException {
        File control = getFileControl();

        control.setType(this.getConfigValue("type", PropertyType.TYPENAME_STRING));
        control.setSaveInfo(false); // set manualy below
        control.setCssClass(CssConstants.CSSCLASS_FILE);
        control.setCssClassFileName(CssConstants.CSSCLASS_EDIT);
        control.setCssStyles("width", this.getConfigValue("width", "100%"));

        this.drawHtmlPre(out);

        String htmlControlBrowse = control.getHtmlBrowse();
        StringBuffer htmlControlFileName = new StringBuffer();
        // create the edit inboxes
        htmlControlFileName.append(this.getHtmlFileName());
        htmlControlFileName.append(".");
        htmlControlFileName.append(this.getHtmlFileExtension());

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
            out.write(Spacer.getHtml(0, 0));
            out.write("<a href=\"" + this.getRequest().getContextPath() + this.getLink() + "\">");
            out.write("<img src=\""
                + this.getRequest().getContextPath()
                + doc.getMimeTypeIcon()
                + "\" class=\""
                + CssConstants.CSSCLASS_FILEICON
                + "\" border=\"0\" width=\"23\" height=\"16\">");
            out.write(doc.getFileName() + "." + doc.getFileExtension() + "</a>");
            out.write("<p/>");
            out.write(htmlControlFileName.toString());
            out.write(Spacer.getHtml(12, 12));
            out.write(this.getHtmlRemove("mgnlDialogFileRemove('" + this.getName() + "');"));

            out.write("</div>");
            out.write("<input type=\"hidden\" id=\""
                + this.getName()
                + "_contentEmpty\" value=\""
                + ControlSuper.escapeHTML(htmlContentEmpty)
                + "\">");

        }
        control.setSaveInfo(true);
        out.write(control.getHtmlSaveInfo());
        control.setNodeDataTemplate(this.getConfigValue("nodeDataTemplate", null));
        out.write(control.getHtmlNodeDataTemplate());
        this.drawHtmlPost(out);
    }

    private String getLink() {
        String path = "/dms" + doc.getPath() + "/" + doc.getFileName() + "." + doc.getFileExtension();
        if (StringUtils.isNotEmpty(doc.getVersion())) {
            path += "?mgnlVersion=" + doc.getVersion();
        }
        return path;
    }

    private String getHtmlFileName() {
        Edit control = new Edit(this.getName() + "_" + FileProperties.PROPERTY_FILENAME, doc.getFileName()); //$NON-NLS-1$
        control.setSaveInfo(false);
        control.setCssClass(CssConstants.CSSCLASS_EDIT);
        control.setCssStyles("width", "45%"); //$NON-NLS-1$ //$NON-NLS-2$
        return control.getHtml();
    }

    private String getHtmlFileExtension() {
        // this will not get saved by the save control. we must do a hack in the dialog
        Edit control = new Edit(this.getName() + "_" + FileProperties.PROPERTY_EXTENSION, doc.getFileExtension()); //$NON-NLS-1$
        control.setSaveInfo(false);
        control.setCssClass(CssConstants.CSSCLASS_EDIT);
        control.setCssStyles("width", "40"); //$NON-NLS-1$ //$NON-NLS-2$
        return control.getHtml();
    }

    public String getHtmlRemove(String additionalOnclick) {
        Button control1 = new Button();
        control1.setLabel(MessagesManager.getMessages("info.magnolia.module.dms.messages").get("dms.edit.uploadNewVersion")); //$NON-NLS-1$
        control1.setCssClass("mgnlControlButtonSmall"); //$NON-NLS-1$
        control1.setOnclick(additionalOnclick + "mgnlControlFileRemove('" + this.getName() + "')"); //$NON-NLS-1$ //$NON-NLS-2$
        return control1.getHtml();
    }

}
