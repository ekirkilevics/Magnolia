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

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.core.Content;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public final class MIMEMapping {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(MIMEMapping.class);

    private static final String START_PAGE = "server"; //$NON-NLS-1$

    private static Iterator mimeList;

    private static Map cachedContent = new Hashtable();

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
        MIMEMapping.cachedContent.clear();
        try {
            log.info("Config : loading MIMEMapping"); //$NON-NLS-1$
            Content startPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(START_PAGE);
            MIMEMapping.mimeList = startPage.getContent("MIMEMapping").getChildren().iterator(); //$NON-NLS-1$
            MIMEMapping.cacheContent();
            log.info("Config : MIMEMapping loaded"); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load MIMEMapping"); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() {
        log.info("Config : re-loading MIMEMapping"); //$NON-NLS-1$
        MIMEMapping.init();
    }

    /**
     * Cache all MIME types configured.
     */
    private static void cacheContent() {
        while (MIMEMapping.mimeList.hasNext()) {
            Content c = (Content) MIMEMapping.mimeList.next();
            try {
                MIMEMapping.cachedContent.put(c.getNodeData("extension").getString(), c //$NON-NLS-1$
                    .getNodeData("mime-type") //$NON-NLS-1$
                    .getString());
            }
            catch (Exception e) {
                log.error("Failed to cache MIMEMapping"); //$NON-NLS-1$
            }
        }
        MIMEMapping.mimeList = null;
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
        return (String) MIMEMapping.cachedContent.get(key.toLowerCase());
    }

    /**
     * Get MIME type String.
     * @param request
     * @return MIME type
     */
    public static String getMIMEType(HttpServletRequest request) {
        String extension = (String) request.getAttribute(Aggregator.EXTENSION);
        if (StringUtils.isEmpty(extension)) {
            extension = StringUtils.substringAfterLast(request.getRequestURI(), "."); //$NON-NLS-1$
            if (StringUtils.isEmpty(extension)) {
                extension = Server.getDefaultExtension();
            }
        }
        String mimeType = (String) MIMEMapping.cachedContent.get(extension.toLowerCase());

        if (mimeType == null && StringUtils.isNotEmpty(extension)) {
            log.info("Cannot find MIME type for extension \"" + extension + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            mimeType = (String) MIMEMapping.cachedContent.get(Server.getDefaultExtension());
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
}
