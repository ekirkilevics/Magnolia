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
package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.gui.dialog.DialogControlImpl;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.servlets.MVCServletHandlerImpl;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.MgnlInstantiationException;
import info.magnolia.repository.RepositoryConstants;

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

    protected String locale;

    protected HierarchyManager hm;

    private Dialog dialog;

    protected Messages msgs;

    protected RequestFormUtil params;

    protected Content storageNode;

    private SaveHandler saveHandler;

    private String jsExecutedAfterSaving;

    private String itemType;

    private I18nAuthoringSupport i18nAuthoringSupport;

    /**
     * Initialize the used parameters: path, nodeCollectionName, nodeName, ..
     * @param request
     * @param response
     */
    public DialogMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        form = MgnlContext.getPostedForm();
        params = new RequestFormUtil(request, form);

        path = params.getParameter("mgnlPath"); //$NON-NLS-1$
        nodeCollectionName = params.getParameter("mgnlNodeCollection"); //$NON-NLS-1$
        nodeName = params.getParameter("mgnlNode"); //$NON-NLS-1$
        richE = params.getParameter("mgnlRichE"); //$NON-NLS-1$
        richEPaste = params.getParameter("mgnlRichEPaste"); //$NON-NLS-1$
        repository = params.getParameter("mgnlRepository", getRepository()); //$NON-NLS-1$
        locale = params.getParameter("mgnlLocale"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(repository)) {
            hm = MgnlContext.getHierarchyManager(repository);
        }
        msgs = MessagesManager.getMessages();
        // proxied. no need to refresh instance every time.
        i18nAuthoringSupport = I18nAuthoringSupport.Factory.getInstance();

    }

    /*
     * @see info.magnolia.cms.servlets.MVCServletHandler#getCommand()
     */
    @Override
    public String getCommand() {
        if (form != null) {
            return COMMAND_SAVE;
        }
        return COMMAND_SHOW_DIALOG;
    }

    /**
     * Calls createDialog and sets the common parameters on the dialog
     */
    public String showDialog() {
        return VIEW_SHOW_DIALOG;
    }

    protected void configureDialog(Dialog dialog) {
        dialog.setConfig("dialog", getName()); //$NON-NLS-1$
        dialog.setConfig("path", path); //$NON-NLS-1$
        dialog.setConfig("nodeCollection", nodeCollectionName); //$NON-NLS-1$
        dialog.setConfig("node", nodeName); //$NON-NLS-1$
        dialog.setConfig("richE", richE); //$NON-NLS-1$
        dialog.setConfig("richEPaste", richEPaste); //$NON-NLS-1$
        dialog.setConfig("repository", repository); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(locale)) {
            dialog.setConfig("locale", locale);
        }

        i18nAuthoringSupport.i18nIze(dialog);
    }

    /**
     * Is called during showDialog(). Here can you create/ add controls for the dialog.
     * @param configNode
     * @param storageNode
     * @throws RepositoryException
     */
    protected Dialog createDialog(Content configNode, Content storageNode) throws RepositoryException {
        return DialogFactory.getDialogInstance(this.getRequest(), this.getResponse(), storageNode, configNode);
    }

    /**
     * Uses the SaveControl. Override to take influence.
     * todo - this should take care of exceptions thrown while writing
     * - onPreSave, Save and onPostSave should be allowed to throw an exception on failure
     * @return close view name
     */
    public String save() {
        if (!validate()) {
            return onValidationFailed();
        }
        SaveHandler saveHandler = getSaveHandler();
        if (!onPreSave(saveHandler)) {
            return onSaveFailed();
        }
        if (!onSave(saveHandler)) {
            return onSaveFailed();
        }
        synchronized (ExclusiveWrite.getInstance()) {
            if (!onPostSave(saveHandler)) {
                return onSaveFailed();
            }
        }
        removeSessionAttributes();
        return VIEW_CLOSE_WINDOW;
    }

    private String onValidationFailed() {
        return showDialog();
    }

    protected boolean validate() {
        boolean passed = this.getDialog().validate();

        SaveHandler saveHandler = this.getSaveHandler();
        if (saveHandler instanceof ValidatingSaveHandler) {
            passed = ((ValidatingSaveHandler) saveHandler).validate();
        }

        if (!passed) {
            // after failing validation on creation of the paragraph, the paragraph info would get lost
            if (StringUtils.isEmpty(this.dialog.getConfigValue("paragraph"))) {
                // make sure paragraph name is set even after reloading the dialog due to failed validation
                String paragraphName = params.getParameter("mgnlParagraph");
                this.dialog.setConfig("paragraph", paragraphName);
            }
            if (StringUtils.isEmpty(this.dialog.getConfigValue("collectionNodeCreationItemType"))) {
                this.dialog.setConfig("collectionNodeCreationItemType", params.getParameter("mgnlCollectionNodeCreationItemType"));
            }
            if (StringUtils.isEmpty(this.dialog.getConfigValue("creationItemType"))) {
                this.dialog.setConfig("creationItemType", params.getParameter("mgnlCreationItemType"));
            }
        }

        return passed;
    }

    /**
     * Called if the save failed.
     */
    protected String onSaveFailed() {
        return showDialog();
    }

    /**
     * Returns the save handler used by this dialog handler.
     * @return the handler
     */
    protected SaveHandler getSaveHandler() {
        if (this.saveHandler == null) {
            createSaveHandler();

            configureSaveHandler(this.saveHandler);
        }
        return this.saveHandler;
    }

    /**
     * If there is the property saveHandler defined in the config it instantiates the configured save handler. Else it
     * instantiates the default save handler
     */
    protected void createSaveHandler() {
        Content configNode = this.getConfigNode();
        if (configNode != null) {
            String className = NodeDataUtil.getString(configNode, "saveHandler");
            if (StringUtils.isNotEmpty(className)) {
                try {
                    this.saveHandler = Classes.newInstance(className);
                }
                catch (MgnlInstantiationException e) {
                    log.error("can't create save handler", e);
                }
                catch (ClassNotFoundException e) {
                    log.error("can't create save handler", e);
                }
            }
        }

        if (this.saveHandler == null) {
            this.saveHandler = Components.getComponentProvider().newInstance(SaveHandler.class);
        }
    }

    /**
     * Configure the save control
     */
    protected void configureSaveHandler(SaveHandler saveHandler) {
        saveHandler.init(form);

        saveHandler.setPath(form.getParameter("mgnlPath")); //$NON-NLS-1$
        saveHandler.setNodeCollectionName(form.getParameter("mgnlNodeCollection")); //$NON-NLS-1$
        saveHandler.setNodeName(form.getParameter("mgnlNode")); //$NON-NLS-1$
        saveHandler.setParagraph(form.getParameter("mgnlParagraph")); //$NON-NLS-1$
        saveHandler.setRepository(form.getParameter("mgnlRepository")); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(form.getParameter("mgnlCollectionNodeCreationItemType"))) {
            saveHandler.setCollectionNodeCreationItemType(new ItemType(form.getParameter("mgnlCollectionNodeCreationItemType")));
        }
        if (StringUtils.isNotEmpty(getItemType())) {
            saveHandler.setCreationItemType(new ItemType(getItemType()));
        } else if (StringUtils.isNotEmpty(form.getParameter("mgnlCreationItemType"))) {
            saveHandler.setCreationItemType(new ItemType(form.getParameter("mgnlCreationItemType")));
        }

        if (this.saveHandler instanceof DialogAwareSaveHandler) {
            ((DialogAwareSaveHandler) saveHandler).setDialog(this.getDialog());
        }
    }

    protected boolean onPreSave(SaveHandler control) {
        return true;
    }

    protected boolean onSave(SaveHandler control) {
        boolean result = control.save();
        if (result) {
            this.nodeName = control.getNodeName();
        }
        return result;
    }

    protected boolean onPostSave(SaveHandler control) {
        return true;
    }

    /**
     * Defines the node/page containing the data editing in this dialog. The default implementation is using the path
     * parameter
     */
    public Content getStorageNode() {
        // hm is null if this dialog is not used to show content
        if (storageNode == null && hm != null) {
            try {
                if(this.path == null){
                    log.debug("No path defined for a dialog called by the url [{}]", this.getRequest().getRequestURL());
                    return null;
                }
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
            catch(AccessDeniedException ade){
                log.error("can't read content to edit", ade);
            }
            catch (RepositoryException re) {
                // content does not exist yet
                log.debug("can't read content or it does not exist yet", re);
            }
        }
        return storageNode;
    }

    /**
     * Returns the node with the dialog definition. Default: null
     */
    public Content getConfigNode() {
        return null;
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandler#renderHtml(java.lang.String)
     */
    @Override
    public void renderHtml(String view) throws IOException {
        PrintWriter out = this.getResponse().getWriter();

        // after saving
        if (VIEW_CLOSE_WINDOW.equals(view)) {

            out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
            out.write(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
            out.println("<html>"); //$NON-NLS-1$
            out.println(new Sources(this.getRequest().getContextPath()).getHtmlJs());
            out.println("<script type=\"text/javascript\">"); //$NON-NLS-1$
            out.println("var path = '" + this.path + "'");
            out.println(StringUtils.defaultIfEmpty(getJsExecutedAfterSaving(), "mgnlDialogReloadOpener();")); //$NON-NLS-1$
            out.println("window.close();"); //$NON-NLS-1$
            out.println("</script></html>"); //$NON-NLS-1$
        }
        // show the created dialog
        else if (VIEW_SHOW_DIALOG.equals(view)) {
            try {
                getDialog().drawHtml(out);
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
        return RepositoryConstants.WEBSITE;
    }

    public void removeSessionAttributes() {
        String[] toRemove = form.getParameterValues(DialogControlImpl.SESSION_ATTRIBUTENAME_DIALOGOBJECT_REMOVE);
        if (toRemove != null) {
            for (int i = 0; i < toRemove.length; i++) {
                HttpSession httpsession = this.getRequest().getSession(false);
                if (httpsession != null) {
                    httpsession.removeAttribute(toRemove[i]);
                }
            }
        }
    }

    /**
     * @param dialog The dialog to set.
     */
    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    /**
     * @return Returns the dialog.
     */
    public Dialog getDialog() {
        if (this.dialog == null) {
            try {
                this.dialog = createDialog(this.getConfigNode(), this.getStorageNode());
            }
            catch (RepositoryException e) {
                log.error("can't create dialog", e);
            }
            configureDialog(this.dialog);
        }
        return this.dialog;
    }


    public String getJsExecutedAfterSaving() {
        return this.jsExecutedAfterSaving;
    }


    public void setJsExecutedAfterSaving(String jsExecutedAfterSaving) {
        this.jsExecutedAfterSaving = jsExecutedAfterSaving;
    }


    public String getItemType() {
        return this.itemType;
    }


    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
