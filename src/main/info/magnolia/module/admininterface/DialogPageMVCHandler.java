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

import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.servlets.MVCServletHandlerImpl;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.cms.util.Resource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * This is the MVCHandler for simple dialog pages.
 * @author Philipp Bracher
 * @version $Revision$
 */

public abstract class DialogPageMVCHandler extends MVCServletHandlerImpl {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(DialogPageMVCHandler.class);

    protected static final String COMMAND_SHOW = "show"; //$NON-NLS-1$

    protected static final String VIEW_DRAW = "draw"; //$NON-NLS-1$

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

    protected info.magnolia.cms.i18n.Messages msgs;

    protected RequestFormUtil params;

    /**
     * @param request
     * @param response
     */
    public DialogPageMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

        this.request = request;
        this.response = response;

        form = Resource.getPostedForm(request);
        params = new RequestFormUtil(request, form);
        msgs = MessagesManager.getMessages(request);
    }

    /*
     * @see info.magnolia.cms.servlets.MVCServletHandler#getCommand()
     */
    public String getCommand() {
        return COMMAND_SHOW;
    }

    /**
     * @return
     */
    public String show() {
        return VIEW_DRAW;
    }

    public void renderHtml(String view) throws IOException {
        if (VIEW_DRAW.equals(view)) {
            try {
                draw(request, response);
            }
            catch (Exception e) {
                response.getWriter().print(e);
            }
        }
    }

    protected abstract void draw(HttpServletRequest request, HttpServletResponse response) throws Exception;

}