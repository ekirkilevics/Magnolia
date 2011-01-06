/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.exchangesimple.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.exchangesimple.DefaultActivationManager;
import info.magnolia.module.exchangesimple.DefaultSubscriber;
import info.magnolia.module.exchangesimple.DefaultSubscription;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Updates configuration and naming of activation related nodes in Magnolia as part of 3.0 upgrade.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class UpdateActivationConfigTask extends AbstractRepositoryTask {
    public static final String EE30_ROOT_PATH = "/subscribers";
    public static final String CE30_ROOT_PATH = "/subscriber";
    private static final String DEFAULT_SUBSCRIBER_NAME = "default";

    public UpdateActivationConfigTask() {
        super("New subscribers configuration", "The subscriber configuration structure changed.");
    }

    // TODO exception handling ?
    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getConfigHierarchyManager();
        if (hm.isExist(EE30_ROOT_PATH)) {
            final Collection subscribers = new ArrayList();
            final Content subscribersParent = hm.getContent(EE30_ROOT_PATH);
            final Collection subscriberNodes = subscribersParent.getChildren(ItemType.CONTENTNODE);
            final Iterator it = subscriberNodes.iterator();
            while (it.hasNext()) {
                final Content subscriberNode = (Content) it.next();
                final String subscriberName = subscriberNode.getName();
                final Subscriber subscriber = from30ConfigLayout(subscriberNode, subscriberName);
                subscribers.add(subscriber);
            }

            to35Layout(hm, subscribers);

            subscribersParent.delete();
        } else if (hm.isExist(CE30_ROOT_PATH)) {
            final Content subscriberNode = hm.getContent(CE30_ROOT_PATH);
            final Subscriber subscriber = from30ConfigLayout(subscriberNode, DEFAULT_SUBSCRIBER_NAME);
            to35Layout(hm, Collections.singleton(subscriber));
            subscriberNode.delete();
        } else {
            throw new TaskExecutionException("Couldn't find " + CE30_ROOT_PATH + " nor " + EE30_ROOT_PATH + " node, can't update activation subscribers configuration.");
        }
    }

    protected void to35Layout(HierarchyManager hm, Collection subscribers) throws RepositoryException {
        final Content activationNode = hm.getContent("/server").createContent("activation");
        activationNode.createNodeData("class", DefaultActivationManager.class.getName());
        final Content subscribersNode = activationNode.createContent("subscribers");
        final Iterator it = subscribers.iterator();
        while (it.hasNext()) {
            final Subscriber subscriber = (Subscriber) it.next();
            final Content subscriberNode = subscribersNode.createContent(subscriber.getName(), ItemType.CONTENTNODE);
            subscriberNode.createNodeData("URL", subscriber.getURL());
            subscriberNode.createNodeData("active", Boolean.toString(subscriber.isActive()));
            subscriberNode.createNodeData("class", DefaultSubscriber.class.getName());
            final Content subscriptionsNode = subscriberNode.createContent("subscriptions", ItemType.CONTENTNODE);
            final Collection subscriptions = subscriber.getSubscriptions();
            final Iterator subscriptionsIt = subscriptions.iterator();
            while (subscriptionsIt.hasNext()) {
                final Subscription subscription = (Subscription) subscriptionsIt.next();
                final Content subscriptionNode = subscriptionsNode.createContent(subscription.getName(), ItemType.CONTENTNODE);
                subscriptionNode.createNodeData("repository", subscription.getRepository());
                subscriptionNode.createNodeData("fromURI", subscription.getFromURI());
                subscriptionNode.createNodeData("toURI", subscription.getToURI());
            }
        }
    }

    protected Subscriber from30ConfigLayout(Content subscriberNode, String subscriberName) throws RepositoryException {
        final String url = getString(subscriberNode, "URL");
        final String active = getString(subscriberNode, "active");
        final Subscriber subscriber = new DefaultSubscriber();
        subscriber.setName(subscriberName);
        subscriber.setURL(url);
        subscriber.setActive(Boolean.valueOf(active).booleanValue());

        final ArrayList subscriptions = new ArrayList();
        final Content contextNode = subscriberNode.getContent("context");
        final Iterator contexts = contextNode.getChildren().iterator();
        while (contexts.hasNext()) {
            final Content context = (Content) contexts.next();
            addSubscriptionsFromContext(context, subscriptions);
        }
        subscriber.setSubscriptions(subscriptions);
        return subscriber;
    }

    private void addSubscriptionsFromContext(Content context, Collection subscriptions) throws RepositoryException {
        final String repoName = context.getName();
        final Collection repoSubs = context.getChildren();
        final Iterator it = repoSubs.iterator();
        while (it.hasNext()) {
            final Content repoSub = (Content) it.next();
            final String subId = repoSub.getName();
            final String subscribedURI = getString(repoSub, "subscribedURI");

            final Subscription subscription = new DefaultSubscription();
            subscription.setName(repoName + subId);
            subscription.setRepository(repoName);
            subscription.setFromURI(subscribedURI);
            subscription.setToURI(subscribedURI);

            subscriptions.add(subscription);
        }

    }

    // TODO exception handling ? (UnexpectedRepoStateException?)
    private String getString(Content node, String propertyName) throws RepositoryException {
        if (node.hasNodeData(propertyName)) {
            return node.getNodeData(propertyName).getString();
        }
        throw new IllegalStateException("No property of name " + propertyName + " was found.");
    }
}
