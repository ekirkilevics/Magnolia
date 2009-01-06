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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.link.UUIDLink;
import info.magnolia.cms.link.UUIDLinkException;

import org.apache.commons.lang.StringUtils;


/**
 * Describes a uri to repository mapping
 * @author Philipp Bracher
 * @version $Id$
 */
public class URI2RepositoryMapping {

    /**
     * The prefix which triggers this mapping
     */
    private String URIPrefix;

    /**
     * The repository used for this mapping
     */
    private String repository;

    /**
     * The prefix added to the uri to create a full handle
     */
    private String handlePrefix;

    /**
     * @param uriPrefix
     * @param repository
     * @param handlePrefix
     */
    public URI2RepositoryMapping(String uriPrefix, String repository, String handlePrefix) {
        this.URIPrefix = uriPrefix;
        this.repository = repository;
        this.handlePrefix = handlePrefix;
    }

    public URI2RepositoryMapping() {
    }

    /**
     * True if this mapping can get applied to the specified uri
     * @param uri
     * @return
     */
    public boolean matches(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.startsWith(URIPrefix);
    }

    /**
     * Create a node handle based on an uri
     * @param uri
     * @return
     */
    public String getHandle(String uri) {
        String handle;
        handle = StringUtils.removeStart(uri, this.URIPrefix);
        if (StringUtils.isNotEmpty(this.handlePrefix)) {
            StringUtils.removeStart(handle, "/");
            handle = this.handlePrefix + "/" + handle;
        }
        //remove extension (ignore . anywere else in the uri)
        String fileName = StringUtils.substringAfterLast(handle, "/");
        String extension = StringUtils.substringAfterLast(fileName, ".");
        handle = StringUtils.removeEnd(handle, extension);
        return cleanHandle(handle);
    }

    /**
     * Clean a handle. Remove double / and add allways a leading /
     * @param handle
     * @return
     */
    private String cleanHandle(String handle) {
        if (!handle.startsWith("/")) {
            handle = "/" + handle;
        }
        handle = StringUtils.replace(handle, "//", "/");
        return handle;
    }

    /**
     * Create a uri based on a handle
     * @param handle
     * @return
     */
    public String getURI(String handle) {
        try {
            return getURI(new UUIDLink().initWithHandle(this.getRepository(), handle));
        }
        catch (UUIDLinkException e) {
            return handle;
        }
    }

    public String getURI(UUIDLink uuidLink){
        String uri = uuidLink.getHandle();
        if (StringUtils.isNotEmpty(this.handlePrefix)) {
            uri = StringUtils.removeStart(uri, this.handlePrefix);
        }
        if (StringUtils.isNotEmpty(this.URIPrefix)) {
            uri = this.URIPrefix + "/" + uri;
        }

        String nodeDataName = uuidLink.getNodeDataName();
        String fileName = uuidLink.getFileName();
        String extension = uuidLink.getExtension();

        uri += (StringUtils.isNotEmpty(nodeDataName)? "/" + nodeDataName : "") +
                (StringUtils.isNotEmpty(fileName)? "/" + fileName : "") +
                (StringUtils.isNotEmpty(extension)? "." + extension : "");

        return cleanHandle(uri);
    }

    public String getHandlePrefix() {
        return handlePrefix;
    }

    public void setHandlePrefix(String handlePrefix) {
        this.handlePrefix = handlePrefix;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getURIPrefix() {
        return URIPrefix;
    }

    public void setURIPrefix(String uriPrefix) {
        this.URIPrefix = uriPrefix;
    }

    public String toString() {
        return this.URIPrefix + " --> " + repository + ":" + this.handlePrefix;
    }
}
