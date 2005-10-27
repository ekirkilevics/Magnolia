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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.SessionAccessControl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Date: May 7, 2004 Time: 05:15:20 PM
 * @author Sameer Charles
 * @version 1.5
 */
public class Syndicator {

    public static final String DEFAULT_CONTEXT = ContentRepository.WEBSITE;

    public static final String DEFAULT_HANDLER = "ActivationHandler"; //$NON-NLS-1$

    /* request headers */
    public static final String ACTIVATE = "activate"; //$NON-NLS-1$

    public static final String DE_ACTIVATE = "deactivate"; //$NON-NLS-1$

    public static final String GET = "get"; //$NON-NLS-1$

    public static final String WORKING_CONTEXT = "context"; //$NON-NLS-1$

    public static final String PAGE = "page"; //$NON-NLS-1$

    public static final String PARENT = "parent"; //$NON-NLS-1$

    public static final String ACTION = "action"; //$NON-NLS-1$

    public static final String RECURSIVE = "recursive"; //$NON-NLS-1$

    public static final String INCLUDE_CONTENTNODES = "includeContentNodes";

    public static final String REMOTE_PORT = "remote-port"; //$NON-NLS-1$

    public static final String SENDER_URL = "senderURL"; //$NON-NLS-1$

    public static final String SENDER_CONTEXT = "senderContext"; //$NON-NLS-1$

    public static final String OBJECT_TYPE = "objectType"; //$NON-NLS-1$

    public static final String GET_TYPE = "gettype"; //$NON-NLS-1$

    public static final String GET_TYPE_BINARY = "binary"; //$NON-NLS-1$

    public static final String GET_TYPE_SERIALIZED_OBJECT = "serializedObject"; //$NON-NLS-1$

    /**
     * return status values all simple activation headers start from sa_
     */
    public static final String ACTIVATION_SUCCESSFUL = "sa_success"; //$NON-NLS-1$

    public static final String ACTIVATION_FAILED = "sa_failed"; //$NON-NLS-1$

    public static final String ACTIVATION_ATTRIBUTE_STATUS = "sa_attribute_status"; //$NON-NLS-1$

    public static final String ACTIVATION_ATTRIBUTE_MESSAGE = "sa_attribute_message"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Syndicator.class);

    private HttpServletRequest request;

    private String context;

    private String parent;

    private String path;

    private boolean recursive;

    private boolean includeContentNodes;

    public Syndicator(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * <p>
     * this will activate specifies page (sub pages) to all configured subscribers
     * </p>
     * @param context repository ID as configured
     * @param parent parent under which this page will be activated
     * @param path page to be activated
     * @param recursive
     * @throws RepositoryException 
     * @throws ActivationException 
     */
    public synchronized void activate(String context, String parent, String path, boolean recursive,
        boolean includeContentNodes) throws ActivationException, RepositoryException  {
        this.parent = parent;
        this.path = path;
        this.recursive = recursive;
        this.includeContentNodes = includeContentNodes;
        this.context = context;
        this.activate();
    }

    /**
     * <p>
     * this will activate specifies page (sub pages) to the specified subscribers
     * </p>
     * @param subscriber
     * @param context repository ID as configured
     * @param parent parent under which this page will be activated
     * @param path page to be activated
     * @param recursive
     */
    public synchronized void activate(Subscriber subscriber, String context, String parent, String path,
        boolean recursive) throws Exception {
        this.parent = parent;
        this.path = path;
        this.recursive = recursive;
        this.context = context;
        this.activate(subscriber);
    }

    /**
     * @throws RepositoryException 
     * @throws ActivationException 
     * @deprecated use activate(String context, String parent, String path, boolean recursive) instead
     */
    public synchronized void activate(String parent, String path, boolean recursive) throws ActivationException, RepositoryException{
        this.parent = parent;
        this.path = path;
        this.recursive = recursive;
        this.context = DEFAULT_CONTEXT;
        this.activate();
    }

    /**
     * @throws RepositoryException 
     * @throws ActivationException 
     */
    private synchronized void activate() throws ActivationException, RepositoryException {
        Enumeration en = Subscriber.getList();
        while (en.hasMoreElements()) {
            Subscriber si = (Subscriber) en.nextElement();
            if (si.isActive()) {
                activate(si);
            }
        }
    }

    /**
     * <p>
     * send activation request only if subscribed to the activated URI
     * </p>
     */
    private synchronized void activate(Subscriber subscriber) throws ActivationException, RepositoryException {
        if (!isSubscribed(subscriber)) {
            if (log.isDebugEnabled()) {
                log.debug("Exchange : subscriber [ " + subscriber.getName() + " ] is not subscribed to " + this.path); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Exchange : sending activation request to " + subscriber.getName()); //$NON-NLS-1$
            log.debug("Exchange : user [ " + Authenticator.getUserId(this.request) + " ]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String handle = getActivationURL(subscriber);
        try {
            URL url = new URL(handle);
            URLConnection urlConnection = url.openConnection();
            this.addActivationHeaders(urlConnection, subscriber);
            String status = urlConnection.getHeaderField(Syndicator.ACTIVATION_ATTRIBUTE_STATUS);

            // check if the activation failed
            if (StringUtils.equals(status, Syndicator.ACTIVATION_FAILED)) {
                String message = urlConnection.getHeaderField(Syndicator.ACTIVATION_ATTRIBUTE_MESSAGE);
                throw new ActivationException("Message received from subscriber: " + message);
            }
            urlConnection.getContent();
            log.info("Exchange : activation request received by " + subscriber.getName()); //$NON-NLS-1$
            updateActivationDetails();
        }
        catch (ActivationException e) {
            throw e;
        }
        catch (MalformedURLException e) {
            throw new ActivationException("Wrong URL for subscriber " + subscriber  + "[" + handle + "]");
        }
        catch (IOException e) {
            throw new ActivationException("Was not able to send the activation request [" +  handle + "]: " + e.getMessage());
        }
        catch (RepositoryException e) {
            throw e;
        }
    }

    private boolean isSubscribed(Subscriber subscriber) {
        boolean isSubscribed = false;
        List subscribedURIList = subscriber.getContext(this.context);
        for (int i = 0; i < subscribedURIList.size(); i++) {
            String uri = (String) subscribedURIList.get(i);
            if (this.path.equals(uri)) {
                isSubscribed = true;
            }
            else if (this.path.startsWith(uri + "/")) { //$NON-NLS-1$
                isSubscribed = true;
            }
            else if (uri.endsWith("/") && (this.path.startsWith(uri))) { //$NON-NLS-1$
                isSubscribed = true;
            }
        }
        return isSubscribed;
    }

    /**
     * @param path , to deactivate
     * @param context
     * @throws RepositoryException 
     * @throws ActivationException 
     */
    public synchronized void deActivate(String context, String path) throws ActivationException, RepositoryException{
        this.path = path;
        this.context = context;
        this.deActivate();
    }

    /**
     * @param path , to deactivate
     * @param context
     * @param subscriber
     * @throws Exception
     */
    public synchronized void deActivate(Subscriber subscriber, String context, String path) throws Exception {
        this.path = path;
        this.context = context;
        this.deActivate(subscriber);
    }

    /**
     * @param path , to deactivate
     * @throws Exception
     * @deprecated use deActivate(String context, String path)
     */
    public synchronized void deActivate(String path) throws Exception {
        this.path = path;
        this.context = DEFAULT_CONTEXT;
        this.deActivate();
    }

    /**
     * @throws RepositoryException 
     * @throws ActivationException 
     */
    private synchronized void deActivate() throws ActivationException, RepositoryException{
        Enumeration en = Subscriber.getList();
        while (en.hasMoreElements()) {
            Subscriber si = (Subscriber) en.nextElement();
            if (!si.isActive()) {
                continue;
            }
            if (log.isDebugEnabled()) {
                log.debug("Removing [ " + this.path + " ] from [ " + si.getAddress() + " ]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            deActivate(si);
        }
    }

    private synchronized void deActivate(Subscriber subscriber) throws ActivationException, RepositoryException {
        if (!isSubscribed(subscriber)) {
            return;
        }
        String handle = getDeactivationURL(subscriber);
        try {
            URL url = new URL(handle);
            URLConnection urlConnection = url.openConnection();
            this.addDeactivationHeaders(urlConnection);
            urlConnection.getContent();
            updateDeActivationDetails();
        }
        catch (MalformedURLException e) {
            throw new ActivationException("Wrong URL for subscriber " + subscriber  + "[" + handle + "]");
        }
        catch (IOException e) {
            throw new ActivationException("Was not able to send the deactivation request [" +  handle + "]: " + e.getMessage());        }
        catch (RepositoryException e) {
            throw e;
        }
    }

    /**
     *
     */
    private String getDeactivationURL(Subscriber subscriberInfo) {
        String handle = subscriberInfo.getProtocol() + "://" + subscriberInfo.getAddress() + "/" + DEFAULT_HANDLER; //$NON-NLS-1$ //$NON-NLS-2$
        return handle;
    }

    private void addDeactivationHeaders(URLConnection connection) {
        connection.setRequestProperty("Authorization", Authenticator.getCredentials(this.request)); //$NON-NLS-1$
        connection.addRequestProperty("context", this.context); //$NON-NLS-1$
        connection.addRequestProperty("page", this.path); //$NON-NLS-1$
        connection.addRequestProperty("action", "deactivate"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return activation handle
     */
    private String getActivationURL(Subscriber subscriberInfo) {
        String handle = subscriberInfo.getProtocol() + "://" + subscriberInfo.getAddress() + "/" + DEFAULT_HANDLER; //$NON-NLS-1$ //$NON-NLS-2$
        return handle;
    }

    private void addActivationHeaders(URLConnection connection, Subscriber subscriber) throws AccessDeniedException {
        connection.setRequestProperty("Authorization", Authenticator.getCredentials(this.request)); //$NON-NLS-1$
        connection.addRequestProperty("context", this.context); //$NON-NLS-1$
        connection.addRequestProperty("page", this.path); //$NON-NLS-1$
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.request, this.context);
        if (StringUtils.isEmpty(this.parent)) {
            try {
                Content page = hm.getContent(this.path);
                this.parent = page.getParent().getHandle();
            }
            catch (RepositoryException re) {
                log.error("failed to build parent path for - " + this.path); //$NON-NLS-1$
                log.error(re.getMessage(), re);
            }
        }
        connection.addRequestProperty("parent", this.parent); //$NON-NLS-1$
        if (hm.isPage(this.path)) {
            connection.addRequestProperty(Syndicator.OBJECT_TYPE, ItemType.CONTENT.getSystemName());
        }
        else if (hm.isNodeType(this.path, ItemType.CONTENTNODE.getSystemName())) {
            connection.addRequestProperty(Syndicator.OBJECT_TYPE, ItemType.CONTENTNODE.getSystemName());
        }
        else if (hm.isNodeData(this.path)) {
            connection.addRequestProperty(Syndicator.OBJECT_TYPE, ItemType.NT_NODEDATA);
        }
        connection.addRequestProperty("action", "activate"); //$NON-NLS-1$ //$NON-NLS-2$
        connection.addRequestProperty("recursive", BooleanUtils.toStringTrueFalse(this.recursive)); //$NON-NLS-1$
        connection.addRequestProperty(Syndicator.INCLUDE_CONTENTNODES, BooleanUtils
            .toStringTrueFalse(this.includeContentNodes)); //$NON-NLS-1$

        String senderURL = subscriber.getSenderURL();

        if (senderURL == null) {
            // todo remove remotePort property once its tested together with apache
            String remotePort = (new Integer(this.request.getServerPort())).toString();
            connection.addRequestProperty(REMOTE_PORT, remotePort);
            connection.addRequestProperty(SENDER_CONTEXT, this.request.getContextPath());
        }
        else {
            connection.addRequestProperty(SENDER_URL, senderURL);
        }
    }

    /**
     *
     */
    private void updateActivationDetails() throws RepositoryException {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.request, this.context);
        Content page = hm.getContent(this.path);
        updateMetaData(page, Syndicator.ACTIVATE, this.recursive, this.includeContentNodes);
        page.save();
    }

    /**
     */
    private void updateDeActivationDetails() throws RepositoryException {
        HierarchyManager hm = SessionAccessControl.getHierarchyManager(this.request, this.context);
        Content page = hm.getContent(this.path);
        updateMetaData(page, Syndicator.DE_ACTIVATE, true, true);
        page.save();
    }

    /**
     * @param node
     * @param recursive
     * @param includeContentNodes update all subnodes of type CONTENTNODE. Is irrelevant if recursive is true
     */
    private void updateMetaData(Content node, String type, boolean recursive, boolean includeContentNodes)
        throws AccessDeniedException {
        // update the passed node
        MetaData md = node.getMetaData(MetaData.ACTIVATION_INFO);
        if (type.equals(Syndicator.ACTIVATE)) {
            md.setActivated();
        }
        else {
            md.setUnActivated();
        }
        md.setActivatorId(Authenticator.getUserId(this.request));
        md.setLastActivationActionDate();
        md = null;

        // recursive call
        if (recursive || includeContentNodes) {
            Collection children = new ArrayList();

            if (recursive) {
                children.addAll(node.getChildren(ItemType.CONTENT));
            }

            // update the metadata of contentnodes too
            if (recursive || includeContentNodes) {
                children.addAll(node.getChildren(ItemType.CONTENTNODE));
            }

            Iterator iter = children.iterator();
            while (iter.hasNext()) {
                Content child = (Content) iter.next();
                try {
                    updateMetaData(child, type, recursive, includeContentNodes);
                }
                catch (AccessDeniedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
