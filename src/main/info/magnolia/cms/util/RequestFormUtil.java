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
package info.magnolia.cms.util;

import javax.servlet.http.HttpServletRequest;

import info.magnolia.cms.beans.runtime.MultipartForm;


/**
 * Sometimes one get the parameters via form (multipart-post) and via request (get, normal post). Using this Util you
 * have not to care.
 * @author philipp
 */
public class RequestFormUtil {

    private MultipartForm from;

    private HttpServletRequest request;

    public RequestFormUtil(HttpServletRequest request) {
        this(request, Resource.getPostedForm(request));
    }

    public RequestFormUtil(HttpServletRequest request, MultipartForm from) {
        super();
        this.from = from;
        this.request = request;
    }

    /**
     * @param name
     * @return
     */

    public String getParameter(String name) {
        return RequestFormUtil.getParameter(this.request, this.from, name);
    }

    public static String getParameter(HttpServletRequest request, String name) {
        return RequestFormUtil.getParameter(request, Resource.getPostedForm(request), name);
    }

    /**
     * returns the value found in the form or the request
     * @param request
     * @param from
     * @param name
     * @return
     */
    public static String getParameter(HttpServletRequest request, MultipartForm from, String name) {
        String param = null;
        if (from != null) {
            param = from.getParameter(name);
        }
        if (param == null) {
            param = request.getParameter(name);
        }
        return param;

    }

    public String getParameter(String name, String defaultValue) {
        return RequestFormUtil.getParameter(this.request, this.from, name, defaultValue);
    }

    public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
        return RequestFormUtil.getParameter(request, Resource.getPostedForm(request), name, defaultValue);
    }

    /**
     * returns the defaultValue if the parameter is not found in the request or form
     * @param string
     * @param repository
     * @return
     */
    public static String getParameter(HttpServletRequest request, MultipartForm from, String name, String defaultValue) {
        String param = getParameter(request, from, name);
        if (param == null) {
            param = defaultValue;
        }
        return param;
    }

}