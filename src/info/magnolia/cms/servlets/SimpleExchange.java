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





/**
 * <p>
 * Version .01 implementation
 * Simple implementation of Exchange interface using serialized objects and
 * binary GET
 * todo -
 * 1. implement incremental delivery
 * 2. concurrent activation
 * 3. context locking
 *
 * </p>
 *
 *
 * User: sameercharles
 * Date: Jul 01, 2003
 * Time: 12:06:22 PM
 * @author Sameer Charles
 * @version 2.0
 * */



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.jcr.PathNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

import info.magnolia.cms.security.Listener;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.security.Lock;
import info.magnolia.cms.beans.runtime.SecureURI;
import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.exchange.simple.SerializableContent;
import info.magnolia.cms.exchange.simple.PacketCollector;
import info.magnolia.cms.exchange.simple.ContentWriter;
import info.magnolia.cms.exchange.simple.Syndicator;
import info.magnolia.exchange.Packet;
import org.apache.log4j.Logger;






public class SimpleExchange extends HttpServlet {



    private static final Logger log = Logger.getLogger(SimpleExchange.class);

    private String context;
    private String page;
    private String parent;
    private String action;
    private String type; // only used if action is get
    private String recursive;
    private String protocol;
    private String host;
    private String remotePort;
    private HttpServletRequest request;
    private HttpServletResponse response;





    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * */
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
        try {
            response.setContentType("text/plain");
            this.handleActivationRequest();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }



    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }



    /**
     *
     * @throws Exception
     */
    private void handleActivationRequest() throws Exception {
        if (!Listener.isAllowed(this.request))
            return;
        if (!Authenticator.authenticate(this.request))
            return;
        if (this.action.equals(Syndicator.ACTIVATE))
            activate();
        else if(this.action.equals(Syndicator.DE_ACTIVATE))
            deactivate();
        else if (this.action.equals(Syndicator.GET))
            get();
        else
            throw new UnsupportedOperationException
                    ("Method not supported by Exchange protocol - Simple (.01)");
    }



    /**
     *
     * @throws Exception
     * */
    public void activate() throws Exception {
        log.info("Exchange : update request received for "+this.page);
        String handle = "/"+Syndicator.DEFAULT_HANDLER;

        URL url = new URL(this.protocol+"://"+this.host+":"+this.remotePort+handle);
        String credentials = this.request.getHeader("Authorization");
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization",credentials);
        urlConnection.addRequestProperty(Syndicator.ACTION,Syndicator.GET);
        urlConnection.addRequestProperty(Syndicator.WORKING_CONTEXT,this.context);
        urlConnection.addRequestProperty(Syndicator.PAGE,this.page);
        urlConnection.addRequestProperty(Syndicator.PARENT,this.parent);
        urlConnection.addRequestProperty(Syndicator.GET_TYPE,Syndicator.GET_TYPE_SERIALIZED_OBJECT);
        urlConnection.addRequestProperty(Syndicator.RECURSIVE,this.recursive);

        /* Import activated page */
        InputStream in = urlConnection.getInputStream();
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            SerializableContent sc = (SerializableContent) objectInputStream.readObject();
            /* deserialize received object */
            (new ContentWriter(this.getHierarchyManager(), this.context,
                    this.protocol+"://"+this.host+":"+this.remotePort+"/"+Syndicator.DEFAULT_HANDLER, this.request))
                    .writeObject(this.parent, sc);
        } catch (Exception e) {
            log.error("Failed to de-serialize - "+this.page);
            log.error(e.getMessage(), e);
        }
        // todo , find a better way to lock only this context->hierarchy
        Lock.setSystemLock();
        CacheHandler.flushCache();
        Lock.resetSystemLock();
        SecureURI.add(this.request, this.page);
    }



    /**
     *
     * @throws Exception
     */
    public void deactivate() throws Exception {
        log.info("Exchange : remove request received for "+this.page);
        HierarchyManager hm = this.getHierarchyManager();
        hm.deletePage(this.page);
        hm.save();
        CacheHandler.flushCache();
        SecureURI.delete(this.page);
        SecureURI.delete(this.page+"/*");
    }



    /**
     *
     * @throws Exception
     * */
    private void get() throws Exception {
        if (this.type.equalsIgnoreCase(Syndicator.GET_TYPE_SERIALIZED_OBJECT))
            this.getSerializedObject();
        else if (this.type.equalsIgnoreCase(Syndicator.GET_TYPE_BINARY))
            this.getBinary();
        else
            this.getSerializedObject(); // default type, supporting magnolia 1.1
    }



    private void getSerializedObject() throws Exception {
        log.info("Serialized object request for "+this.page);
        boolean recurse = (new Boolean(this.recursive)).booleanValue();
        Packet packet = PacketCollector.getPacket(this.getHierarchyManager(), this.page, recurse);
        ObjectOutputStream os = new ObjectOutputStream(this.response.getOutputStream());
        os.writeObject((SerializableContent)packet.getBody().getObject());
        os.flush();
    }


    /**
     *
     *
     * */
    private void getBinary() throws Exception {
        log.info("Binary request for "+this.page);
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
        } catch (PathNotFoundException e) {
            log.error("Unable to spool "+this.page);
            throw new PathNotFoundException(e.getMessage());
        }
    }



    private HierarchyManager getHierarchyManager() throws Exception {
        return SessionAccessControl.getHierarchyManager(this.request, this.context);
    }



    public String getOperatedHandle() {
        return this.page;
    }


    /**
     * Exclude version number
     * */
    private String getProtocolName() {
        String protocol = this.request.getProtocol();
        int lastIndexOfSlash = protocol.lastIndexOf("/");
        return protocol.substring(0,lastIndexOfSlash);
    }


}
