/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.link.Link;

import info.magnolia.link.LinkFactory;
import info.magnolia.link.LinkException;
import info.magnolia.objectfactory.Components;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maps uri prefixes to repositories.
 *
 * @author Philipp Bracher
 * @version $Id$
 */
public class URI2RepositoryManager {

    protected static final URI2RepositoryMapping DEFAULT_MAPPING = new URI2RepositoryMapping("", ContentRepository.WEBSITE,"");

    private static Logger log = LoggerFactory.getLogger(URI2RepositoryManager.class);

    /**
     * The mappings.
     */
    private final Collection<URI2RepositoryMapping> mappings;

    public URI2RepositoryManager() {
        mappings = new TreeSet<URI2RepositoryMapping>(getMappingComparator());
    }

    protected Comparator<URI2RepositoryMapping> getMappingComparator() {
        return new Comparator<URI2RepositoryMapping>() {
            public int compare(URI2RepositoryMapping m0, URI2RepositoryMapping m1) {
                return m1.getURIPrefix().compareTo(m0.getURIPrefix());
            }
        };
    }

    /**
     * The mapping to use for this uri.
     */
    public URI2RepositoryMapping getMapping(String uri) {
        for (URI2RepositoryMapping mapping : mappings) {
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
     * Get the handle for this uri.
     */
    public String getHandle(String uri) {
        return this.getMapping(uri).getHandle(uri);
    }

    /**
     * Get the repository to use for this uri.
     */
    public String getRepository(String uri) {
        return this.getMapping(uri).getRepository();
    }

    /**
     * Get the uri to use for this handle.
     */
    public String getURI(String repository, String handle) {
        try {
            return getURI(LinkFactory.createLink(repository, handle, null, null, null));
        }
        catch (LinkException e) {
            log.error("can't map [" + handle + "] to a uri", e);
        }
        return handle;
    }

    public String getURI(Link uuidLink) {
        for (URI2RepositoryMapping mapping : mappings) {
            if (StringUtils.equals(mapping.getRepository(), uuidLink.getRepository()) && uuidLink.getHandle().startsWith(mapping.getHandlePrefix())) {
                return mapping.getURI(uuidLink);
            }
        }
        return this.getDefaultMapping().getURI(uuidLink);
    }

    public void addMapping(URI2RepositoryMapping mapping) {
        mappings.add(mapping);
    }

    /**
     * @deprecated since 5.0, use IoC
     */
    public static URI2RepositoryManager getInstance() {
        return Components.getSingleton(URI2RepositoryManager.class);
    }

    /**
     * @return the mappings
     */
    public Collection<URI2RepositoryMapping> getMappings() {
        return this.mappings;
    }
}
