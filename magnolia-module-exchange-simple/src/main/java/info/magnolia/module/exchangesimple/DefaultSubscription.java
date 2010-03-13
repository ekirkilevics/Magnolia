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
