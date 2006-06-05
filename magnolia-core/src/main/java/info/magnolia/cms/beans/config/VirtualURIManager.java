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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Store for all virtual URI to template/page mapping.
 * @author Sameer Charles
 * @version 2.0
 */
public final class VirtualURIManager extends ObservedManager {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(VirtualURIManager.class);

    /**
     * The current implementation of the ParagraphManager. Defeined in magnolia.properties.
     */
    private static VirtualURIManager instance = (VirtualURIManager) FactoryUtil.getSingleton(VirtualURIManager.class);

    /**
     * Instantiated by the system.
     */
    public VirtualURIManager() {
    }

    /**
     * all cached data.
     */
    private Map cachedURImapping = new Hashtable();

    /**
     * checks for the requested URI mapping in Server config : Servlet Specification 2.3 Section 10 "Mapping Requests to
     * Servlets".
     * @return URI string mapping
     */
    public String getURIMapping(String uri) {
        Iterator e = cachedURImapping.keySet().iterator();
        String mappedURI = StringUtils.EMPTY;
        int lastMatchedPatternlength = 0;
        while (e.hasNext()) {
            UrlPattern p = (UrlPattern) e.next();
            if (p.match(uri)) {
                int patternLength = p.getLength();
                if (lastMatchedPatternlength < patternLength) {
                    lastMatchedPatternlength = patternLength;
                    mappedURI = ((String[]) cachedURImapping.get(p))[0];
                }
            }
        }
        return mappedURI;
    }

    protected void onRegister(Content node) {
        try {
            log.info("Config : Loading VirtualMap - " + node.getHandle()); //$NON-NLS-1$
            Collection list = node.getChildren(ItemType.CONTENTNODE);
            Collections.sort((List) list, new StringComparator("fromURI")); //$NON-NLS-1$
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Content container = (Content) it.next();
                NodeData fromURI = NodeDataUtil.getOrCreate(container,"fromURI"); //$NON-NLS-1$
                UrlPattern p = new SimpleUrlPattern(fromURI.getString());
                cachedURImapping.put(p, new String[]{NodeDataUtil.getString(container,"toURI"),fromURI.getString()}); //$NON-NLS-1$
            }
            log.info("Config : VirtualMap loaded - " + node.getHandle()); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("Config : Failed to load VirtualMap - " + node.getHandle() + " - " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    protected void onClear() {
        this.cachedURImapping.clear();
    }

    public Map getURIMappings() {
        return cachedURImapping;
    }

    /**
     * @return Returns the instance.
     */
    public static VirtualURIManager getInstance() {
        return instance;
    }
}
