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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This is a simple default implementation. Overwrite the render method to process the page.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public abstract class SimplePageMVCHandler extends PageMVCHandler {
    
    /**
     * @param name
     * @param request
     * @param response
     */
    public SimplePageMVCHandler(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Calls the render method.
     * @see info.magnolia.cms.servlets.MVCServletHandler#renderHtml(java.lang.String)
     */
    public void renderHtml(String view) throws IOException {
        if (VIEW_SHOW.equals(view)) {
            try {
                render(getRequest(), getResponse());
            }
            catch (Exception e) {
                log.error("Exception during rendering the page",e);
                e.printStackTrace(getResponse().getWriter());
            }
        }
    }

    /**
     * Does the rendering job. You have to override this method.
     * @param request
     * @param response
     * @throws Exception
     */
    protected abstract void render(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
