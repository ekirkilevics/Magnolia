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



package info.magnolia.cms.exchange.ice;


import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.exchange.Channel;
import info.magnolia.exchange.Packet;

import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
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


    private static final String DEFAULT_CONTEXT = ContentRepository.WEBSITE;
    private static final String DEFAULT_HANDLER = "ActivationHandler";
    private static final String EXCHANGE_HANDLER = "Exchange";

    private static final int ACTIVATE = 1;
    private static final int DE_ACTIVATE = 2;


    private HttpServletRequest request;
    private String context;
    private Session contextSession;
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
            log.info("Exchange : subscriber [ "+subscriber.getName()+" ] is not subscribed to "+this.path);
            return;
        }
        log.info("Exchange : sending activation request to "+subscriber.getName());
        log.info("Exchange : user [ "+Authenticator.getUserId(this.request)+" ]");
        String handle = getActivationURL(subscriber);
        URL url = new URL(handle);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization",Authenticator.getCredentials(this.request));
        String remotePort = (new Integer(this.request.getServerPort())).toString();
        urlConnection.addRequestProperty("remote-port",remotePort);
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


    private synchronized void send() {
        Enumeration en = Subscriber.getList();
        while (en.hasMoreElements()) {
            Subscriber si = (Subscriber)en.nextElement();
            try {
                log.info("Syndicating [ "+this.path+" ] to [ "+si.getAddress()+" ]");
                send(si);
            } catch (Exception e) {
                log.error("Failed to syndicated [ "+this.path+" ] to [ "+si.getAddress()+" ]");
                log.error(e.getMessage(), e);
            }
        }

    }



    private synchronized void send(Subscriber subscriber) throws Exception {
        PacketCollector pc = new PacketCollector(subscriber);
        pc.collect(this.contextSession,this.path,1);
        Hashtable packets = pc.getPackets();
        String urlString = subscriber.getProtocol()+"://"+subscriber.getAddress()+"/"+EXCHANGE_HANDLER;
        URL url = new URL(urlString);
        Channel channel
                = ChannelFactory.getChannel(subscriber.getName(),url,Authenticator.getCredentials(this.request));
        Packet packet = (Packet) packets.get(PacketCollector.MAIN_PACKET);
        packet.getHeaders().addHeader(Header.ACTION,Header.ACTION_ADD);
        channel.send(packet);
        packets.remove(PacketCollector.MAIN_PACKET);
        Enumeration enum = packets.keys();
        while (enum.hasMoreElements()) {
            Packet binaryPacket = (Packet) packets.get((String)enum.nextElement());
            binaryPacket.getHeaders().addHeader(Header.ACTION,Header.ACTION_ADD);
            channel.send(binaryPacket);
        }
    }




    private synchronized void remove() {
        Enumeration en = Subscriber.getList();
        while (en.hasMoreElements()) {
            Subscriber si = (Subscriber)en.nextElement();
            try {
                log.info("Removing [ "+this.path+" ] from [ "+si.getAddress()+" ]");
                remove(si);
            } catch(Exception e) {
                log.error("Failed to remove [ "+this.path+" ] from [ "+si.getAddress()+" ]");
                log.error(e.getMessage(), e);
            }
        }
    }



    private synchronized void remove(Subscriber subscriber) throws Exception {
        Packet packet = PacketFactory.getPacket("");
        packet.getHeaders().addHeader(Header.CONTEXT,this.context);
        packet.getHeaders().addHeader(Header.ACTION,Header.ACTION_REMOVE);
        packet.getHeaders().addHeader(Header.PATH,this.path);
        String urlString = subscriber.getProtocol()+"://"+subscriber.getAddress()+"/"+EXCHANGE_HANDLER;
        URL url = new URL(urlString);
        Channel channel
                = ChannelFactory.getChannel(subscriber.getName(),url,Authenticator.getCredentials(this.request));
        channel.send(packet);
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
        urlConnection.setRequestProperty("Authorization",Authenticator.getCredentials(this.request));
        urlConnection.getContent();
        updateDeActivationDetails();
    }




    /**
     *
     * */
    private String getDeactivationURL(Subscriber subscriberInfo) {
        String handle = subscriberInfo.getProtocol()+"://"+subscriberInfo.getAddress()
                + "/"+DEFAULT_HANDLER+"?context="+this.context
                +"&page="+this.path
                +"&action=deactivate";
        return handle;
    }




    /**
     *
     * @return activation handle
     */
    private String getActivationURL(Subscriber subscriberInfo) {
        String handle = subscriberInfo.getProtocol()+"://"+subscriberInfo.getAddress()
                +"/"+DEFAULT_HANDLER+"?context="+this.context
                +"&page="+this.path
                +"&parent="+this.parent
                +"&action=activate&recursive="+(new Boolean(this.recursive)).toString();
        return handle;
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
     * todo update activation details for appropriate  context
     */
    private void updateActivationDetails()  throws RepositoryException {
        HierarchyManager hm = new HierarchyManager(this.request);
        hm.init(SessionAccessControl.getSession(this.request).getRootNode());
        Content page = hm.getPage(this.path);
        updateMetaData(page,Syndicator.ACTIVATE);
        if (this.recursive)
            this.updateTree(page,Syndicator.ACTIVATE);
    }



    /**
     */
    private void updateDeActivationDetails()  throws RepositoryException {
        HierarchyManager hm = new HierarchyManager(this.request);
        hm.init(SessionAccessControl.getSession(this.request).getRootNode());
        Content page = hm.getPage(this.path);
        updateMetaData(page,Syndicator.DE_ACTIVATE);
        this.updateTree(page,Syndicator.DE_ACTIVATE);
    }



    /**
     * @param startPage
     */
    private void updateTree(Content startPage, int type) {
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
    private void updateMetaData(Content page, int type) {
        MetaData md = page.getMetaData(MetaData.ACTIVATION_INFO);
        if (type == Syndicator.ACTIVATE)
            md.setActivated();
        else
            md.setUnActivated();
        md.setActivatorId(Authenticator.getUserId(this.request));
        md.setLastActivationActionDate();
        md = null;
    }





}
