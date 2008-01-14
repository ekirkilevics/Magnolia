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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.link.UUIDLink;
import info.magnolia.cms.link.UUIDLinkException;
import info.magnolia.cms.util.FactoryUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Mapps uri prefixes to repositories
 * @author Philipp Bracher
 * @version $Id$
 */
public class URI2RepositoryManager {

    protected static final URI2RepositoryMapping DEFAULT_MAPPING = new URI2RepositoryMapping("", ContentRepository.WEBSITE,"");

    private static Logger log = LoggerFactory.getLogger(URI2RepositoryManager.class);

    /**
     * The mappings
     */
    private Collection mappings;

    public URI2RepositoryManager() {
        mappings = new TreeSet(new Comparator() {
            public int compare(Object arg0, Object arg1) {
                URI2RepositoryMapping m0 = (URI2RepositoryMapping) arg0;
                URI2RepositoryMapping m1 = (URI2RepositoryMapping) arg1;
                return m1.getURIPrefix().compareTo(m0.getURIPrefix());
            }
        });

    }

    /**
     * The mapping to use for this uri
     */
    public URI2RepositoryMapping getMapping(String uri) {
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            URI2RepositoryMapping mapping = (URI2RepositoryMapping) iter.next();
            if (mapping.matches(uri)) {
                return mapping;
            }
        }
        return this.getDefaultMapping();
    }

    public URI2RepositoryMapping getDefaultMapping() {
        return DEFAULT_MAPPING;
    }

    /**
     * Get the handle for this uri
     * @param uri
     * @return
     */
    public String getHandle(String uri) {
        return this.getMapping(uri).getHandle(uri);
    }

    /**
     * Get the repository to use for this uri
     * @param uri
     * @return
     */
    public String getRepository(String uri) {
        return this.getMapping(uri).getRepository();
    }

    /**
     * Get the uri to use for this handle
     */
    public String getURI(String repository, String handle) {
        try {
            return getURI(new UUIDLink().initWithHandle(repository, handle));
        }
        catch (UUIDLinkException e) {
            log.error("can't map [" + handle + "] to a uri", e);
        }
        return handle;
    }

    public String getURI(UUIDLink uuidLink) {
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            URI2RepositoryMapping mapping = (URI2RepositoryMapping) iter.next();
            if (StringUtils.equals(mapping.getRepository(), uuidLink.getRepository()) && uuidLink.getHandle().startsWith(mapping.getHandlePrefix())) {
                return mapping.getURI(uuidLink);
            }
        }
        return this.getDefaultMapping().getURI(uuidLink);
    }

    public void addMapping(URI2RepositoryMapping mapping) {
        mappings.add(mapping);
    }

    public static URI2RepositoryManager getInstance() {
        return (URI2RepositoryManager) FactoryUtil.getSingleton(URI2RepositoryManager.class);
    }

    /**
     * @return the mappings
     */
    public Collection getMappings() {
        return this.mappings;
    }

}
