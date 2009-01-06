/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.servlets;

import info.magnolia.cms.util.RequestFormUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of a MVCHandler. Calls the command (method) through reflection.
 * @author Philipp Bracher
 * @version $Id$
 */
public abstract class MVCServletHandlerImpl implements MVCServletHandler {

    protected static final String VIEW_ERROR = "error"; //$NON-NLS-1$
    
    protected static final String VIEW_SUCCESS = "success"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MVCServletHandlerImpl.class);

    protected HttpServletRequest request;

    protected HttpServletResponse response;

    protected Throwable exception;

    private String name;

    private String command;

    protected MVCServletHandlerImpl(String name, HttpServletRequest request, HttpServletResponse response) {
        this.name = name;
        this.setRequest(request);
        this.setResponse(response);
    }

    public void init() {
        populateFromRequest(this);
    }

    protected void populateFromRequest(Object bean) {
        RequestFormUtil requestFormUtil = new RequestFormUtil(this.getRequest());
        Map parameters = new HashMap(); // needed, can't directly modify the map returned by request.getParameterMap()
        parameters.putAll(requestFormUtil.getParameters());
        parameters.putAll(requestFormUtil.getDocuments()); // handle uploaded files too

        try {
            // TODO : we could filter the parameters
            BeanUtils.populate(bean, parameters);
        }
        catch (Exception e) {
            log.error("can't set properties on the handler", e);
        }
    }

    /**
     * @see info.magnolia.cms.servlets.MVCServletHandler#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Call the method through reflection
     * @param command
     * @return the name of the view to show (used in renderHtml)
     */
    public String execute(String command) {
        String view = VIEW_ERROR;
        Method method;

        try {
            method = this.getClass().getMethod(command, new Class[]{});
            // method.setAccessible(true);
            view = (String) method.invoke(this, new Object[]{});
        }
        catch (InvocationTargetException e) {
            log.error("can't call command: " + command, e.getTargetException()); //$NON-NLS-1$
            exception = e.getTargetException();
        }
        catch (Exception e) {
            log.error("can't call command: " + command, e); //$NON-NLS-1$
            exception = e;
        }

        return view;
    }

    /**
     * @param request The request to set.
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * @return Returns the request.
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * @param response The response to set.
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * @return Returns the response.
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * @return Returns the command.
     */
    public String getCommand() {
        return this.command;
    }

    /**
     * @param command The command to set.
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Getter for <code>exception</code>.
     * @return Returns the exception.
     */
    public Throwable getException() {
        return this.exception;
    }

    /**
     * Returns the stacktrace from the exception as a String
     * @return
     */
    public String getExceptionStackTrace() {
        if (this.exception == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        this.exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
