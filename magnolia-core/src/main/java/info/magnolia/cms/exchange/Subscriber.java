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

/**
 * @author Sameer Charles
 * $Id$
 */
public interface Subscriber {

    public String getName();

    public void setName(String name);

    public String getURL();

    public void setURL(String url);

    public boolean isActive();

    public void setActive(boolean active);

    public void setSubscriptions(Collection subscriptions);

    public Collection getSubscriptions();

    public void addSubscriptions(Subscription subscription);

    public Subscription getMatchedSubscription(String path, String repositoryId);

    public boolean isSubscribed(String path, String repositoryId);

}
