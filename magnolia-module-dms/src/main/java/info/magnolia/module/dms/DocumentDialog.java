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
package info.magnolia.module.dms;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.JSPIncludeUtil;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;
import info.magnolia.module.dms.beans.Document;
import info.magnolia.module.dms.util.PathUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Handles the document upload and adds some special properties (for searching)
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DocumentDialog extends ConfiguredDialog {

    protected static Logger log = Logger.getLogger(DocumentDialog.class);

    private boolean create;

    private String version;

    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public DocumentDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
        this.version = request.getParameter("mgnlVersion");
    }

    /**
     * Add the comment popup to the dialog
     */
    protected Dialog createDialog(Content configNode, Content storageNode) throws RepositoryException {
        // get the version node
        Document doc = new Document(storageNode, version);
        // set this document for some of the subcontrols
        Document.setCurrent(request, doc);

        // execute the js init code
        Dialog dialog = new Dialog() {

            protected void drawHtmlPreSubsHead(Writer out) throws IOException {
                super.drawHtmlPreSubsHead(out);
                out.write("<script type=\"text/javascript\">");
                out.write("mgnl.dms.DMSDialog.init();");
                out.write("</script>");
            }
        };

        if (StringUtils.isNotEmpty(this.version)) {
            storageNode = storageNode.getVersionedContent(this.version);
        }

        dialog.init(request, response, storageNode, configNode);

        dialog.addSub(new DialogControlImpl() {

            public void drawHtml(Writer out) throws IOException {
                String str = "";
                try {
                    str = JSPIncludeUtil.get("/admintemplates/dms/versionCommentPopUp.jsp", request, response);
                }
                catch (Exception e) {
                    log.error("can't get comment popup", e);
                }
                out.write(str);
            }
        });

        if (StringUtils.isNotEmpty(this.version)) {
            dialog.setConfig("cancelLabel", "Close");
        }

        return dialog;
    }

    /**
     * Overriden to force creation if the node does not exist
     */
    protected boolean onPreSave(SaveHandler handler) {
        // check if this is a creation
        this.create = this.nodeName.equals("mgnlNew");

        if (this.create) {
            // get the new name of the node
            info.magnolia.cms.beans.runtime.Document documentMaybeNull = form.getDocument("document");
            String name = documentMaybeNull != null ? documentMaybeNull.getFileName() : "file";

            name = Path.getValidatedLabel(name);
            if (name.matches("^-*$")) {
                name = "file";
            }

            name = Path.getUniqueLabel(hm, path, name);
            handler.setNodeName(name);
            handler.setCreate(true);
            handler.setCreationItemType(ItemType.CONTENTNODE);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#onPostSave(info.magnolia.cms.gui.control.Save)
     */
    protected boolean onPostSave(SaveHandler handler) {
        super.onPostSave(handler);
        Content node = this.getStorageNode();

        Document doc = new Document(node);

        try {
            // update the extension if changed
            if (!form.getParameter("document_" + FileProperties.PROPERTY_EXTENSION).equals(doc.getFileExtension())) {
                doc.setFileExtension(form.getParameter("document_" + FileProperties.PROPERTY_EXTENSION));
            }
            doc.updateMetaData();
            doc.save();
        }
        catch (Exception e) {
            log.error("can't update the data", e);
        }

        // create version
        try {
            // create Version
            Version v = doc.addVersion();
            log.info("add version: " + v.getName());
        }
        catch (Exception e) {
            log.error("faild to add a version to " + node.getHandle(), e);
        }
        return true;
    }

    /**
     * do not reload the tree
     */
    public void renderHtml(String view) throws IOException {
        PrintWriter out = response.getWriter();

        // after saving
        if (view == VIEW_CLOSE_WINDOW) {
            out.println("<html>");
            out.println(new Sources(request.getContextPath()).getHtmlJs());
            out.println("<script type=\"text/javascript\">");
            out.println("opener.mgnl.dms.DMS.reloadAfterEdit('" + this.path + "')");
            out.println("window.close();");
            out.println("</script></html>");
        }

        // show the created dialog
        else {
            super.renderHtml(view);
            // do not show the save button if this is an old version
            // or a not writeable node
            if (StringUtils.isNotEmpty(this.version)
                || !(this.nodeName.equals("mgnlNew") || this.getStorageNode().isGranted(Permission.WRITE))) {
                // if this is view only
                out.println("<script type=\"text/javascript\">mgnl.dms.DMSDialog.hideSaveButton()</script>");
            }
        }
    }
}
