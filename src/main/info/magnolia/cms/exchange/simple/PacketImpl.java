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
package info.magnolia.cms.exchange.simple;

import info.magnolia.cms.util.uuid.UUID;
import info.magnolia.exchange.Packet;
import info.magnolia.exchange.PacketBody;
import info.magnolia.exchange.PacketHeader;
import info.magnolia.exchange.PacketType;

import java.util.Iterator;

import org.apache.log4j.Logger;


/**
 * Date: May 4, 2004 Time: 5:09:41 PM
 * @author Sameer Charles
 */
public class PacketImpl implements Packet {

    private static Logger log = Logger.getLogger(PacketImpl.class);

    private String id;

    private PacketHeader header;

    private PacketBody body;

    public PacketImpl() {
        this.header = new PacketHeaderImpl();
        this.body = new PacketBodyImpl();
    }

    public String getID() {
        return this.id;
    }

    public void assignID() {
        /* UUID itself is syncronized */
        this.id = (new UUID()).toString();
        this.getHeaders().addHeader("id", id);
    }

    public void assignID(String id) {
        this.id = id;
        this.getHeaders().addHeader("ID", id);
    }

    public void setChannelID(String id) {
        log.debug("Method not implemented (setChannelID)");
    }

    public String getChannelID() {
        log.debug("Method not implemented (getChannelID)");
        return null;
    }

    public PacketHeader getHeaders() {
        return this.header;
    }

    public void setHeaders(PacketHeader header) {
        /**
         * Add key by key to make sure we dont loose any existing keys assigned to this header object.
         */
        Iterator keys = header.getKeys().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            this.header.addHeader(key, header.getValueByName(key));
        }
    }

    public void setBody(PacketBody body) {
        this.body = body;
        this.getHeaders().addHeader("Type", PacketType.getNameByType(this.body.getType()));
    }

    public PacketBody getBody() {
        return this.body;
    }
}
