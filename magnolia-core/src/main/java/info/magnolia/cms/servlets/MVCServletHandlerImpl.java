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
