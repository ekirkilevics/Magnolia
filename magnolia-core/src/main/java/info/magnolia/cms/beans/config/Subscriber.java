/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
 * @version $Revision$ ($Author$)
 *
 * @deprecated since 3.5, use the new info.magnolia.cms.exchange package
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

