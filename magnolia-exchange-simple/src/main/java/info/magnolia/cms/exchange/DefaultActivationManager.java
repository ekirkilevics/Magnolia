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
package info.magnolia.cms.exchange;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Sameer Charles
 * $Id$
 */
public class DefaultActivationManager implements ActivationManager {

    private Collection subscribers = new ArrayList();

    public Collection getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Collection subscribers) {
        this.subscribers = subscribers;
    }

    public void addSubscribers(Subscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    public String getConfigPath() {
        return "/server/activation/subscribers";
    }

    public boolean hasAnyActiveSubscriber() {
        Iterator subscribers = this.getSubscribers().iterator();
        while (subscribers.hasNext()) {
            Subscriber subscriber = (Subscriber) subscribers.next();
            if (subscriber.isActive()) {
                return true;
            }
        }
        return false;
    }

}
