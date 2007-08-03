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

import info.magnolia.cms.exchange.Subscription;

/**
 * @author Sameer Charles
 * $Id$
 */
public class DefaultSubscription implements Subscription {
    private String name;

    private String fromURI;

    private String toURI;

    private String repository;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFromURI() {
        return fromURI;
    }

    public void setFromURI(String fromURI) {
        this.fromURI = fromURI;
    }

    public String getToURI() {
        return toURI;
    }

    public void setToURI(String toURI) {
        this.toURI = toURI;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Checks if we are subscribed to the given path
     * @param value path to check
     * @return the length of the fromURI
     * */
    public int vote(Object value) {
        String path = String.valueOf(value);
        String subscribedPath = getFromURI();
        if (path.equals(subscribedPath)) {
            return subscribedPath.length();
        }
        if (!subscribedPath.endsWith("/")) {
            subscribedPath += "/";
        }
        if (path.startsWith(subscribedPath)) {
            return subscribedPath.length();
        }

        return -1;
    }

    public boolean isEnabled() {
        return true;
    }

}
