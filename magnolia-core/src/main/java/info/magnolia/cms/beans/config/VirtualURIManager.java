/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Store for all virtual URI to template/page mapping.
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
            VirtualURIMapping vm = (VirtualURIMapping) e.next();
            VirtualURIMapping.MappingResult result = vm.mapURI(uri);
            if (result != null && lastMatchedLevel < result.getLevel()) {
                lastMatchedLevel = result.getLevel();
                mappedURI = result.getToURI();
            }
        }
        return mappedURI;
    }

    protected void onRegister(Content node) {
        try {
            log.info("Config : Loading VirtualMap - " + node.getHandle()); //$NON-NLS-1$
            Content2BeanUtil.setProperties(this.cachedURImapping, node, true, new Content2BeanTransformerImpl(){
                protected TypeDescriptor onResolveClass(TransformationState state) {
                    if(state.getLevel()==2){
                        return this.getTypeMapping().getTypeDescriptor(DefaultVirtualURIMapping.class);
                    }
                    return null;
                }
            });
            log.info("Config : VirtualMap loaded - " + node.getHandle()); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("Config : Failed to load VirtualMap - " + node.getHandle() + " - " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
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
        return (VirtualURIManager) FactoryUtil.getSingleton(VirtualURIManager.class);
    }
}
