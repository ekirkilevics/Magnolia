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
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Sameer Charles
 * $Id$
 */
public class DefaultSubscriber implements Subscriber {

    private String url;

    private boolean active;

    private String name;

    Collection subscriptions = new ArrayList();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSubscriptions(Collection subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Collection getSubscriptions() {
        return this.subscriptions;
    }

    public void addSubscriptions(Subscription subscription) {
        this.subscriptions.add(subscription);
    }

    public Subscription getMatchedSubscription(String path, String repositoryId) {
        Iterator subscriptions = this.getSubscriptions().iterator();
        Subscription matchedSubscription = null;
        int highestVote = -1;
        while (subscriptions.hasNext()) {
            Subscription subscription = (Subscription) subscriptions.next();
            if (repositoryId.equalsIgnoreCase(subscription.getRepository())) {
                int vote = subscription.vote(path);
                if (highestVote < vote) {
                    highestVote = vote;
                    matchedSubscription = subscription;
                }
            }
        }
        return matchedSubscription;
    }

    public boolean isSubscribed(String path, String repositoryId) {
        return (null != this.getMatchedSubscription(path, repositoryId));
    }

    public String getDocumentBase(String path, String repositoryId) {
        String documentBase = null;
        Subscription subscription = this.getMatchedSubscription(path, repositoryId);
        if (null != subscription) {
            documentBase = subscription.getToURI();
            if (null == documentBase) {
                // if this property is not present get subscribed URI since the result after
                // string replacement will be the same
                documentBase = subscription.getFromURI();
            }
        }
        return documentBase;
    }

    public String getSubscribedPath(String path, String repositoryId) {
        Subscription subscription = this.getMatchedSubscription(path, repositoryId);
        String subscribedPath = null;
        if (null != subscription) {
            subscribedPath = subscription.getFromURI();
        }
        return subscribedPath;
    }

}
