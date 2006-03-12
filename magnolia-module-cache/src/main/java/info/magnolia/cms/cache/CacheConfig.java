package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
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

        String uri = request.getRequestURI();
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
     * @todo refactor
     */
    private Map cacheCacheableURIMappings(Content nodeList, Map mappings, boolean allow) {
        for (Iterator it = nodeList.getChildren().iterator(); it.hasNext();) {
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

    private void registerEventListener() throws ConfigurationException {
        try {
            Node node = this.content.getJCRNode();
            ObservationManager observationManager = node.getSession().getWorkspace().getObservationManager();
            EventListener listener = new CacheConfigListener(this.cacheManager, this.content);
            int events = Event.NODE_ADDED
                | Event.NODE_REMOVED
                | Event.PROPERTY_ADDED
                | Event.PROPERTY_CHANGED
                | Event.PROPERTY_REMOVED;
            observationManager.addEventListener(listener, events, node.getPath(), true, null, null, false);
        }
        catch (Exception e) {
            throw new ConfigurationException("Could not register JCR EventLister.");
        }
    }

    /**
     * @todo refactor
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
