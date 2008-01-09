/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

/**
 *
 * @author Sameer Charles $Id$
 */
public class SimpleSyndicator extends BaseSyndicatorImpl {
    private static final Logger log = LoggerFactory.getLogger(SimpleSyndicator.class);

    public SimpleSyndicator() {
    }

    public synchronized void activate(ActivationContent activationContent) throws ExchangeException {
        Iterator subscribers = ActivationManagerFactory.getActivationManager().getSubscribers().iterator();
        while (subscribers.hasNext()) {
            Subscriber subscriber = (Subscriber) subscribers.next();
            if (subscriber.isActive()) {
                activate(subscriber, activationContent);
            }
        }
    }

    /**
     * Send activation request if subscribed to the activated URI
     * @param subscriber
     * @param activationContent
     * @throws ExchangeException
     */
    public synchronized void activate(Subscriber subscriber, ActivationContent activationContent)
            throws ExchangeException {
        if (null == subscriber) {
            throw new ExchangeException("Null Subscriber");
        }
        
        Subscription subscription = subscriber.getMatchedSubscription(this.path, this.repositoryName);
        if (null != subscription) {
            // its subscribed since we found the matching subscription
            String mappedPath = this.getMappedPath(this.parent, subscription);
            activationContent.setProperty(PARENT_PATH, mappedPath);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Exchange : subscriber [{}] is not subscribed to {}", subscriber.getName(), this.path);
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Exchange : sending activation request to {}", subscriber.getName()); //$NON-NLS-1$
            log.debug("Exchange : user [{}]", this.user.getName()); //$NON-NLS-1$
        }
        
        String handle = getActivationURL(subscriber);

        if (subscriber.getAuthenticationMethod() == null || "basic".equalsIgnoreCase(subscriber.getAuthenticationMethod())) {
            activationContent.addProperty(AUTHORIZATION, this.basicCredentials);
        } else if ("form".equalsIgnoreCase(subscriber.getAuthenticationMethod())) {
            handle += (handle.indexOf('?') > 0 ? "&" : "?") + AUTH_USER + "=" + this.user.getName();
            handle += "&" + AUTH_CREDENTIALS + "=" + this.user.getPassword();
        } else {
            log.info("Unknown authentication method for activation: " + subscriber.getAuthenticationMethod());
        }

        try {
            URL url = new URL(handle);
            URLConnection urlConnection = url.openConnection();
            this.addActivationHeaders(urlConnection, activationContent);

            Transporter.transport(urlConnection, activationContent);

            String status = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_STATUS);

            // check if the activation failed
            if (StringUtils.equals(status, ACTIVATION_FAILED)) {
                String message = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_MESSAGE);
                throw new ExchangeException("Message received from subscriber: " + message);
            }
            urlConnection.getContent();
            log.debug("Exchange : activation request sent to {}", subscriber.getName()); //$NON-NLS-1$
        }
        catch (ExchangeException e) {
            throw e;
        }
        catch (MalformedURLException e) {
            throw new ExchangeException("Incorrect URL for subscriber " + subscriber + "[" + handle + "]");
        }
        catch (IOException e) {
            throw new ExchangeException("Not able to send the activation request [" + handle + "]: " + e.getMessage());
        }
        catch (Exception e) {
            throw new ExchangeException(e);
        }
    }

    public synchronized void doDeactivate() throws ExchangeException {
        Iterator subscribers = ActivationManagerFactory.getActivationManager().getSubscribers().iterator();
        while (subscribers.hasNext()) {
            Subscriber subscriber = (Subscriber) subscribers.next();
            if (subscriber.isActive()) {
                doDeactivate(subscriber);
            }
        }
    }

    /**
     * deactivate from a specified subscriber
     * @param subscriber
     * @throws ExchangeException
     */
    public synchronized void doDeactivate(Subscriber subscriber) throws ExchangeException {
        Subscription subscription = subscriber.getMatchedSubscription(this.path, this.repositoryName);
        if (null != subscription) {
            String handle = getDeactivationURL(subscriber);
            try {
                // authentication headers
                if (subscriber.getAuthenticationMethod() != null && "form".equalsIgnoreCase(subscriber.getAuthenticationMethod())) {
                    handle += (handle.indexOf('?') > 0 ? "&" : "?") + AUTH_USER + "=" + this.user.getName();
                    handle += "&" + AUTH_CREDENTIALS + "=" + this.user.getPassword();
                } 
                URL url = new URL(handle);
                URLConnection urlConnection = url.openConnection();
                // authentication headers
                if (subscriber.getAuthenticationMethod() == null || "basic".equalsIgnoreCase(subscriber.getAuthenticationMethod())) {
                    urlConnection.setRequestProperty(AUTHORIZATION, this.basicCredentials);
                } else if (!"form".equalsIgnoreCase(subscriber.getAuthenticationMethod())) {
                    log.info("Unknown Authentication method for deactivation: " + subscriber.getAuthenticationMethod());
                }

                this.addDeactivationHeaders(urlConnection);
                urlConnection.getContent();
            }
            catch (MalformedURLException e) {
                throw new ExchangeException("Incorrect URL for subscriber " + subscriber + "[" + handle + "]");
            }
            catch (IOException e) {
                throw new ExchangeException("Not able to send the deactivation request [" + handle + "]: " + e.getMessage());
            }
            catch (Exception e) {
                throw new ExchangeException(e);
            }
        }
    }

    /**
     * path should be without trailing slash
     */
    protected String getMappedPath(String path, Subscription subscription) {
        if (null != subscription.getToURI()) {
            String fromURI = subscription.getFromURI();
            String toURI = subscription.getToURI();
            if (!path.equals(fromURI)) {
                if (fromURI.endsWith("/")) {
                    fromURI = StringUtils.substringBeforeLast(fromURI, "/");
                }
                if (toURI.endsWith("/")) {
                    toURI = StringUtils.substringBeforeLast(toURI, "/");
                }
            }
            path = path.replaceFirst(fromURI, toURI);
            if (path.equals("")) {
                return "/";
            }
        }
        return path;
    }

}
