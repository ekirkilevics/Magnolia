/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.servlets;




import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

import info.magnolia.cms.beans.config.*;
import info.magnolia.cms.beans.runtime.*;
import info.magnolia.cms.Aggregator;
import info.magnolia.cms.Dispatcher;
import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.CacheProcess;
import info.magnolia.cms.security.Listener;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.security.Lock;
//import com.quatico.i18n.TranslationEngine;
import org.apache.log4j.Logger;



/**
 *
 * This is the main http servlet which will be called for any resource request
 * this servlet will dispacth or process requests according to their nature
 * -- all resource requests will go to ResourceDispatcher
 * -- all page requests will be handed over to the defined JSP or Servlet (template)
 *
 *
 *
 * Date: June 3, 2004
 * Time: 2:26:12 PM
 * @author Sameer Charles
 * @version 2.0
 * */




public class EntryServlet extends HttpServlet {



    private static Logger log = Logger.getLogger(EntryServlet.class);

    private static final String REQUEST_INTERCEPTOR = "/RequestInterceptor";
    public static final String INTERCEPT = "mgnlIntercept";


    private String uri;
    private String extension;




    /**
     * <p>
     * This makes browser and proxy caches work more effectively,
     * reducing the load on server and network resources.
     *
     * </p>
     *
     * @param request
     * @return last modied time in miliseconds since 1st Jan 1970 GMT
     * */
    public long getLastModified(HttpServletRequest request) {
        return info.magnolia.cms.beans.runtime.Cache.getCreationTime(request);
    }



    /**
     * <p>
     * All HTTP/s requests are handled here
     * </p>
     *
     * @param req
     * @param res
     * */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException,
            IOException {
        try {
            /**
             * Try to find out what the preferred language of this user is.
             */
            //TranslationEngine.findPreferredLanguage(req);
            this.setURI(req);
            if (isAllowed(req,res)) {
                if (redirect(req,res))
                    return;
                intercept(req,res);
                /* try to stream from cache first */
                if (info.magnolia.cms.beans.runtime.Cache.isCached(req)) {
                    if (CacheHandler.streamFromCache(req,res))
                        return; /* if success return */
                }
                /* aggregate content */
                Aggregator aggregator = new Aggregator(req,res);
                boolean success = aggregator.collect();
                aggregator = null;
                try {
                    Dispatcher.dispatch(req,res,getServletContext());
                    if (success) {
                        if (info.magnolia.cms.beans.config.Cache.isCacheable()) {
                            CacheProcess cache = new CacheProcess(req);
                            cache.start();
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }



    /**
     * <p>All requests are handles by get handler</p>
     *
     * @param req
     * @param res
     * */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req,res);
    }



    /**
     * <p>
     * checks access from Listener / Authenticator / AccessLock
     * </p>
     *
     * @return boolean
     * @param req HttpServletRequest as received by the service method
     * @param res HttpServletResponse as received by the service method
     */
    private boolean isAllowed (HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (Lock.isSystemLocked()) {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        } else if (SessionAccessControl.isSecuredSession(req)) {
            return true;
        } else if ((SecureURI.isProtected(uri))) {
            return authenticate(req,res);
        } else if (!Listener.isAllowed(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }



    /**
     * <p>
     * Authenticate on basic headers
     * </p>
     *
     * @param req
     * @param res
     * */
    private boolean authenticate(HttpServletRequest req, HttpServletResponse res) {
        try {
            if (!Authenticator.authenticate(req)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setHeader("WWW-Authenticate","BASIC realm=\""+Server.getBasicRealm()+"\"");
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }



    /**
     * <p>
     * redirect based on the mapping in config/server/.node.xml
     * </p>
     * @param request
     * @param response
     * */
    private boolean redirect(HttpServletRequest request, HttpServletResponse response) {
        String URI = this.getURIMap(request);
        if (!URI.equals("")) {
            try {
                request.getRequestDispatcher(URI).forward(request,response);
            } catch (Exception e) {
                log.error("Failed to forward - "+URI);
                log.error(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }

    
    /**
     * <p>
     * attach Interceptor servlet if interception needed
     * </p>
     * @param request
     * @param response
     * */
    private void intercept(HttpServletRequest request, HttpServletResponse response) {
        if (request.getParameter(INTERCEPT) != null) {
            try {
                request.getRequestDispatcher(REQUEST_INTERCEPTOR).include(request,response);
            } catch (Exception e) {
                log.error("Failed to Intercept");
                log.error(e.getMessage(), e);
            }
        }
    }


    /**
     *
     * @return URI mapping as in ServerInfo
     */
    private String getURIMap(HttpServletRequest request) {
        return VirtualMap.getInstance().getURIMapping(request.getRequestURI());
    }



    /**
     * <p>Extracts uri and extension</p>
     * @param request
     * */
    private void setURI(HttpServletRequest request) {
        extension = Server.getDefaultExtension();
        try {
            int lastIndexOfDot = request.getRequestURI().lastIndexOf(".");
            if (lastIndexOfDot > -1) {
                extension = request.getRequestURI().substring(lastIndexOfDot+1);
                uri = request.getRequestURI().substring(0,lastIndexOfDot);
            } else {
                uri = request.getRequestURI();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}





