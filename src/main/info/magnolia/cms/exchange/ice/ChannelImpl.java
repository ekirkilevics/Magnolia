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
package info.magnolia.cms.exchange.ice;

import info.magnolia.exchange.Channel;
import info.magnolia.exchange.ChannelException;
import info.magnolia.exchange.ChannelInitializationException;
import info.magnolia.exchange.ChannelOverflowException;
import info.magnolia.exchange.Packet;
import info.magnolia.exchange.PacketHeader;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Sameer Charles
 */
public class ChannelImpl implements Channel {

    private String id;

    private URL destination;

    private String authString;

    private Map queue;

    public ChannelImpl(String id, URL destination, String authString) {
        this.id = id;
        this.destination = destination;
        this.authString = authString;
        queue = new Hashtable();
    }

    public String getID() {
        return this.id;
    }

    /**
     * here we only check if the connection can be open
     */
    public void open() throws ChannelInitializationException {
        try {
            URLConnection urlConnection = destination.openConnection();
            urlConnection.setRequestProperty("Authorization", this.authString);
            urlConnection.connect();
        }
        catch (Exception e) {
            throw new ChannelInitializationException(e.getMessage());
        }
    }

    /**
     * @param packet
     */
    public void send(Packet packet) throws ChannelOverflowException, ChannelException {
        if (packet.getID() == null) {
            packet.assignID();
        }
        packet.setChannelID(this.id);
        this.remoteSend(packet);
        this.queue.put(packet.getID(), ""); /* just a referece for confirmation */
    }

    /**
     * @param packet
     */
    public void receive(Packet packet) throws ChannelOverflowException, ChannelException {
        if (this.queue.get(packet.getID()) != null) { /* its a confirmation packet */
            this.removePacketReference(packet.getID());
            return;
        }
        /* deserialize packet data */
    }

    public void removePacketReference(String id) {
        this.queue.remove(id);
    }

    public void flush() {
        this.queue.clear(); // fake flush
    }

    public void close() {
    }

    private void remoteSend(Packet packet) throws ChannelException {
        try {
            URLConnection urlConnection = destination.openConnection();
            urlConnection.setRequestProperty("Authorization", this.authString);
            this.setHeader(packet.getHeaders(), urlConnection);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            PrintWriter writer = new PrintWriter(urlConnection.getOutputStream());
            writer.print(packet.getBody().toString());
            writer.flush();
            writer.close();
            urlConnection.getContent();
        }
        catch (Exception e) {
            throw (new ChannelException(e.getMessage()));
        }
    }

    private void setHeader(PacketHeader header, URLConnection urlConnection) {
        Iterator keys = header.getKeys().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            urlConnection.setRequestProperty(key, header.getValueByName(key));
        }
    }
}
