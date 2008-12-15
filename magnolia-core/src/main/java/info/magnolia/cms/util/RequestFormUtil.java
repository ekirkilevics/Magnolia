/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


/**
 * Sometimes one get the parameters via form (multipart-post) and via request (get, normal post). Using this Util you
 * have not to care.
 * @author philipp
 */
public class RequestFormUtil {

    private MultipartForm form;

    private HttpServletRequest request;

    public RequestFormUtil(HttpServletRequest request) {
        this(request, Resource.getPostedForm());
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
        return getParameter(this.request, this.form, name);
    }

    public static String getParameter(HttpServletRequest request, String name) {
        return getParameter(request, Resource.getPostedForm(), name);
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
            if (request.getMethod().equals("GET")) {
                param = getURLParameterDecoded(request, name, "UTF8");
            }
            else {
                param = request.getParameter(name);
            }
        }
        return param;

    }

    public String getParameter(String name, String defaultValue) {
        return getParameter(this.request, this.form, name, defaultValue);
    }

    public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
        return getParameter(request, Resource.getPostedForm(), name, defaultValue);
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

        String queryString = request.getQueryString();
        if (queryString != null) {
            return getURLParametersDecoded(queryString, charset);
        }
        return new HashMap();
    }

    /**
     * Extract and decodes parameters from a query string
     * @param queryString query string
     * @param charset charset (e.g UTF-8)
     */
    public static Map getURLParametersDecoded(String queryString, String charset) {
        Map map = new HashMap();
        String[] params = queryString.split("&");
        for (int i = 0; i < params.length; i++) {
            String name = StringUtils.substringBefore(params[i], "=");
            String value = StringUtils.substringAfter(params[i], "=");
            try {
                value = URLDecoder.decode(value, charset);
            }
            catch (UnsupportedEncodingException e) {
                // nothing: return value as is
            }
            // @todo what about multi-valued parameters??
            map.put(name, value);
        }
        return map;
    }

    public MultipartForm getForm() {
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
        if (form == null) {
            return new HashMap();
        }

        return form.getDocuments();
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.beans.runtime.MultipartForm#getParameters()
     */
    public Map getParameters() {
        return getParameters(this.request);
    }

    public static Map getParameters(HttpServletRequest request) {
        MultipartForm form = Resource.getPostedForm();
        if (form == null) {
            // if get use UTF8 decoding
            if (request.getMethod() == "GET") {
                return getURLParametersDecoded(request, "UTF8");
            }
            return request.getParameterMap();
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
