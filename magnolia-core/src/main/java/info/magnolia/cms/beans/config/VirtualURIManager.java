/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.TransformationState;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store for all virtual URI to template/page mapping.
 *
 * @author Sameer Charles
 * @version 2.0
 */
@Singleton
public final class VirtualURIManager extends ObservedManager {

    private static final Logger log = LoggerFactory.getLogger(VirtualURIManager.class);

    public static final String FROM_URI_NODEDATANAME = "fromURI";

    public static final String TO_URI_NODEDATANAME = "toURI";

    private final Node2BeanProcessor nodeToBean;

    /**
     * Instantiated by the system.
     */
    @Inject
    public VirtualURIManager(Node2BeanProcessor nodeToBean) {
        this.nodeToBean = nodeToBean;
    }

    /**
     * all cached data. <UrlPattern, String[target, pattern]>
     */
    private final List<VirtualURIMapping> cachedURImapping = new ArrayList<VirtualURIMapping>();

    /**
     * checks for the requested URI mapping in Server config : Servlet Specification 2.3 Section 10 "Mapping Requests to
     * Servlets".
     *
     * @param uri the URI of the current request, decoded and without the context path
     * @return URI string mapping
     */
    public String getURIMapping(String uri) {
        return getURIMapping(uri, null);
    }

    /**
     * checks for the requested URI mapping in Server config : Servlet Specification 2.3 Section 10 "Mapping Requests to
     * Servlets".
     *
     * @param uri the URI of the current request, decoded and without the context path
     * @param queryString the Query String of the current request
     * @return URI string mapping
     */
    public String getURIMapping(String uri, String queryString) {
        Iterator<VirtualURIMapping> e = cachedURImapping.iterator();
        String mappedURI = StringUtils.EMPTY;
        int lastMatchedLevel = 0;
        while (e.hasNext()) {
            try{
                VirtualURIMapping vm = e.next();
                final VirtualURIMapping.MappingResult result;
                if (queryString != null && vm instanceof QueryAwareVirtualURIMapping){
                    result = ((QueryAwareVirtualURIMapping)vm).mapURI(uri, queryString);
                } else {
                    result = vm.mapURI(uri);
                }
                if (result != null && lastMatchedLevel < result.getLevel()) {
                    lastMatchedLevel = result.getLevel();
                    mappedURI = result.getToURI();
                }
            }catch(ClassCastException ex){
                log.error("Virtual URI configuration error, mapping rule is skipped: " + ex.getMessage(), ex);
            }
        }
        return mappedURI;
    }

    @Override
    protected void onRegister(Content node) {
        try {
            log.info("Loading VirtualURIMapping from {}", node.getHandle()); //$NON-NLS-1$
            nodeToBean.setProperties(this.cachedURImapping, node.getJCRNode(), true, new Node2BeanTransformerImpl() {
                @Override
                protected TypeDescriptor onResolveType(TypeMapping typeMapping, TransformationState state, TypeDescriptor resolvedType, ComponentProvider componentProvider) {
                    if (state.getLevel() == 2 && resolvedType == null) {
                        return typeMapping.getTypeDescriptor(DefaultVirtualURIMapping.class);
                    }
                    return resolvedType;
                }
            }, Components.getComponentProvider());
            log.debug("VirtualURIMapping loaded from {}", node.getHandle()); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("Failed to load VirtualURIMapping from " + node.getHandle() + " - " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    protected void onClear() {
        this.cachedURImapping.clear();
    }

    // TODO : should this really be public ?
    public Collection<VirtualURIMapping> getURIMappings() {
        return cachedURImapping;
    }

    /**
     * @return Returns the instance.
     * @deprecated since 4.5, use IoC !
     */
    @Deprecated
    public static VirtualURIManager getInstance() {
        return Components.getSingleton(VirtualURIManager.class);
    }
}
