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

import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.ObservationUtil;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public final class MIMEMapping {

    /**
     *
     */
    private static final String DEFAULT_ICON = "/.resources/file-icons/general.png";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MIMEMapping.class);

    private static final String START_PAGE = "server"; //$NON-NLS-1$

    private static Map cachedContent = new Hashtable();

    /**
     * Used to keep the configuration in memory
     */
    protected static class MIMEMappingItem {

        protected String ext;

        protected String mime;

        protected String icon;
    }

    public static final String ICONS_PATH = "/.resources/file-icons/"; //$NON-NLS-1$

    /**
     * Utility class, don't instantiate.
     */
    private MIMEMapping() {
        // unused
    }

    /**
     * Reads all configured mime mapping (config/server/MIMEMapping).
     */
    public static void init() {
        load();
        registerEventListener();
    }

    /**
     * Reads all configured mime mapping (config/server/MIMEMapping).
     */
    public static void load() {
        MIMEMapping.cachedContent.clear();
        try {
            log.info("Config : loading MIMEMapping"); //$NON-NLS-1$
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(START_PAGE);
            Collection mimeList = startPage.getContent("MIMEMapping").getChildren(ItemType.CONTENTNODE); //$NON-NLS-1$
            MIMEMapping.cacheContent(mimeList);
            log.info("Config : MIMEMapping loaded"); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load MIMEMapping"); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() {
        log.info("Config : re-loading MIMEMapping"); //$NON-NLS-1$
        MIMEMapping.load();
    }

    /**
     * Register an event listener: reload cache configuration when something changes.
     */
    private static void registerEventListener() {

        log.info("Registering event listener for MIMEMapping"); //$NON-NLS-1$

        ObservationUtil.registerChangeListener(
            ContentRepository.CONFIG,
            "/" + START_PAGE + "/" + "MIMEMapping",
            new EventListener() {

                public void onEvent(EventIterator iterator) {
                    // reload everything
                    reload();
                }
            });
    }

    /**
     * Cache all MIME types configured.
     */
    private static void cacheContent(Collection mimeList) {
        Iterator iterator = mimeList.iterator();
        while (iterator.hasNext()) {
            Content c = (Content) iterator.next();
            try {
                MIMEMappingItem item = new MIMEMappingItem();
                item.ext = NodeDataUtil.getString(c, "extension", c.getName());//$NON-NLS-1$
                item.mime = c.getNodeData("mime-type").getString();//$NON-NLS-1$
                item.icon = NodeDataUtil.getString(c, "icon", "general.png");

                MIMEMapping.cachedContent.put(item.ext, item);
            }
            catch (Exception e) {
                log.error("Failed to cache MIMEMapping"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Get MIME type String.
     * @param key extension for which MIME type is requested
     * @return MIME type
     */
    public static String getMIMEType(String key) {
        if (StringUtils.isEmpty(key)) {
            return StringUtils.EMPTY;
        }
        // check that the cached content contains the key first to avoid NPE when accessing 'mime'
        String loweredKey = key.toLowerCase();
        if (MIMEMapping.cachedContent.containsKey(loweredKey)) {
            return ((MIMEMappingItem) MIMEMapping.cachedContent.get(loweredKey)).mime;
        }

        // this is expected by the caller getMIMEType(HttpServletRequest)
        return null;

    }

    /**
     * @deprecated do not pass the request
     */
    public static String getMIMEType(HttpServletRequest request) {
        return getMIMEType();
    }

    /**
     * Get MIME type String.
     * @return MIME type of the current context
     */
    public static String getMIMEType() {
        String extension = Aggregator.getExtension();
        String mimeType = getMIMEType(extension);

        if (mimeType == null && StringUtils.isNotEmpty(extension)) {
            log.info("Cannot find MIME type for extension \"{}\"", extension); //$NON-NLS-1$
            mimeType = ((MIMEMappingItem) MIMEMapping.cachedContent.get(Server.getDefaultExtension())).mime;
        }
        return mimeType;
    }

    /**
     * @param request
     */
    public static String getContentEncoding(HttpServletRequest request) {
        String contentType = MIMEMapping.getMIMEType(request);
        if (contentType != null) {
            int index = contentType.lastIndexOf(";"); //$NON-NLS-1$
            if (index > -1) {
                String encoding = contentType.substring(index + 1).toLowerCase().trim();
                encoding = encoding.replaceAll("charset=", StringUtils.EMPTY); //$NON-NLS-1$
                return encoding;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns the icon used for rendering this type
     * @param extension
     * @return the icon name
     */
    public static String getMIMETypeIcon(String extension) {
        MIMEMappingItem item = (MIMEMappingItem) MIMEMapping.cachedContent.get(extension.toLowerCase());
        if (item != null) {
            return StringUtils.defaultIfEmpty(item.icon, DEFAULT_ICON);
        }

        return DEFAULT_ICON;

    }
}
