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

import info.magnolia.exchange.Packet;
import info.magnolia.exchange.PacketHeader;
import info.magnolia.exchange.PacketBody;
import info.magnolia.exchange.PacketType;
import org.w3c.util.UUID;

import java.util.Enumeration;



/**
 * Date: May 4, 2004
 * Time: 5:09:41 PM
 *
 * @author Sameer Charles
 */


public class PacketImpl implements Packet {



    private String id;
    private PacketHeader header;
    private String channelID;
    private PacketBody body;




    public PacketImpl() {
        this.header = new PacketHeaderImpl();
    }



    public String getID() {
        return this.id;
    }


    public void assignID() {
        /* UUID itself is syncronized */
        this.id = (new UUID()).toString();
        this.getHeaders().addHeader("id",id);
    }


    public void assignID(String id) {
        this.id = id;
        this.getHeaders().addHeader("ID",id);
    }


    public void setChannelID(String id) {
        this.channelID = id;
        this.getHeaders().addHeader("ChannelID",id);
    }

    public String getChannelID() {
        return this.channelID;
    }


    public PacketHeader getHeaders() {
        return this.header;
    }


    public void setHeaders(PacketHeader header) {
        /**
         * Add key by key to make sure we dont loose any
         * existing keys assigned to this header object.
         * */
        Enumeration keys = header.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            this.header.addHeader(key, header.getValueByName(key));
        }
    }


    public void setBody(PacketBody body) {
        this.body = body;
        this.getHeaders().addHeader("Content-length",
                (new Long(this.body.getLength())).toString());
        this.getHeaders().addHeader("Type",
                PacketType.getNameByType(this.body.getType()));
    }


    public PacketBody getBody() {
        return this.body;
    }


}
