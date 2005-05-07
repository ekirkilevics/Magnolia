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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.control.Save;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.servlets.MVCServletHandlerImpl;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * This is the MVCHandler for dialogs. You can make a subclass to take influence on creation or saving.
 * @author Philipp Bracher
 * @version $Id$
 */

public class DialogMVCHandler extends MVCServletHandlerImpl {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(MVCServletHandlerImpl.class);

    /*
     * Commands
     */
    protected static final String COMMAND_SAVE = "save";

    protected static final String COMMAND_SELECT_PARAGRAPH = "selectParagraph";

    protected static final String COMMAND_SHOW_DIALOG = "showDialog";

    /*
     * Views
     */
    protected static final String VIEW_CLOSE_WINDOW = "close";

    protected static final String VIEW_SHOW_DIALOG = "show";

    /**
     * the request passed by the MVCServlet
     */
    protected HttpServletRequest request;

    /**
     * The repsonse passed by the MVCServlet
     */
    protected HttpServletResponse response;

    /**
     * The posted multipart form. Use params for easy access.
     */
    protected MultipartForm form;

    /**
     * Path to the node containing the data
     */
    protected String path = "";

    /**
     * If the dialog serves a collection (multiple instances of the same dialog)
     */
    private String nodeCollectionName = "";

    /**
     * the node containing the date for this dialog
     */
    protected String nodeName = "";

    protected String richE = "";

    protected String richEPaste = "";

    protected String repository = "";

    protected HierarchyManager hm;

    protected DialogDialog dialog;

    protected info.magnolia.cms.i18n.Messages msgs;

    protected RequestFormUtil params;

    private Content storageNode;

    /**
     * Initialize the used parameters: path, nodeCollectionName, nodeName, ..
     * @param request
     * @param response
     */
    public DialogMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        this.request = request;
        this.response = response;

        form = Resource.getPostedForm(request);

        params = new RequestFormUtil(request, form);

        path = params.getParameter("mgnlPath");
        nodeCollectionName = params.getParameter("mgnlNodeCollection");
        nodeName = params.getParameter("mgnlNode");
        richE = params.getParameter("mgnlRichE");
        richEPaste = params.getParameter("mgnlRichEPaste");
        repository = params.getParameter("mgnlRepository", getRepository());

        hm = SessionAccessControl.getHierarchyManager(request, repository);
        msgs = MessagesManager.getMessages(request);
    }

    /*
     * @see info.magnolia.cms.servlets.MVCServletHandler#getCommand()
     */
    public String getCommand() {
        if (form != null && form.getParameter("mgnlParagraphSelected") == null) {
            return COMMAND_SAVE;
        }
        else {
            return COMMAND_SHOW_DIALOG;
        }
    }

    /**
     * Calls createDialog and sets the common parameters on the dialog
     * @return
     */
    public String showDialog() {
        Content configNode = getConfigNode();
        Content storageNode = getStorageNode();

        try {
            dialog = createDialog(configNode, storageNode);
        }
        catch (RepositoryException e) {
            log.error("can't instantiate dialog", e);
        }

        dialog.setConfig("dialog", getName());
        dialog.setConfig("path", path);
        dialog.setConfig("nodeCollection", nodeCollectionName);
        dialog.setConfig("node", nodeName);
        dialog.setConfig("richE", richE);
        dialog.setConfig("richEPaste", richEPaste);
        dialog.setConfig("repository", repository);

        return VIEW_SHOW_DIALOG;
    }

    /**
     * Is called during showDialog(). Here can you create/ add controls for the dialog.
     * @param configNode
     * @param storageNode
     * @throws RepositoryException
     */
    protected DialogDialog createDialog(Content configNode, Content storageNode) throws RepositoryException {
        return DialogFactory.getDialogDialogInstance(request, response, storageNode, configNode);
    }

    /**
     * Uses the SaveControl. Override to take influence.
     * @return close view name
     */
    public String save() {
        Save control = onPreSave();
        onSave(control);
        onPostSave(control);
        return VIEW_CLOSE_WINDOW;
    }

    protected Save onPreSave() {
        return new Save(form, request);
    }

    protected void onSave(Save control) {
        control.save();
    }

    protected void onPostSave(Save control) {
    }

    /**
     * Defines the node/page containing the data editing in this dialog. The default implementation is using the path
     * parameter
     */
    protected Content getStorageNode() {
        if (storageNode == null) {
            try {
                Content parentContent = hm.getContent(path);
                if (StringUtils.isEmpty(nodeName)) {
                    storageNode = parentContent;
                }
                else {
                    if (StringUtils.isEmpty(nodeCollectionName)) {
                        storageNode = parentContent.getContent(nodeName);

                    }
                    else {
                        storageNode = parentContent.getContent(nodeCollectionName).getContent(nodeName);

                    }
                }
            }
            catch (RepositoryException re) {
                // content does not exist yet

            }
        }
        return storageNode;
    }

    /**
     * Returns the node with the dialog definition. Default: null
     * @return
     */
    protected Content getConfigNode() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.servlets.MVCServletHandler#renderHtml(java.lang.String)
     */
    public void renderHtml(String view) throws IOException {
        PrintWriter out = response.getWriter();

        // after saving
        if (view == VIEW_CLOSE_WINDOW) {
            out.println("<html>");
            out.println(new Sources(request.getContextPath()).getHtmlJs());
            out.println("<script type=\"text/javascript\">");
            out.println("mgnlDialogReloadOpener();");
            out.println("window.close();");
            out.println("</script></html>");
        }
        // show the created dialog
        else if (view == VIEW_SHOW_DIALOG) {
            try {
                dialog.drawHtml(out);
            }
            catch (IOException e) {
                log.error(e);
            }
        }
    }

    /**
     * @return the default repository
     */
    public String getRepository() {
        return ContentRepository.WEBSITE;
    }
}