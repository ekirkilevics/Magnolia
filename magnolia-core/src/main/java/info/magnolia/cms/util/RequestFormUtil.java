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
package info.magnolia.cms.util;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * Sometimes one get the parameters via form (multipart-post) and via request (get, normal post). Using this Util you
 * have not to care.
 * @author philipp
 */
public class RequestFormUtil {

    private MultipartForm form;

    private HttpServletRequest request;

    public RequestFormUtil(HttpServletRequest request) {
        this(request, Resource.getPostedForm(request));
    }

    public RequestFormUtil(HttpServletRequest request, MultipartForm form) {
        super();
        this.form = form;
        this.request = request;
    }

    /**
     * @param name
     * @return
     */

    public String getParameter(String name) {
        return RequestFormUtil.getParameter(this.request, this.form, name);
    }

    public static String getParameter(HttpServletRequest request, String name) {
        return RequestFormUtil.getParameter(request, Resource.getPostedForm(request), name);
    }

    /**
     * returns the value found in the form or the request
     * @param request
     * @param form
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
        return RequestFormUtil.getParameter(this.request, this.form, name, defaultValue);
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

    /**
     * @param request
     * @param charset
     * @return decoded value
     */
    public static String getURLParameterDecoded(HttpServletRequest request, String name, String charset) {
        return (String) getURLParametersDecoded(request, charset).get(name);
    }

    /**
     * The url is not always properly decoded. This method does the job.
     * @param request
     * @param charset
     * @return decoded map of all values
     */
    public static Map getURLParametersDecoded(HttpServletRequest request, String charset) {
        Map map = new HashMap();
        String[] params = request.getQueryString().split("&");
        for (int i = 0; i < params.length; i++) {
            String name = StringUtils.substringBefore(params[i], "=");
            String value = StringUtils.substringAfter(params[i], "=");
            try {
                value = URLDecoder.decode(value, charset);
            }
            catch (UnsupportedEncodingException e) {
                // nothing: return value as is
            }
            map.put(name, value);
        }
        return map;
    }

    public MultipartForm getFrom() {
        return form;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.beans.runtime.MultipartForm#getDocument(java.lang.String)
     */
    public Document getDocument(String name) {
        return form.getDocument(name);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.beans.runtime.MultipartForm#getDocuments()
     */
    public Map getDocuments() {
        if(form == null){
            return new HashMap();
        }
        
        return form.getDocuments();
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.beans.runtime.MultipartForm#getParameters()
     */
    public Map getParameters() {
        if(form == null){
            return this.request.getParameterMap();
        }
        return form.getParameters();
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.beans.runtime.MultipartForm#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        if (this.form != null) {
            return this.form.getParameterValues(name);
        }
        return request.getParameterValues(name);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.beans.runtime.MultipartForm#removeParameter(java.lang.String)
     */
    public void removeParameter(String name) {
        form.removeParameter(name);
    }

}