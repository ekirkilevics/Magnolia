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
package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.exchange.ActivationContent;
import info.magnolia.cms.exchange.ExchangeException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles $Id$
 */
public class SimpleSyndicator extends BaseSyndicatorImpl {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SimpleSyndicator.class);

    /**
     *
     */
    public SimpleSyndicator() {

    }

    /**
     * @throws ExchangeException
     */
    public synchronized void activate(ActivationContent activationContent) throws ExchangeException {
        Subscriber si = Subscriber.getSubscriber();
        if (si.isActive()) {
            activate(si, activationContent);
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
        if (!isSubscribed(subscriber)) {
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
        try {
            URL url = new URL(handle);
            URLConnection urlConnection = url.openConnection();
            this.addActivationHeaders(urlConnection, activationContent);

            Transporter.transport(urlConnection, activationContent);

            String status = urlConnection.getHeaderField(SimpleSyndicator.ACTIVATION_ATTRIBUTE_STATUS);

            // check if the activation failed
            if (StringUtils.equals(status, SimpleSyndicator.ACTIVATION_FAILED)) {
                String message = urlConnection.getHeaderField(SimpleSyndicator.ACTIVATION_ATTRIBUTE_MESSAGE);
                throw new ExchangeException("Message received from subscriber: " + message);
            }
            urlConnection.getContent();
            log.info("Exchange : activation request received by {}", subscriber.getName()); //$NON-NLS-1$
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

    /**
     * @throws ExchangeException
     */
    public synchronized void doDeActivate() throws ExchangeException {
        Subscriber si = Subscriber.getSubscriber();
        if (si.isActive()) {
            if (log.isDebugEnabled()) {
                log.debug("Removing [{}] from [{}]", this.path, si.getURL()); //$NON-NLS-1$
            }
            doDeActivate(si);
        }
    }

    /**
     * deactivate from a specified subscriber
     * @param subscriber
     * @throws ExchangeException
     */
    public synchronized void doDeActivate(Subscriber subscriber) throws ExchangeException {
        if (!isSubscribed(subscriber)) {
            return;
        }
        String handle = getDeactivationURL(subscriber);
        try {
            URL url = new URL(handle);
            URLConnection urlConnection = url.openConnection();
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
