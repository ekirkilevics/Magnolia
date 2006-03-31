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
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.servlets.MVCServletHandlerImpl;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.cms.util.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * This is the MVCHandler for simple pages.
 * @author Philipp Bracher
 * @version $Revision$
 */

public abstract class PageMVCHandler extends MVCServletHandlerImpl {

    /**
     * The name of the parameter passed by the request. Not used for simple pages.
     */
    protected static final String COMMAND_PARAMETER_NAME = "cmd";

    protected static final String COMMAND_SHOW = "show"; //$NON-NLS-1$

    protected static final String VIEW_SHOW = "render"; //$NON-NLS-1$

    /**
     * the request passed by the MVCServlet
     */
    private HttpServletRequest request;

    /**
     * The repsonse passed by the MVCServlet
     */
    private HttpServletResponse response;

    /**
     * The posted multipart form. Use params for easy access.
     */
    private MultipartForm form;

    /**
     * The messages used for this page
     */
    private info.magnolia.cms.i18n.Messages msgs;

    /**
     * Parameters passed by the request. 
     */
    private RequestFormUtil params;

    /**
     * 
     * @param request
     * @param response
     */
    public PageMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        this.setRequest(request);
        this.setResponse(response);

        setForm(Resource.getPostedForm(request));
        setParams(new RequestFormUtil(request, getForm()));
        setMsgs(MessagesManager.getMessages());
    }

    /*
     * @see info.magnolia.cms.servlets.MVCServletHandler#getCommand()
     */
    public String getCommand() {
        if(StringUtils.isNotEmpty(getParams().getParameter(COMMAND_PARAMETER_NAME))){
            return getParams().getParameter(COMMAND_PARAMETER_NAME);
        }
        return COMMAND_SHOW;
    }

    /**
     * This is an empty implementation return the default show view.
     * @return the view name
     */
    public String show() {
        return VIEW_SHOW;
    }

    /**
     * @param request The request to set.
     */
    protected void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * @return Returns the request.
     */
    protected HttpServletRequest getRequest() {
        return request;
    }

    /**
     * @param response The response to set.
     */
    protected void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * @return Returns the response.
     */
    protected HttpServletResponse getResponse() {
        return response;
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
    protected void setMsgs(info.magnolia.cms.i18n.Messages msgs) {
        this.msgs = msgs;
    }

    /**
     * @return Returns the msgs.
     */
    protected info.magnolia.cms.i18n.Messages getMsgs() {
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

 
}