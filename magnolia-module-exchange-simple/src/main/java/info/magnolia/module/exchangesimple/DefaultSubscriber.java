/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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

    private String authMethod = "Basic";

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

    public String getAuthenticationMethod() {
        return this.authMethod;
    }

    public void setAuthenticationMethod(String method) {
        this.authMethod  = method;
    }

}
