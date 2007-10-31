/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.Subscription;
import info.magnolia.cms.util.DeprecationUtil;
import org.apache.commons.collections.IteratorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 *
 * @deprecated since 3.1, use the new info.magnolia.cms.exchange package
 * @see ActivationManagerFactory#getActivationManager()
 */
public class Subscriber {
    private static void dep() {
        DeprecationUtil.isDeprecated("Use the new info.magnolia.cms.exchange package.");
    }

    private static void dep(String extraMessage) {
        DeprecationUtil.isDeprecated("Use the new info.magnolia.cms.exchange package, " + extraMessage);
    }

    public static void init() {
        dep();
    }

    public static void reload() {
        dep();
    }

    public static boolean isSubscribersEnabled() {
        dep();
        return getActMan().hasAnyActiveSubscriber();
    }

    public static Enumeration getList() {
        dep();
        final Collection subscribers = getActMan().getSubscribers();
        return IteratorUtils.asEnumeration(subscribers.iterator());
    }


    /**
     * @return configured subscriber
     **/
    public static Subscriber getSubscriber() {
        dep("returns the first subscriber, which is maybe not exactly what you need or expect.");
        final Collection subscribers = getActMan().getSubscribers();
        if (subscribers.size() >= 0) {
            final Iterator it = subscribers.iterator();
            info.magnolia.cms.exchange.Subscriber subscriber = (info.magnolia.cms.exchange.Subscriber) it.next();
            return new Subscriber(subscriber);
        }
        return null;
    }

    private static ActivationManager getActMan() {
        return ActivationManagerFactory.getActivationManager();
    }

    private final info.magnolia.cms.exchange.Subscriber wrapped;

    private Subscriber(info.magnolia.cms.exchange.Subscriber subscriber) {
        this.wrapped = subscriber;
    }

    /**
     * Getter for <code>active</code>.
     * @return Returns the active.
     */
    public boolean isActive() {
        dep();
        return wrapped.isActive();
    }

    /**
     * Getter for <code>requestConfirmation</code>.
     * @return Returns the requestConfirmation.
     */
    public boolean getRequestConfirmation() {
        dep("this is not used anymore, as no equivalent in the new API, will return false.");
        return false;
    }

    /**
     * @return name
     */
    public String getName() {
        dep();
        return wrapped.getName();
    }

    /**
     * @return context details
     */
    public List getContext(String name) {
        dep("this has changed, and the returned results probably make very little sense.");
        final List result = new ArrayList();
        final Collection subscriptions = wrapped.getSubscriptions();
        final Iterator it = subscriptions.iterator();
        while (it.hasNext()) {
            final Subscription subscription = (Subscription) it.next();
            result.add(subscription.getToURI());
        }
        return result;
    }

    /**
     * Getter for <code>senderURL</code>.
     * @return Returns the senderURL.
     */
    public String getSenderURL() {
        dep("this is not used anymore, as no equivalent in the new API, will return null.");
        return null;
    }

    /**
     * Returns the url of the subscriber, in the form <code>protocol://server:port/context/</code> (always with the
     * leading "/")
     * @return Returns the url.
     */
    public String getURL() {
        dep();
        return wrapped.getURL();
    }

}

