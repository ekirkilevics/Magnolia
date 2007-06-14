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

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.servlets.CommandBasedMVCServletHandler;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.cms.util.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the MVCHandler for simple pages. The properties with coresponding request parameters are set with BeanUtils.
 * @author Philipp Bracher
 * @version $Revision$
 */

public abstract class PageMVCHandler extends CommandBasedMVCServletHandler {

    /**
     * Logger
     */
    Logger log = LoggerFactory.getLogger(PageMVCHandler.class);

    /**
     * The name of the parameter passed by the request. Not used for simple pages.
     */
    protected static final String COMMAND_PARAMETER_NAME = "command";

    protected static final String COMMAND_SHOW = "show"; //$NON-NLS-1$

    protected static final String VIEW_SHOW = "show"; //$NON-NLS-1$

    /**
     * The posted multipart form. Use params for easy access.
     */
    private MultipartForm form;

    /**
     * The messages used for this page
     */
    private Messages msgs;

    /**
     * Parameters passed by the request.
     */
    private RequestFormUtil params;

    /**
     * Basename used to get messages
     */
    private String i18nBasename = MessagesManager.DEFAULT_BASENAME;

    /**
     * Constuctor
     * @param name
     * @param request
     * @param response
     */
    public PageMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        setForm(Resource.getPostedForm(request));
        setParams(new RequestFormUtil(request, getForm()));
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandlerImpl#init()
     */
    public void init() {
        super.init();
        if (StringUtils.isEmpty(this.getCommand())) {
            this.setCommand(COMMAND_SHOW);
        }
    }

    /**
     * This is an empty implementation return the default show view.
     * @return the view name
     */
    public String show() {
        return VIEW_SHOW;
    }

    /**
     * @param form The form to set.
     */
    protected void setForm(MultipartForm form) {
        this.form = form;
    }

    /**
     * @return Returns the form.
     */
    protected MultipartForm getForm() {
        return form;
    }

    /**
     * @param msgs The msgs to set.
     */
    protected void setMsgs(Messages msgs) {
        this.msgs = msgs;
    }

    /**
     * @return Returns the msgs.
     */
    protected Messages getMsgs() {
        if(msgs == null){
            msgs = MessagesManager.getMessages(getI18nBasename());
        }
        return msgs;
    }

    /**
     * @param params The params to set.
     */
    protected void setParams(RequestFormUtil params) {
        this.params = params;
    }

    /**
     * @return Returns the params.
     */
    protected RequestFormUtil getParams() {
        return params;
    }


    public String getI18nBasename() {
        return this.i18nBasename;
    }


    public void setI18nBasename(String basename) {
        this.i18nBasename = basename;
    }
}