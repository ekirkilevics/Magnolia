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

import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store for all virtual URI to template/page mapping.
 *
 * @author Sameer Charles
 * @version 2.0
 */
public final class VirtualURIManager extends ObservedManager {

    private static final Logger log = LoggerFactory.getLogger(VirtualURIManager.class);

    public static final String FROM_URI_NODEDATANAME = "fromURI";

    public static final String TO_URI_NODEDATANAME = "toURI";

    /**
     * Instantiated by the system.
     */
    public VirtualURIManager() {
    }

    /**
     * all cached data. <UrlPattern, String[target, pattern]>
     */
    private final List cachedURImapping = new ArrayList();

    /**
     * checks for the requested URI mapping in Server config : Servlet Specification 2.3 Section 10 "Mapping Requests to
     * Servlets".
     * @return URI string mapping
     */
    public String getURIMapping(String uri) {
        Iterator e = cachedURImapping.iterator();
        String mappedURI = StringUtils.EMPTY;
        int lastMatchedLevel = 0;
        while (e.hasNext()) {
            try{
                VirtualURIMapping vm = (VirtualURIMapping) e.next();
                VirtualURIMapping.MappingResult result = vm.mapURI(uri);
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

    protected void onRegister(Content node) {
        try {
            log.info("Loading VirtualURIMapping from {}", node.getHandle()); //$NON-NLS-1$
            Content2BeanUtil.setProperties(this.cachedURImapping, node, true, new Content2BeanTransformerImpl(){
                protected TypeDescriptor onResolveType(TransformationState state, TypeDescriptor resolvedType) {
                    if(state.getLevel()==2 && resolvedType == null){
                        return this.getTypeMapping().getTypeDescriptor(DefaultVirtualURIMapping.class);
                    }
                    return resolvedType;
                }
            });
            log.debug("VirtualURIMapping loaded from {}", node.getHandle()); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("Failed to load VirtualURIMapping from " + node.getHandle() + " - " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    protected void onClear() {
        this.cachedURImapping.clear();
    }

    // TODO : should this really be public ?
    public Collection getURIMappings() {
        return cachedURImapping;
    }

    /**
     * @return Returns the instance.
     */
    public static VirtualURIManager getInstance() {
        return Components.getSingleton(VirtualURIManager.class);
    }
}
