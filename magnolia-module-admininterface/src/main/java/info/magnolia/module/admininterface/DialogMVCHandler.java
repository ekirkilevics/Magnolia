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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogSuper;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.servlets.MVCServletHandlerImpl;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the MVCHandler for dialogs. You can make a subclass to take influence on creation or saving.
 * @author Philipp Bracher
 * @version $Id$
 */

public class DialogMVCHandler extends MVCServletHandlerImpl {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DialogMVCHandler.class);

    /*
     * Commands
     */
    protected static final String COMMAND_SAVE = "save"; //$NON-NLS-1$

    protected static final String COMMAND_SELECT_PARAGRAPH = "selectParagraph"; //$NON-NLS-1$

    protected static final String COMMAND_SHOW_DIALOG = "showDialog"; //$NON-NLS-1$

    /*
     * Views
     */
    protected static final String VIEW_CLOSE_WINDOW = "close"; //$NON-NLS-1$

    protected static final String VIEW_SHOW_DIALOG = "show"; //$NON-NLS-1$

    /**
     * The posted multipart form. Use params for easy access.
     */
    protected MultipartForm form;

    /**
     * Path to the node containing the data
     */
    protected String path = StringUtils.EMPTY;

    /**
     * If the dialog serves a collection (multiple instances of the same dialog)
     */
    private String nodeCollectionName = StringUtils.EMPTY;

    /**
     * the node containing the date for this dialog
     */
    protected String nodeName = StringUtils.EMPTY;

    protected String richE = StringUtils.EMPTY;

    protected String richEPaste = StringUtils.EMPTY;

    protected String repository = StringUtils.EMPTY;

    protected HierarchyManager hm;

    protected DialogDialog dialog;

    protected Messages msgs;

    protected RequestFormUtil params;

    protected Content storageNode;

    private SaveHandler save;

    /**
     * Initialize the used parameters: path, nodeCollectionName, nodeName, ..
     * @param request
     * @param response
     */
    public DialogMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        form = Resource.getPostedForm(request);
        params = new RequestFormUtil(request, form);

        path = params.getParameter("mgnlPath"); //$NON-NLS-1$
        nodeCollectionName = params.getParameter("mgnlNodeCollection"); //$NON-NLS-1$
        nodeName = params.getParameter("mgnlNode"); //$NON-NLS-1$
        richE = params.getParameter("mgnlRichE"); //$NON-NLS-1$
        richEPaste = params.getParameter("mgnlRichEPaste"); //$NON-NLS-1$
        repository = params.getParameter("mgnlRepository", getRepository()); //$NON-NLS-1$
        if(StringUtils.isNotEmpty(repository)){
            hm = MgnlContext.getHierarchyManager(repository);
        }
        msgs = MessagesManager.getMessages();
    }

    /*
     * @see info.magnolia.cms.servlets.MVCServletHandler#getCommand()
     */
    public String getCommand() {
        if (form != null) {
            return COMMAND_SAVE;
        }
        return COMMAND_SHOW_DIALOG;
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

            dialog.setConfig("dialog", getName()); //$NON-NLS-1$
            dialog.setConfig("path", path); //$NON-NLS-1$
            dialog.setConfig("nodeCollection", nodeCollectionName); //$NON-NLS-1$
            dialog.setConfig("node", nodeName); //$NON-NLS-1$
            dialog.setConfig("richE", richE); //$NON-NLS-1$
            dialog.setConfig("richEPaste", richEPaste); //$NON-NLS-1$
            dialog.setConfig("repository", repository); //$NON-NLS-1$
        }
        catch (RepositoryException e) {
            log.error("can't instantiate dialog", e); //$NON-NLS-1$
        }

        return VIEW_SHOW_DIALOG;
    }

    /**
     * Is called during showDialog(). Here can you create/ add controls for the dialog.
     * @param configNode
     * @param storageNode
     * @throws RepositoryException
     */
    protected DialogDialog createDialog(Content configNode, Content storageNode) throws RepositoryException {
        return DialogFactory.getDialogDialogInstance(this.getRequest(), this.getResponse(), storageNode, configNode);
    }

    /**
     * Uses the SaveControl. Override to take influence.
     * @return close view name
     */
    public String save() {
        SaveHandler control = getSaveHandler();
        onPreSave(control);
        onSave(control);
        onPostSave(control);
        removeSessionAttributes();
        return VIEW_CLOSE_WINDOW;
    }

    /**
     * Returns the save handler used by this dialog handler.
     * @return the handler
     */
    protected SaveHandler getSaveHandler() {
        if (this.save == null) {
            this.save = (SaveHandler) FactoryUtil.getInstance(SaveHandler.class);
            configureSaveHandler(this.save);
        }
        return this.save;
    }

    /**
     * Configure the save control
     */
    protected void configureSaveHandler(SaveHandler save) {
        save.init(form);

        save.setPath(form.getParameter("mgnlPath")); //$NON-NLS-1$
        save.setNodeCollectionName(form.getParameter("mgnlNodeCollection")); //$NON-NLS-1$
        save.setNodeName(form.getParameter("mgnlNode")); //$NON-NLS-1$
        save.setParagraph(form.getParameter("mgnlParagraph")); //$NON-NLS-1$
        save.setRepository(form.getParameter("mgnlRepository")); //$NON-NLS-1$
    }

    protected void onPreSave(SaveHandler control) {
    }

    protected void onSave(SaveHandler control) {
        control.save();
    }

    protected void onPostSave(SaveHandler control) {
    }

    /**
     * Defines the node/page containing the data editing in this dialog. The default implementation is using the path
     * parameter
     */
    public Content getStorageNode() {
        // hm is null if this dialog is not used to show content
        if (storageNode == null && hm != null) {
            try {
                Content parentContent = hm.getContent(path);
                if (StringUtils.isEmpty(nodeName)) {
                    if (StringUtils.isEmpty(nodeCollectionName)) {
                        storageNode = parentContent;
                    }
                    else {
                        storageNode = parentContent.getContent(nodeCollectionName);
                    }
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
    public Content getConfigNode() {
        return null;
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandler#renderHtml(java.lang.String)
     */
    public void renderHtml(String view) throws IOException {
        PrintWriter out = this.getResponse().getWriter();

        // after saving
        if (VIEW_CLOSE_WINDOW.equals(view)) {
            out.println("<html>"); //$NON-NLS-1$
            out.println(new Sources(this.getRequest().getContextPath()).getHtmlJs());
            out.println("<script type=\"text/javascript\">"); //$NON-NLS-1$
            out.println("mgnlDialogReloadOpener();"); //$NON-NLS-1$
            out.println("window.close();"); //$NON-NLS-1$
            out.println("</script></html>"); //$NON-NLS-1$
        }
        // show the created dialog
        else if (view == VIEW_SHOW_DIALOG) {
            try {
                dialog.drawHtml(out);
            }
            catch (IOException e) {
                log.error("Exception caught", e);
            }
        }
    }

    /**
     * @return the default repository
     */
    public String getRepository() {
        return ContentRepository.WEBSITE;
    }

    public void removeSessionAttributes() {
        String[] toRemove = form.getParameterValues(DialogSuper.SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE);
        if (toRemove != null) {
            for (int i = 0; i < toRemove.length; i++) {
                HttpSession httpsession = this.getRequest().getSession(false);
                if (httpsession != null) {
                    httpsession.removeAttribute(toRemove[i]);
                }
            }
        }
    }
}