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
package info.magnolia.cms.servlets;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.exchange.simple.ContentWriter;
import info.magnolia.cms.exchange.simple.PacketCollector;
import info.magnolia.cms.exchange.simple.Syndicator;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.Listener;
import info.magnolia.cms.security.Lock;
import info.magnolia.cms.security.SecureURI;
import info.magnolia.cms.security.SessionAccessControl;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class SimpleExchange extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SimpleExchange.class);

    private String context;

    private String page;

    private String parent;

    private String action;

    private String type; // only used if action is get

    private String recursive;

    private String protocol;

    private String host;

    private String remotePort;

    private String senderURL;

    private String objectType;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private HierarchyManager hierarchyManager;

    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.request = request;
        this.response = response;
        this.context = this.request.getHeader(Syndicator.WORKING_CONTEXT);
        this.page = this.request.getHeader(Syndicator.PAGE);
        this.parent = this.request.getHeader(Syndicator.PARENT);
        this.recursive = this.request.getHeader(Syndicator.RECURSIVE);
        this.action = this.request.getHeader(Syndicator.ACTION);
        this.type = this.request.getHeader(Syndicator.GET_TYPE);
        this.protocol = getProtocolName();
        this.host = request.getRemoteHost();
        this.remotePort = this.request.getHeader(Syndicator.REMOTE_PORT);
        this.senderURL = this.request.getHeader(Syndicator.SENDER_URL);
        if (this.senderURL == null || this.senderURL.equals("")) {
            this.senderURL = this.protocol + "://" + this.host + ":" + this.remotePort;
        }
        this.objectType = this.request.getHeader(Syndicator.OBJECT_TYPE);
        try {
            response.setContentType("text/plain");
            this.handleActivationRequest();
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
    private void handleActivationRequest() throws Exception {
        if (ConfigLoader.isConfigured()) { // ignore security is server is not configured
            if (!Listener.isAllowed(this.request)) {
                return;
            }
            if (!Authenticator.authenticate(this.request)) {
                return;
            }
            this.hierarchyManager = SessionAccessControl.getHierarchyManager(this.request, this.context);
        }
        else {
            this.hierarchyManager = ContentRepository.getHierarchyManager(this.context);
        }
        if (this.action.equals(Syndicator.ACTIVATE)) {
            activate();
        }
        else if (this.action.equals(Syndicator.DE_ACTIVATE)) {
            deactivate();
        }
        else if (this.action.equals(Syndicator.GET)) {
            get();
        }
        else {
            throw new UnsupportedOperationException("Method not supported by Exchange protocol - Simple (.01)");
        }
    }

    /**
     * @throws Exception
     */
    public void activate() throws Exception {
        log.info("Exchange : update request received for " + this.page);
        String handle = "/" + Syndicator.DEFAULT_HANDLER;
        URL url = new URL(this.senderURL + handle);
        String credentials = this.request.getHeader("Authorization");
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", credentials);
        urlConnection.addRequestProperty(Syndicator.ACTION, Syndicator.GET);
        urlConnection.addRequestProperty(Syndicator.WORKING_CONTEXT, this.context);
        urlConnection.addRequestProperty(Syndicator.PAGE, this.page);
        urlConnection.addRequestProperty(Syndicator.PARENT, this.parent);
        urlConnection.addRequestProperty(Syndicator.GET_TYPE, Syndicator.GET_TYPE_SERIALIZED_OBJECT);
        urlConnection.addRequestProperty(Syndicator.RECURSIVE, this.recursive);
        urlConnection.addRequestProperty(Syndicator.OBJECT_TYPE, this.objectType);
        /* Import activated page */
        InputStream in = urlConnection.getInputStream();
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            Object sc = objectInputStream.readObject();
            /* deserialize received object */
            ContentWriter contentWriter = new ContentWriter(this.getHierarchyManager(), this.context, this.senderURL
                + "/"
                + Syndicator.DEFAULT_HANDLER, this.request);
            contentWriter.writeObject(this.parent, sc);
        }
        catch (Exception e) {
            log.error("Failed to de-serialize - " + this.page);
            log.error(e.getMessage(), e);
        }
        // todo , find a better way to lock only this context->hierarchy
        Lock.setSystemLock();
        CacheHandler.flushCache();
        Lock.resetSystemLock();
        // SecureURI.add(this.request, this.page);
    }

    /**
     * @throws Exception
     */
    public void deactivate() throws Exception {
        log.info("Exchange : remove request received for " + this.page);
        HierarchyManager hm = this.getHierarchyManager();
        hm.delete(this.page);
        hm.save();
        CacheHandler.flushCache();
        SecureURI.delete(this.page);
        SecureURI.delete(this.page + "/*");
    }

    /**
     * @throws Exception
     */
    private void get() throws Exception {
        if (this.type.equalsIgnoreCase(Syndicator.GET_TYPE_SERIALIZED_OBJECT)) {
            this.getSerializedObject();
        }
        else if (this.type.equalsIgnoreCase(Syndicator.GET_TYPE_BINARY)) {
            this.getBinary();
        }
        else {
            this.getSerializedObject(); // default type, supporting magnolia 1.1
        }
    }

    private void getSerializedObject() throws Exception {
        log.info("Serialized object request for " + this.page);
        boolean recurse = (new Boolean(this.recursive)).booleanValue();
        Packet packet = PacketCollector.getPacket(this.getHierarchyManager(), this.page, recurse);
        ObjectOutputStream os = new ObjectOutputStream(this.response.getOutputStream());
        os.writeObject(packet.getBody().getObject());
        os.flush();
    }

    /**
     *
     *
     */
    private void getBinary() throws Exception {
        log.info("Binary request for " + this.page);
        HierarchyManager hm = this.getHierarchyManager();
        try {
            InputStream is = hm.getNodeData(this.page).getValue().getStream();
            ServletOutputStream os = this.response.getOutputStream();
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
            os.flush();
            os.close();
        }
        catch (PathNotFoundException e) {
            log.error("Unable to spool " + this.page);
            throw new PathNotFoundException(e.getMessage());
        }
    }

    private HierarchyManager getHierarchyManager() throws Exception {
        return this.hierarchyManager;
    }

    public String getOperatedHandle() {
        return this.page;
    }

    /**
     * Exclude version number.
     */
    private String getProtocolName() {
        String protocol = this.request.getProtocol();
        int lastIndexOfSlash = protocol.lastIndexOf("/");
        return protocol.substring(0, lastIndexOfSlash);
    }
}
