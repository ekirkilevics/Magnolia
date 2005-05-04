/*
 * Created on 04.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package info.magnolia.cms.util;

import javax.servlet.http.HttpServletRequest;

import info.magnolia.cms.beans.runtime.MultipartForm;


/**
 * @author philipp Sometimes one get the parameters via form (multipart-post) and via request (get, normal post). Using
 * this Util you have not to care.
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