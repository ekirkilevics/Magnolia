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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import javax.jcr.RepositoryException;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public class Subscriber {

    private static Logger log = Logger.getLogger(Subscriber.class);

    private static final String START_PAGE = "subscribers";

    private static Iterator ipList;

    private static Hashtable cachedContent = new Hashtable();

    private String name;

    private Hashtable context;

    private Hashtable params;

    /**
     * constructor
     */
    public Subscriber() {
    }

    /**
     * <p>
     * reads listener config from the config repository and caches its content in to the hash table
     * </p>
     */
    public static void init() {
        Subscriber.cachedContent.clear();
        try {
            log.info("Config : loading Subscribers");
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getPage(START_PAGE);
            Collection children = startPage.getContentNode("SubscriberConfig").getChildren();
            if (children != null) {
                Subscriber.ipList = children.iterator();
                Subscriber.cacheContent();
            }
            log.info("Config : Subscribers loaded");
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Subscribers");
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() {
        log.info("Config : re-loading Sunscribers");
        Subscriber.init();
    }

    /**
     * <p>
     * Cache listener content from the config repository
     * </p>
     */
    private static void cacheContent() {
        while (Subscriber.ipList.hasNext()) {
            ContentNode c = (ContentNode) Subscriber.ipList.next();
            Subscriber si = new Subscriber();
            si.params = new Hashtable();
            Iterator paramList = c.getChildren(ItemType.NT_NODEDATA).iterator();
            while (paramList.hasNext()) {
                NodeData nd = (NodeData) paramList.next();
                si.params.put(nd.getName(), nd.getString());
            }
            // si.address = c.getNodeData("address").getString();
            // si.protocol = c.getNodeData("protocol").getString();
            // si.requestConfirmation = c.getNodeData("requestConfirmation").getBoolean();
            si.name = c.getName();
            /* all context info */
            try {
                addContext(si, c);
            }
            catch (Exception e) {/* valid */
            }
            Subscriber.cachedContent.put(c.getName(), si);
        }
        Subscriber.ipList = null;
    }

    /**
     * <p>
     * Adds context datail to cache
     * </p>
     * @param subscriberInfo
     * @param contentNode
     */
    private static void addContext(Subscriber subscriberInfo, ContentNode contentNode) throws Exception {
        subscriberInfo.context = new Hashtable();
        ContentNode contextList = contentNode.getContentNode("Context");
        Iterator it = contextList.getChildren().iterator();
        while (it.hasNext()) {
            ContentNode context = (ContentNode) it.next();
            Iterator contextDetails = context.getChildren().iterator();
            ArrayList list = new ArrayList();
            while (contextDetails.hasNext()) {
                ContentNode map = (ContentNode) contextDetails.next();
                list.add(map.getNodeData("subscribedURI").getString());
            }
            subscriberInfo.context.put(context.getName(), list);
        }
    }

    /**
     * <p>
     * get list of all configured ip
     * </p>
     * @return Enumeration
     */
    public static Enumeration getList() {
        return Subscriber.cachedContent.elements();
    }

    /**
     * @return SubscriberInfo object as configured
     */
    public static Subscriber getSubscriber(String name) {
        return (Subscriber) Subscriber.cachedContent.get(name);
    }

    /**
     * @return param value
     */
    public String getParam(String id) {
        return (String) this.params.get(id);
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return context details
     */
    public ArrayList getContext(String name) {
        if (this.context.get(name) == null)
            return null;
        return (ArrayList) this.context.get(name);
    }
}
