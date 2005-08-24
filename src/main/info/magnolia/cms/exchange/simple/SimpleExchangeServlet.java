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
package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Listener;
import info.magnolia.cms.security.Lock;
import info.magnolia.cms.security.SecureURI;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.exchange.ExchangeException;
import info.magnolia.exchange.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.jcr.PathNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * <p>
 * Version .01 implementation Simple implementation of Exchange interface using serialized objects and binary GET
 * </p>
 * 
 * <pre>
 * todo -
 * 1. implement incremental delivery
 * 2. concurrent activation
 * 3. context locking
 * </pre>
 * 
 * @author Sameer Charles
 * @version 2.0
 */
public class SimpleExchangeServlet extends HttpServlet implements SingleThreadModel {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SimpleExchangeServlet.class);

    private transient HierarchyManager hierarchyManager;

    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String context = request.getHeader(Syndicator.WORKING_CONTEXT);

        log.debug("SimpleExchange.doGet()"); //$NON-NLS-1$

        try {
            response.setContentType("text/plain"); //$NON-NLS-1$
            response.setCharacterEncoding(DEFAULT_ENCODING);
            // this.handleActivationRequest();

            String action = request.getHeader(Syndicator.ACTION);
            String page = request.getHeader(Syndicator.PAGE);
            String recursive = request.getHeader(Syndicator.RECURSIVE);
            boolean recurse = BooleanUtils.toBoolean(recursive);
            boolean includeContentNodes = BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBooleanObject(request
                .getHeader(Syndicator.INCLUDE_CONTENTNODES)), true);

            if (ConfigLoader.isConfigured() && (!Listener.isAllowed(request) || !Authenticator.authenticate(request))) {
                // ignore security is server is not configured
                return;
            }

            if (ConfigLoader.isConfigured()) {
                this.hierarchyManager = SessionAccessControl.getHierarchyManager(request, context);
            }
            else {
                this.hierarchyManager = ContentRepository.getHierarchyManager(context);
            }

            // @todo getHierarchyManager() should not return null without throwing an exception
            if (this.hierarchyManager == null) {
                throw new ExchangeException("HierarchyManager is not configured for " + context); //$NON-NLS-1$
            }

            if (action.equals(Syndicator.ACTIVATE)) {
                activate(request);
            }
            else if (action.equals(Syndicator.DE_ACTIVATE)) {
                deactivate(request);
            }
            else if (action.equals(Syndicator.GET)) {
                String type = request.getHeader(Syndicator.GET_TYPE);
                get(page, type, recurse, includeContentNodes, response);
            }
            else {
                throw new UnsupportedOperationException("Method not supported by Exchange protocol - Simple (.01)"); //$NON-NLS-1$
            }
        }
        catch (OutOfMemoryError e) {
            Runtime rt = Runtime.getRuntime();
            log.error("---------\nOutOfMemoryError caught during activation. Total memory = " //$NON-NLS-1$
                + rt.totalMemory() + ", free memory = " //$NON-NLS-1$
                + rt.freeMemory() + "\n---------"); //$NON-NLS-1$

        }
        catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * @throws Exception
     */
    public void activate(HttpServletRequest request) throws Exception {

        String page = request.getHeader(Syndicator.PAGE);

        if (log.isDebugEnabled()) {
            log.debug("Exchange : update request received for " + page); //$NON-NLS-1$
        }

        String parent = request.getHeader(Syndicator.PARENT);
        String objectType = request.getHeader(Syndicator.OBJECT_TYPE);
        String recursive = request.getHeader(Syndicator.RECURSIVE);
        String senderContext = request.getHeader(Syndicator.SENDER_CONTEXT);
        String context = request.getHeader(Syndicator.WORKING_CONTEXT);

        String protocol = getProtocolName(request);
        String host = request.getRemoteHost();
        String remotePort = request.getHeader(Syndicator.REMOTE_PORT);
        String senderURL = request.getHeader(Syndicator.SENDER_URL);

        if (StringUtils.isEmpty(senderURL)) {
            senderURL = protocol + "://" + host + ":" + remotePort; //$NON-NLS-1$ //$NON-NLS-2$
        }

        String handle = StringUtils.defaultString(senderContext) + "/" + Syndicator.DEFAULT_HANDLER; //$NON-NLS-1$

        URL url = new URL(senderURL + handle);
        String credentials = request.getHeader("Authorization"); //$NON-NLS-1$
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", credentials); //$NON-NLS-1$
        urlConnection.addRequestProperty(Syndicator.ACTION, Syndicator.GET);
        urlConnection.addRequestProperty(Syndicator.WORKING_CONTEXT, context);
        urlConnection.addRequestProperty(Syndicator.PAGE, page);
        urlConnection.addRequestProperty(Syndicator.PARENT, parent);
        urlConnection.addRequestProperty(Syndicator.GET_TYPE, Syndicator.GET_TYPE_SERIALIZED_OBJECT);
        urlConnection.addRequestProperty(Syndicator.RECURSIVE, recursive);
        urlConnection.addRequestProperty(Syndicator.OBJECT_TYPE, objectType);

        // add this parameter only if present. this was not present in older versions
        if (request.getHeader(Syndicator.INCLUDE_CONTENTNODES) != null) {
            urlConnection.addRequestProperty(Syndicator.INCLUDE_CONTENTNODES, request
                .getHeader(Syndicator.INCLUDE_CONTENTNODES));
        }

        // Import activated page
        InputStream in = urlConnection.getInputStream();
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            Object sc = objectInputStream.readObject();
            // deserialize received object
            ContentWriter contentWriter = new ContentWriter(this.getHierarchyManager(), context, senderURL
                + StringUtils.defaultString(senderContext)
                + "/" //$NON-NLS-1$
                + Syndicator.DEFAULT_HANDLER, request);
            contentWriter.writeObject(parent, sc);
        }
        catch (Exception e) {
            log.error("Failed to de-serialize - " + page); //$NON-NLS-1$
            log.error(e.getMessage(), e);
        }
        Lock.setSystemLock();
        CacheHandler.flushCache();
        Lock.resetSystemLock();
    }

    /**
     * @throws Exception
     */
    public void deactivate(HttpServletRequest request) throws Exception {

        String page = request.getHeader(Syndicator.PAGE);
        if (log.isDebugEnabled()) {
            log.debug("Exchange : remove request received for " + page); //$NON-NLS-1$
        }
        HierarchyManager hm = this.getHierarchyManager();

        try {
            hm.delete(page);
            hm.save();
            CacheHandler.flushCache();
            SecureURI.delete(page);
            SecureURI.delete(page + "/*"); //$NON-NLS-1$
        }
        catch (PathNotFoundException e) {
            // ok, the node simply doesn't exist on the public instance, maybe it has never been activated
            // don't log any error
            if (log.isDebugEnabled()) {
                log.debug("Unable to deactivate node " + page + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * @param page
     * @param type
     * @param recurse
     * @param includeContentNodes only relevant if the node is of type CONTENT
     * @param response
     * @throws Exception
     */
    private void get(String page, String type, boolean recurse, boolean includeContentNodes,
        HttpServletResponse response) throws Exception {
        if (type.equalsIgnoreCase(Syndicator.GET_TYPE_BINARY)) {
            // this.getBinary();
            if (log.isDebugEnabled()) {
                log.debug("Binary request for " + page); //$NON-NLS-1$
            }
            HierarchyManager hm = this.getHierarchyManager();
            try {
                InputStream is = hm.getNodeData(page).getValue().getStream();
                ServletOutputStream os = response.getOutputStream();
                byte[] buffer = new byte[8192];
                int read = 0;
                while ((read = is.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                }
                os.flush();
                os.close();
            }
            catch (PathNotFoundException e) {
                log.error("Unable to spool " + page); //$NON-NLS-1$
                throw new PathNotFoundException(e.getMessage());
            }
        }
        else {
            // this.getSerializedObject(); // default type, supporting magnolia 1.1
            if (log.isDebugEnabled()) {
                log.debug("Serialized object request for " + page); //$NON-NLS-1$
            }

            Packet packet = PacketCollector.getPacket(this.getHierarchyManager(), page, recurse, includeContentNodes);
            ObjectOutputStream os = new ObjectOutputStream(response.getOutputStream());
            os.writeObject(packet.getBody().getObject());
            os.flush();
        }
    }

    private HierarchyManager getHierarchyManager() throws Exception {
        return this.hierarchyManager;
    }

    protected String getOperatedHandle(HttpServletRequest request) {
        return request.getHeader(Syndicator.PAGE);
    }

    /**
     * Exclude version number.
     */
    private String getProtocolName(HttpServletRequest request) {
        String protocol = request.getProtocol();
        return StringUtils.substringBeforeLast(protocol, "/"); //$NON-NLS-1$
    }
}
