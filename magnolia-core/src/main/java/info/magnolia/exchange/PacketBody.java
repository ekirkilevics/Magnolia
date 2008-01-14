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

import java.io.InputStream;
import java.util.Calendar;


/**
 * @author Sameer Charles
 */
public interface PacketBody {

    /**
     * <p>
     * set packet type, supported types
     * <ul>
     * <li><code>PacketType.STRING</code>
     * <li><code>PacketType.BINARY</code>
     * <li><code>PacketType.LONG</code>
     * <li><code>PacketType.DOUBLE</code>
     * <li><code>PacketType.DATE</code>
     * <li><code>PacketType.BOOLEAN</code>
     * </ul>
     * </p>
     * @param type
     */
    void setType(int type);

    /**
     * <p>
     * one of these packet types
     * <ul>
     * <li><code>PacketType.STRING</code>
     * <li><code>PacketType.BINARY</code>
     * <li><code>PacketType.LONG</code>
     * <li><code>PacketType.DOUBLE</code>
     * <li><code>PacketType.DATE</code>
     * <li><code>PacketType.BOOLEAN</code>
     * </ul>
     * </p>
     */
    int getType();

    /**
     * @param size
     */
    void setLength(long size);

    /**
     * @return lenth of the packet body
     */
    long getLength();

    /**
     * <p>
     * set packet body, Packet type also must be set here
     * </p>
     * @param data
     */
    void setBody(String data) throws PacketIOException;

    /**
     * <p>
     * set packet body, Packet type must be set here
     * </p>
     * @param data
     */
    void setBody(InputStream data) throws PacketIOException;

    /**
     * <p>
     * set packet body, Packet type must be set here
     * </p>
     * @param data
     */
    void setBody(Long data) throws PacketIOException;

    /**
     * <p>
     * set packet body, Packet type must be set here
     * </p>
     * @param data
     */
    void setBody(Double data) throws PacketIOException;

    /**
     * <p>
     * set packet body, Packet type must be set here
     * </p>
     * @param data
     */
    void setBody(Calendar data) throws PacketIOException;

    /**
     * <p>
     * set packet body, Packet type must be set here
     * </p>
     * @param data
     */
    void setBody(Boolean data) throws PacketIOException;

    /**
     * <p>
     * set packet body, Packet type must be set here
     * </p>
     * @param data
     */
    void setBody(Object data) throws PacketIOException;

    /**
     * <p>
     * returns string representation of the data set by any data type
     * </p>
     */
    String toString();

    /**
     * <p>
     * gets data as object
     * </p>
     */
    Object getObject();
}
