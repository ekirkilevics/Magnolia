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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Sameer Charles
 * @version 1.1
 */
public class MIMEMapping {
    private static final Logger log = LoggerFactory.getLogger(MIMEMapping.class);

    public static final String ICONS_PATH = "/.resources/file-icons/"; //$NON-NLS-1$
    private static final String DEFAULT_ICON = ICONS_PATH + "general.png";
    private static final String NODEPATH = "/server/MIMEMapping"; //$NON-NLS-1$

    private static Map cachedContent = new Hashtable();
    private static final String DEFAULT_CHAR_ENCODING = "UTF-8";

    /**
     * Used to keep the configuration in memory
     */
    protected static class MIMEMappingItem {

        protected String ext;

        protected String mime;

        protected String icon;
    }


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
            final HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);

            Collection mimeList = hm.getContent(NODEPATH).getChildren(ItemType.CONTENTNODE); //$NON-NLS-1$
            MIMEMapping.cacheContent(mimeList);
            log.info("Config : MIMEMapping loaded"); //$NON-NLS-1$
        } catch (PathNotFoundException e) {
            log.warn("Config : no MIMEMapping info configured at " + NODEPATH); //$NON-NLS-1$
        }catch (RepositoryException re) {
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

        ObservationUtil.registerChangeListener(ContentRepository.CONFIG, NODEPATH, new EventListener() {
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
                item.icon = NodeDataUtil.getString(c, "icon");

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

        // this is expected by the caller
        return null;

    }

    /**
     * Returns the mime-type associated with this extension, or the server's default.
     */
    public static String getMIMETypeOrDefault(String extension) {
        String mimeType = getMIMEType(extension);

        if (StringUtils.isNotEmpty(mimeType)) {
            return mimeType;
        }

        if (StringUtils.isNotEmpty(extension)) {
            log.info("Cannot find MIME type for extension \"{}\"", extension);
        }

        final String defaultExtension = ServerConfiguration.getInstance().getDefaultExtension();
        return getMIMEType(defaultExtension);
    }

    public static String getContentEncoding(String contentType) {
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

    public static String getContentEncodingOrDefault(String contentType) {
        final String characterEncoding = getContentEncoding(contentType);

        if (StringUtils.isEmpty(characterEncoding)) {
            return DEFAULT_CHAR_ENCODING;
        } else {
            return characterEncoding;
        }
    }

    /**
     * Returns the icon used for rendering this type
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
