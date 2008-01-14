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
