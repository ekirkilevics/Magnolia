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



package info.magnolia.cms.exchange.simple;


import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SessionAccessControl;

import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Hashtable;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;



/**
 * Date: May 7, 2004
 * Time: 05:15:20 PM
 *
 * 
 * @author Sameer Charles
 * @version 1.5
 */


public class Syndicator {


    private static Logger log = Logger.getLogger(Syndicator.class);


    public static final String DEFAULT_CONTEXT = ContentRepository.WEBSITE;
    public static final String DEFAULT_HANDLER = "ActivationHandler";

    /* request headers */
    public static final String ACTIVATE = "activate";
    public static final String DE_ACTIVATE = "deactivate";
    public static final String GET = "get";
    public static final String WORKING_CONTEXT = "context";
    public static final String PAGE = "page";
    public static final String PARENT = "parent";
    public static final String ACTION = "action";
    public static final String RECURSIVE = "recursive";
    public static final String REMOTE_PORT = "remote-port";
    public static final String OBJECT_TYPE = "objectType";
    public static final String GET_TYPE = "gettype";
    public static final String GET_TYPE_BINARY = "binary";
    public static final String GET_TYPE_SERIALIZED_OBJECT = "serializedObject";


    private HttpServletRequest request;
    private String context;
    private String parent;
    private String path;
    private boolean recursive;




    public Syndicator(HttpServletRequest request) {
        this.request = request;
    }




    /**
     *
     * */
    public synchronized void activate(String context,
                                      String parent,
                                      String path,
                                      boolean recursive)
            throws Exception {
        this.parent = parent;
        this.path = path;
        this.recursive = recursive;
        this.context = context;
        this.activate();
    }




    /**
     *
     * */
    public synchronized void activate(Subscriber subscriber,
                                      String context,
                                      String parent,
                                      String path,
                                      boolean recursive)
            throws Exception {
        this.parent = parent;
        this.path = path;
        this.recursive = recursive;
        this.context = context;
        this.activate(subscriber);
    }




    /**
     * @deprecated
     * */
    public synchronized void activate(String parent, String path, boolean recursive)
            throws Exception {
        this.parent = parent;
        this.path = path;
        this.recursive = recursive;
        this.context = DEFAULT_CONTEXT;
        this.activate();
    }




    /**
     *
     * @throws Exception
     * */
    private synchronized void activate() throws Exception {
        Enumeration en = Subscriber.getList();
        while (en.hasMoreElements()) {
            Subscriber si = (Subscriber)en.nextElement();
            activate(si);
        }
    }




    /**
     * <p>
     * send activation request only if subscribed to the activated URI
     * </p>
     *
     * @throws Exception
     * */
    private synchronized void activate(Subscriber subscriber) throws Exception {
        if (!isSubscribed(subscriber)) {
            System.out.println("not sunbcribed....");
            log.info("Exchange : subscriber [ "+subscriber.getName()+" ] is not subscribed to "+this.path);
            return;
        }
        log.info("Exchange : sending activation request to "+subscriber.getName());
        log.info("Exchange : user [ "+Authenticator.getUserId(this.request)+" ]");
        String handle = getActivationURL(subscriber);
        URL url = new URL(handle);
        URLConnection urlConnection = url.openConnection();

        this.addActivationHeaders(urlConnection);

        urlConnection.getContent();
        log.info("Exchange : activation request received by "+subscriber.getName());
        updateActivationDetails();
    }



    private boolean isSubscribed(Subscriber subscriber) {
        boolean isSubscribed = false;
        ArrayList subscribedURIList = subscriber.getContext(this.context);
        for (int i=0; i<subscribedURIList.size(); i++) {
            String uri = (String) subscribedURIList.get(i);
            if (this.path.equals(uri))
                isSubscribed = true;
            else if (this.path.startsWith(uri+"/"))
                isSubscribed = true;
            else if (uri.endsWith("/") && (this.path.startsWith(uri)))
                isSubscribed = true;
        }

        return isSubscribed;
    }



    /**
     *
     * @param path , to deactivate
     * @param context
     * @throws Exception
     * */
    public synchronized void deActivate(String context, String path) throws Exception {
        this.path = path;
        this.context = context;
        this.deActivate();
    }




    /**
     *
     * @param path , to deactivate
     * @param context
     * @param subscriber
     * @throws Exception
     * */
    public synchronized void deActivate(Subscriber subscriber,
                                        String context,
                                        String path)
            throws Exception {
        this.path = path;
        this.context = context;
        this.deActivate(subscriber);
    }



    /**
     *
     * @param path , to deactivate
     * @throws Exception
     * @deprecated
     * */
    public synchronized void deActivate(String path) throws Exception {
        this.path = path;
        this.context = DEFAULT_CONTEXT;
        this.deActivate();
    }




    /**
     */
    private synchronized void deActivate() {
        Enumeration en = Subscriber.getList();
        while (en.hasMoreElements()) {
            Subscriber si = (Subscriber)en.nextElement();
            try {
                log.info("Removing [ "+this.path+" ] from [ "+si.getAddress()+" ]");
                deActivate(si);
            } catch (Exception e) {
                log.error("Failed to remove [ "+this.path+" ] from [ "+si.getAddress()+" ]");
                log.error(e.getMessage(), e);
            }
        }
    }



    /**
     * @throws Exception
     * */
    private synchronized void deActivate(Subscriber subscriber)
            throws Exception {
        if (!isSubscribed(subscriber))
            return;
        String handle = getDeactivationURL(subscriber);
        URL url = new URL(handle);
        URLConnection urlConnection = url.openConnection();
        this.addDeactivationHeaders(urlConnection);
        urlConnection.getContent();
        updateDeActivationDetails();
    }




    /**
     *
     * */
    private String getDeactivationURL(Subscriber subscriberInfo) {
        String handle = subscriberInfo.getProtocol()+"://"+subscriberInfo.getAddress()
                + "/"+DEFAULT_HANDLER;
        return handle;
    }




    private void addDeactivationHeaders(URLConnection connection) {
        connection.setRequestProperty("Authorization",Authenticator.getCredentials(this.request));
        connection.addRequestProperty("context", this.context);
        connection.addRequestProperty("page", this.path);
        connection.addRequestProperty("action", "deactivate");
    }




    /**
     *
     * @return activation handle
     */
    private String getActivationURL(Subscriber subscriberInfo) {
        String handle = subscriberInfo.getProtocol()+"://"+subscriberInfo.getAddress()
                +"/"+DEFAULT_HANDLER;
        return handle;
    }



    private void addActivationHeaders(URLConnection connection) {
        connection.setRequestProperty("Authorization",Authenticator.getCredentials(this.request));
        connection.addRequestProperty("context", this.context);
        connection.addRequestProperty("page", this.path);
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.request,this.context);
        if (this.parent == null || this.parent.equals("")) {
            try {
                Content page = hm.getContent(this.path);
                this.parent = page.getParent().getHandle();
            } catch (RepositoryException re) {
                log.error("failed to build parent path for - "+this.path);
                log.error(re.getMessage(), re);
            }
        }
        connection.addRequestProperty("parent", this.parent);
        if (hm.isPage(this.path)) {
            connection.addRequestProperty(Syndicator.OBJECT_TYPE, ItemType.NT_CONTENT);
        } else if (hm.isContentNode(this.path)) {
            connection.addRequestProperty(Syndicator.OBJECT_TYPE, ItemType.NT_CONTENTNODE);
        } else if (hm.isNodeData(this.path)) {
            connection.addRequestProperty(Syndicator.OBJECT_TYPE, ItemType.NT_NODEDATA);
        }

        connection.addRequestProperty("action", "activate");
        connection.addRequestProperty("recursive", (new Boolean(this.recursive)).toString());
        String remotePort = (new Integer(this.request.getServerPort())).toString();
        connection.addRequestProperty("remote-port",remotePort);
    }



    /**
     * @deprecated
     * */
    private void updateDestination(Subscriber subscriberInfo) {
        ArrayList list = subscriberInfo.getContext(this.context);
        if (list == null) return;
        for (int i=0; i<list.size(); i++) {
            Hashtable map = (Hashtable)list.get(i);
            if (this.path.indexOf(((String)map.get("source"))) == 0) { /* match, assign and exit */
                this.parent.replaceFirst((String)map.get("source"),(String)map.get("destination"));
                break;
            }
        }
    }



    /**
     *
     */
    private void updateActivationDetails()  throws RepositoryException {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.request,this.context);
        Content page = hm.getPage(this.path);
        updateMetaData(page,Syndicator.ACTIVATE);
        if (this.recursive)
            this.updateTree(page,Syndicator.ACTIVATE);
        page.save();
    }



    /**
     */
    private void updateDeActivationDetails()  throws RepositoryException {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.request,this.context);
        Content page = hm.getPage(this.path);
        updateMetaData(page,Syndicator.DE_ACTIVATE);
        this.updateTree(page,Syndicator.DE_ACTIVATE);    
    }



    /**
     * @param startPage
     */
    private void updateTree(Content startPage, String type) {
        Iterator children = startPage.getChildren().iterator();
        while (children.hasNext()) {
            Content aPage = (Content)children.next();
            updateMetaData(aPage,type);
            if (aPage.hasChildren())
                updateTree(aPage,type);
        }
    }



    /**
     * @param page
     */
    private void updateMetaData(Content page, String type) {
        MetaData md = page.getMetaData(MetaData.ACTIVATION_INFO);
        if (type.equals(Syndicator.ACTIVATE))
            md.setActivated();
        else
            md.setUnActivated();
        md.setActivatorId(Authenticator.getUserId(this.request));
        md.setLastActivationActionDate();
        md = null;
    }





}
