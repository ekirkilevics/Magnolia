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
package info.magnolia.cms.core.ie;

import info.magnolia.cms.core.Content;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;


/**
 * Date: May 24, 2005 Time: 4:48:26 PM
 * @author Sameer Charles $Id :$
 */
public interface ImportHandler {

    /**
     * If true, this importer assumes that binary data is not embedded in main data file
     * @param binaryAsLink
     */
    void setBinaryAsLink(boolean binaryAsLink);

    /**
     * returns true if the binary properties are included as link
     */
    boolean getBinaryAsLink();

    /**
     * import content from the given input stream
     * @param target where this content stream should be imported
     * @param inStream
     * @throws RepositoryException
     * @throws java.io.IOException
     */
    void importContent(Content target, InputStream inStream) throws RepositoryException, IOException;

    /**
     * set importer implementation dependent parameters
     * @param key
     * @param value
     */
    void setParameter(String key, Object value);

    /**
     * get parameter previously set on this importer
     * @return Object
     */
    Object getParameter(String key);
}
