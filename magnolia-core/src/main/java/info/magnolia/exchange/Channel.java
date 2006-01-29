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
package info.magnolia.exchange;

/**
 * @author Sameer Charles
 */
public interface Channel {

    /**
     * <p>
     * opens the cahnnel for communication NOTE : every channel has to be open for 2-way communication
     * </p>
     * @throws ChannelInitializationException
     */
    void open() throws ChannelInitializationException;

    /**
     * <p>
     * Push packet to the FIFO Channel. adds ID to check list if packet needs confirmation.
     * </p>
     * @param packet
     * @throws ChannelOverflowException
     * @throws ChannelException
     */
    void send(Packet packet) throws ChannelOverflowException, ChannelException;

    /**
     * <p>
     * pushes packet to the receiving FIFO channel pigns back the same packet without body for confirmation
     * </p>
     * @param packet
     * @throws ChannelOverflowException
     * @throws ChannelException
     */
    void receive(Packet packet) throws ChannelOverflowException, ChannelException;

    /**
     * removes any packet reference for the specified ID.This could be used once packet confirmation is received
     * @param id
     * @throws ChannelException
     */
    void removePacketReference(String id) throws ChannelException;

    /**
     * flush all resources referenced through this channel.
     * @throws ChannelException
     */
    void flush() throws ChannelException;

    /**
     * Closes the channel if nothing left in the queue.
     */
    void close();

    /**
     * @return channel ID as specified on challen creation
     */
    String getID();
}
