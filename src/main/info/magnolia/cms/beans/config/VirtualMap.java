/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.StringComparator;
import info.magnolia.cms.util.regex.RegexWildcardPattern;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.jcr.RepositoryException;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0 Store for all virtual URI to template/page mapping.
 */
public class VirtualMap {

    private static Logger log = Logger.getLogger(VirtualMap.class);

    /**
     * all cached data
     */
    private static Hashtable cachedURImapping = new Hashtable();

    private static VirtualMap virtualMap = new VirtualMap();

    public static VirtualMap getInstance() {
        return virtualMap;
    }

    public void update(String configPath) {
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {
            log.info("Config : Loading VirtualMap - " + configPath);
            ContentNode mappingNode = configHierarchyManager.getContentNode(configPath);
            this.cacheURIMappings(mappingNode);
            log.info("Config : VirtualMap loaded - " + configPath);
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load VirtualMap - " + configPath);
            log.error(re.getMessage(), re);
        }
    }

    protected void init() {
        VirtualMap.cachedURImapping.clear();
    }

    protected void reload() {
        log.info("Config : re-loading VirtualMap");
        this.init();
    }

    /**
     * @param nodeList to be added in cache
     */
    private void cacheURIMappings(ContentNode nodeList) throws RepositoryException {
        Collection list = nodeList.getChildren();
        Collections.sort((List) list, new StringComparator("fromURI"));
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ContentNode container = (ContentNode) it.next();
            NodeData fromURI = container.getNodeData("fromURI");
            StringBuffer fromURIStringBuffer = new StringBuffer();
            char[] chars = fromURI.getString().toCharArray();
            int i = 0, last = 0;
            while (i < chars.length) {
                char c = chars[i];
                if (c == '*') {
                    fromURIStringBuffer.append(chars, last, i - last);
                    fromURIStringBuffer.append(RegexWildcardPattern.getMultipleCharPattern());
                    last = i + 1;
                }
                i++;
            }
            fromURIStringBuffer.append(chars, last, i - last);
            Pattern p = Pattern.compile(fromURIStringBuffer.toString());
            VirtualMap.cachedURImapping.put(p, container.getNodeData("toURI").getString());
        }
    }

    /**
     * <p>
     * checks for the requested URI mapping in Server config : Servlet Specification 2.3 Section 10 "Mapping Requests to
     * Servlets"
     * </p>
     * @return URI string mapping
     */
    public String getURIMapping(String uri) {
        Enumeration e = VirtualMap.cachedURImapping.keys();
        while (e.hasMoreElements()) {
            Pattern p = (Pattern) e.nextElement();
            if (p.matcher(uri).matches())
                return (String) VirtualMap.cachedURImapping.get(p);
        }
        return "";
    }
}
