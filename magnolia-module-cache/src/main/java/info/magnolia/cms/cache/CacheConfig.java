/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 * @version $Id:CacheConfig.java 6314 2006-09-11 08:24:51Z scharles $
 *
 * @deprecated since 3.6, cache config is using the standard module mechanism, see info.magnolia.module.cache.CacheModule
 */
public class CacheConfig {

    private static final String DEFAULT_CACHE_IMPLEMENTATION = "info.magnolia.cms.cache.simple.CacheImpl";

    private boolean active;

    private String cacheImplementation;

    private CacheManager cacheManager;

    /**
     * Compression wont work for these pre compressed formats.
     */
    private Map compressionList;

    private final Content content;

    private Map uriMapping;

    /**
     * The list of repositories we observe
     */
    private List repositories;


    public List getRepositories() {
        return this.repositories;
    }

    /**
     * Create a new CacheConfig and loads the config from the repository.
     * @throws ConfigurationException
     */
    protected CacheConfig(CacheManager cacheManager, Content content) throws ConfigurationException {
        this.cacheManager = cacheManager;
        this.content = content;

        loadConfig();
        registerEventListener();
    }

    public boolean canCompress(String key) {
        return this.compressionList.containsKey(key.trim().toLowerCase());
    }

    public String getCacheImplementation() {
        return this.cacheImplementation;
    }

    public Content getContent() {
        return this.content;
    }

    public Content getContent(String name) throws RepositoryException {
        return this.content.getContent(name);
    }

    public Node getNode() {
        return this.content.getJCRNode();
    }

    public boolean isActive() {
        return this.active;
    }

    /**
     * @return true if the uri is allowed to be cached, false otherwise
     */
    public boolean isUriCacheable(HttpServletRequest request) {
        final String uri = MgnlContext.getAggregationState().getCurrentURI();
        boolean isAllowed = false;
        int lastMatchedPatternlength = 0;

        for (Iterator listEnum = this.uriMapping.keySet().iterator(); listEnum.hasNext();) {
            UrlPattern p = (UrlPattern) listEnum.next();
            if (p.match(uri)) {
                int patternLength = p.getLength();
                if (lastMatchedPatternlength < patternLength) {
                    lastMatchedPatternlength = patternLength;
                    isAllowed = ((Boolean) this.uriMapping.get(p)).booleanValue();
                }
            }
        }

        return isAllowed;
    }

    /**
     * todo refactor
     */
    private Map cacheCacheableURIMappings(Content nodeList, Map mappings, boolean allow) {
        for (Iterator it = nodeList.getChildren(ItemType.CONTENTNODE).iterator(); it.hasNext();) {
            Content container = (Content) it.next();
            NodeData uri = container.getNodeData("URI");
            UrlPattern p = new SimpleUrlPattern(uri.getString());
            mappings.put(p, BooleanUtils.toBooleanObject(allow));
        }

        return mappings;
    }

    private void loadConfig() throws ConfigurationException {
        this.active = this.content.getNodeData("active").getBoolean();

        // load mandatory config
        try {
            Content contentNode = this.content.getContent("URI/allow");
            Map mappings = new HashMap();
            cacheCacheableURIMappings(contentNode, mappings, true);
            contentNode = this.content.getContent("URI/deny");
            cacheCacheableURIMappings(contentNode, mappings, false);
            this.uriMapping = Collections.unmodifiableMap(mappings);
            Content compressionListNode = this.content.getContent("compression");
            this.compressionList = Collections.unmodifiableMap(updateCompressionList(compressionListNode));

            repositories = new ArrayList();
            Content repositoriesNode = this.getContent("repositories");
            for (Iterator iter = repositoriesNode.getChildren().iterator(); iter.hasNext();) {
                Content repositoryNode = (Content) iter.next();
                repositories.add(NodeDataUtil.getString(repositoriesNode, "name", repositoryNode.getName()));
            }
        }
        catch (RepositoryException e) {
            throw new ConfigurationException("Could not load cache configuration: " + e.getMessage(), e);
        }

        String cacheImplementation = this.content.getNodeData("cacheImplementation").getString();
        if (StringUtils.isBlank(cacheImplementation)) {
            cacheImplementation = DEFAULT_CACHE_IMPLEMENTATION;
        }

        this.cacheImplementation = cacheImplementation;

    }

    protected void reload() throws ConfigurationException {
        this.loadConfig();
    }

    private void registerEventListener() throws ConfigurationException {
        try {
            Node node = this.content.getJCRNode();
            EventListener listener = new CacheConfigListener(this.cacheManager, this);
            ObservationUtil.registerDeferredChangeListener(ContentRepository.CONFIG, node.getPath(), listener, 5000, 30000);
        }
        catch (Exception e) {
            throw new ConfigurationException("Could not register JCR EventLister.");
        }
    }

    /**
     * todo refactor
     */
    private Map updateCompressionList(Content list) {
        Map compressionList = new HashMap();

        if (list == null) {
            return compressionList;
        }

        Iterator it = list.getChildren().iterator();
        while (it.hasNext()) {
            Content node = (Content) it.next();
            compressionList.put(node.getNodeData("extension").getString(), node.getNodeData("type").getString());
        }

        return compressionList;
    }
}
