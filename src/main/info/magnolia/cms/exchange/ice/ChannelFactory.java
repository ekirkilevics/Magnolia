/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.exchange.ice;

import info.magnolia.exchange.Channel;
import info.magnolia.exchange.ChannelInitializationException;
import info.magnolia.exchange.ChannelException;

import java.util.Hashtable;
import java.net.URL;


/**
 * Date: May 4, 2004
 * Time: 5:05:31 PM
 *
 * @author Sameer Charles
 */


public class ChannelFactory {


    private static Hashtable channels = new Hashtable();



    /**
     * <p>
     * Returns an already open channel associated with this ID.<br>
     * Initailizes and opens a new channel if none exist in the factory
     * with the specified ID.
     * </p>
     *
     * @param id
     * @param destination , destination URL to with this chgannel is connected
     * @param authString , authorization header for the destination URL
     *
     * */
    public static Channel getChannel(String id, URL destination, String authString)
            throws ChannelInitializationException {
        Channel channel = (Channel) channels.get(id);
        if (channel == null) {
            openChannel(id, destination, authString);
            channel = (Channel) channels.get(id);
        }

        return channel;
    }



    /**
     * <p>
     * Initializes new channel
     * </p>
     *
     * @param id
     * @param destination , destination URL to with this chgannel is connected
     * @param authString , authorization header for the destination URL
     *
     * */
    private static void openChannel(String id, URL destination, String authString)
            throws ChannelInitializationException {
        Channel channel = new ChannelImpl(id, destination, authString);
        channel.open();
        channels.put(id,channel);
    }



    /**
     * <p>
     * Close channel if exist
     * </p>
     * @param id
     * */
    public boolean closeChannel(String id) {
        Channel channel = (Channel) channels.get(id);
        if (channel == null)
            return true;
        try {
            channel.flush();
            channel.close();
        } catch (ChannelException ce) {
            return false;
        }
        return true;
    }





}
