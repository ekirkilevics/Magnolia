/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.cache;

import java.io.Serializable;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 *
 * @deprecated since 3.6, use info.magnolia.module.cache.filter.CachedPage
 */
public class CacheableEntry implements Serializable {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    byte[] out;

    private String contentType;

    private String characterEncoding;

    public CacheableEntry(byte[] out) {
        this.out = out;
    }

    /**
     * Getter for <code>out</code>.
     * @return Returns the out.
     */
    public byte[] getOut() {
        return this.out;
    }

    public int getSize() {
        return this.out.length;
    }

    /**
     * Getter for <code>contentType</code>.
     * @return Returns the contentType.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Setter for <code>contentType</code>.
     * @param contentType The contentType to set.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Getter for <code>characterEncoding</code>.
     * @return Returns the characterEncoding.
     */
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    /**
     * Setter for <code>characterEncoding</code>.
     * @param characterEncoding The characterEncoding to set.
     */
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

}
