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
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import EDU.oswego.cs.dl.util.concurrent.Sync;

/**
 * Implementation of syndicator that simply sends all activated content over http connection specified in the subscriber.
 * 
 * @author Sameer Charles $Id$
 */
public class SimpleSyndicator extends BaseSyndicatorImpl {
    private static final Logger log = LoggerFactory.getLogger(SimpleSyndicator.class);

    @Override
    public void activate(final ActivationContent activationContent, String nodePath) throws ExchangeException {
        String nodeUUID = activationContent.getproperty(NODE_UUID);
        final ExchangeTask task;
        // Create runnable task for subscriber execute
        if (Boolean.parseBoolean(activationContent.getproperty(ItemType.DELETED_NODE_MIXIN))) {
            task = getDeactivateTask(nodeUUID, nodePath);
        } else {
            task = getActivateTask(activationContent, nodePath);
        }
        List<Exception> errors = executeExchangeTask(task);

        // clean storage BEFORE re-throwing exception
        executeInPool(new Runnable() {
            @Override
            public void run() {
                cleanTemporaryStore(activationContent);
            }
        });

        handleErrors(errors);

    }

    protected void handleErrors(List<Exception> errors) throws ExchangeException {
        // collect all the errors and send them back.
        if (!errors.isEmpty()) {
            Exception e = errors.get(0);
            log.error(e.getMessage(), e);
            throw new ExchangeException("1 error detected: \n" + e.getMessage(), e);
        }
    }

    private ExchangeTask getActivateTask(final ActivationContent activationContent, final String nodePath) {
        ExchangeTask r = new ExchangeTask() {
            @Override
            public void runTask(Subscriber subscriber) throws ExchangeException {
                activate(subscriber, activationContent, nodePath);
            }
        };
        return r;
    }

    @Override
    public void doDeactivate(String nodeUUID, String nodePath) throws ExchangeException {
        List<Exception> errors = executeExchangeTask(getDeactivateTask(nodeUUID, nodePath));
        handleErrors(errors);
    }

    private List<Exception> executeExchangeTask(ExchangeTask runnable) throws ExchangeException {
        Collection<Subscriber> allSubscribers = ActivationManagerFactory.getActivationManager().getSubscribers();
        Iterator<Subscriber> subscriberIterator = allSubscribers.iterator();
        final Sync done = new CountDown(allSubscribers.size());
        final List<Exception> errors = new ArrayList<Exception>();
        int count = 0;
        while (subscriberIterator.hasNext()) {
            count++;
            final Subscriber subscriber = subscriberIterator.next();
            if (subscriber.isActive()) {
                // TODO: Inject?
                runnable.setErrors(errors);
                runnable.setSubscriber(subscriber);
                runnable.setSync(done);
                // Create runnable task for each subscriber.
                executeInPool(runnable);
                break;
            } else {
                done.release();
            }
        } // end of subscriber loop

        // release unused barriers
        for (; count < allSubscribers.size(); count++) {
            done.release();
        }

        // wait until all tasks are executed before returning back to user to make sure errors can be propagated back to the user.
        acquireIgnoringInterruption(done);

        return errors;
    }

    private ExchangeTask getDeactivateTask(final String nodeUUID, final String nodePath) {
        ExchangeTask r = new ExchangeTask() {
            @Override
            public void runTask(Subscriber subscriber) throws ExchangeException {
                doDeactivate(subscriber, nodeUUID, nodePath);
            }
        };
        return r;
    }

    /**
     * Deactivate from a specified subscriber.
     * 
     * @param subscriber
     * @throws ExchangeException
     */
    @Override
    public String doDeactivate(Subscriber subscriber, String nodeUUID, String path) throws ExchangeException {
        Subscription subscription = subscriber.getMatchedSubscription(path, this.repositoryName);
        if (null != subscription) {
            String urlString = getDeactivationURL(subscriber);
            try {
                URLConnection urlConnection = prepareConnection(subscriber, urlString);

                this.addDeactivationHeaders(urlConnection, nodeUUID, null);
                String status = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_STATUS);

                if (StringUtils.equals(status, ACTIVATION_HANDSHAKE)) {
                    String handshakeKey = urlConnection.getHeaderField(ACTIVATION_AUTH);
                    // receive all pending data
                    urlConnection.getContent();

                    // transport the data again
                    urlConnection = prepareConnection(subscriber, getActivationURL(subscriber));
                    // and get the version & status again
                    this.addDeactivationHeaders(urlConnection, nodeUUID, handshakeKey);
                    status = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_STATUS);
                }

                // check if the activation failed
                if (StringUtils.equals(status, ACTIVATION_FAILED)) {
                    String message = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_MESSAGE);
                    throw new ExchangeException("Message received from subscriber: " + message);
                }

                urlConnection.getContent();

            } catch (MalformedURLException e) {
                throw new ExchangeException("Incorrect URL for subscriber " + subscriber + "[" + stripPasswordFromUrl(urlString) + "]");
            } catch (IOException e) {
                throw new ExchangeException("Not able to send the deactivation request [" + stripPasswordFromUrl(urlString) + "]: " + e.getMessage());
            } catch (Exception e) {
                throw new ExchangeException(e);
            }
        }
        return null;
    }

}
