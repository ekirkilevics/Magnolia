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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.FactoryUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;


/**
 * This class enables to get the current Request without passing the request around the world. A ThreadLocal is used to
 * manage this.
 * <p>
 * In a later version this class should not depend on servlets. The core should use the context to get and set
 * attributes instead of using the request or session object directly. Magnolia could run then in a neutral and
 * configurable context.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */

public class MgnlContext {

    /**
     * Logger
     */
    public static Logger log = Logger.getLogger(MgnlContext.class);

    /**
     * The thread local variable holding the current context
     */
    private static ThreadLocal context = new ThreadLocal();

    private HttpServletRequest request;

    private HttpServletResponse response;

    private HttpSession session;

    /**
     * Do not instantiate this class. The constructor must be public to use discovery
     */
    public MgnlContext() {

    }

    /**
     * Called in the filter to initialize the current context
     * @param newRequest The incoming http request
     * @param newResponse The outgoing http reply
     */
    public void init(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession(true);
    }

    public static HttpSession getSession() {
        return MgnlContext.getInstance().session;
    }

    /**
     * @return Returns the request.
     */
    public static HttpServletRequest getRequest() {
        return getInstance().request;
    }

    /**
     * @return Returns the response.
     */
    public static HttpServletResponse getResponse() {
        return getInstance().response;
    }

    /**
     * A short cut for the current user.
     * @return the current user
     */
    public static User getUser() {
        return Security.getUserManager().getCurrent();
    }

    /**
     * Make it easier to get the HierarchyManager independent of the presence of a request.
     */
    public static HierarchyManager getHierarchyManager(String repository) {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return SessionAccessControl.getHierarchyManager(request, repository);
        }
        else {
            return ContentRepository.getHierarchyManager(repository);
        }
    }

    /**
     * Get the current context of the system. Uses the FactoryUtil
     * @return the context
     */
    public static MgnlContext getInstance() {
        MgnlContext ctx = (MgnlContext) context.get();
        if (ctx == null) {
            ctx = (MgnlContext) FactoryUtil.getInstance(MgnlContext.class);
            context.set(ctx);
        }
        return ctx;
    }

}
