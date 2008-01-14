/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
