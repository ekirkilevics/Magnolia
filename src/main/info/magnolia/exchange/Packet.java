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
package info.magnolia.exchange;

/**
 * @author Sameer Charles
 */
public interface Packet {

    /**
     * Implementor must make sure this ID is unique over the process.
     */
    String getID();

    /**
     * Implementing class have to make sure this ID has not been assigned to any parallel packet in the same process
     */
    void assignID();

    /**
     * Sets current packet ID as specified
     */
    void assignID(String id);

    /**
     * Channel through which this packet is being initiated.
     */
    void setChannelID(String id);

    /**
     * Channel through which this packet is being initiated.
     */
    String getChannelID();

    /**
     * hash (key - Header String , value - Value)
     * @return empty initialized header if none exist.
     */
    PacketHeader getHeaders();

    /**
     * initialize empty PacketHeader if none specified
     * @param header
     */
    void setHeaders(PacketHeader header);

    /**
     * @param body , packet data
     */
    void setBody(PacketBody body);

    /**
     * @return packet body if any
     */
    PacketBody getBody();
}
