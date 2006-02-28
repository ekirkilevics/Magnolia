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
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to store links in a format so that one can make relative pathes on the public site. Later we will store the
 * UUID, but in the current version the UUID is changeing during activation!
 * <p>
 * It stores the links in the following format: ${link:{uuid:{},path:{}}}. We store allready the UUID.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public final class LinkUtil {

    /**
     * The HierarchyManager to get the uuid
     */
    private static HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);

    /**
     * Pattern to find a link
     */
    private static Pattern linkPattern = Pattern.compile("(<a[^>]+href[ ]*=[ ]*\")(/[^\"]*).html((#[^\"]*)?\"[^>]*>)"); //$NON-NLS-1$

    /**
     * Pattern to find a magnolia formatted link
     */
    private static Pattern uuidPattern = Pattern.compile("\\$\\{link:\\{uuid:\\{([^\\}]*)\\}," //$NON-NLS-1$
        + "repository:\\{[^\\}]*\\}," // has value website unless we support it //$NON-NLS-1$
        + "workspace:\\{[^\\}]*\\}," // has value default unless we support it //$NON-NLS-1$
        + "path:\\{([^\\}]*)\\}\\}\\}"); //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LinkUtil.class);

    /**
     * Transforms all the links to the magnolia format. Used during storing.
     * @param str html
     * @return html with changed hrefs
     */
    public static String convertAbsoluteLinksToUUIDs(String str) {
        // get all link tags
        Matcher matcher = linkPattern.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            String path = matcher.group(2);
            String uuid = makeUUIDFromAbsolutePath(path);
            matcher.appendReplacement(res, "$1\\${link:{" //$NON-NLS-1$
                + "uuid:{" //$NON-NLS-1$
                + uuid
                + "}," //$NON-NLS-1$
                + "repository:{website}," //$NON-NLS-1$
                + "workspace:{default}," //$NON-NLS-1$
                + "path:{" //$NON-NLS-1$
                + path
                + "}}}$3"); //$NON-NLS-1$
        }
        matcher.appendTail(res);
        return res.toString();
    }

    /**
     * Convert the mangolia format to absolute (repository friendly) pathes
     * @param str html
     * @return html with absolute links
     */
    public static String convertUUIDsToAbsoluteLinks(String str) {
        Matcher matcher = uuidPattern.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            String absolutePath = null;
            String uuid = matcher.group(1);

            if (StringUtils.isNotEmpty(uuid)) {
                absolutePath = LinkUtil.makeAbsolutePathFromUUID(uuid);
            }

            // can't find the uuid
            if (StringUtils.isEmpty(absolutePath)) {
                log.error("Was not able to get the page by jcr:uuid nor by mgnl:uuid. Will use the saved path");
                absolutePath = matcher.group(2);
            }
            matcher.appendReplacement(res, absolutePath + ".html"); //$NON-NLS-1$
        }
        matcher.appendTail(res);
        return res.toString();
    }

    /**
     * Transforms stored magnolia style links to relative links. This is used to display them in the browser
     * @param str html
     * @param page the links are relative to this page
     * @return html with proper links
     */
    public static String convertUUIDsToRelativeLinks(String str, Content page) {
        Matcher matcher = uuidPattern.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            String absolutePath = null;
            String uuid = matcher.group(1);

            if (StringUtils.isNotEmpty(uuid)) {
                absolutePath = LinkUtil.makeAbsolutePathFromUUID(uuid);
            }

            // can't find the uuid
            if (StringUtils.isEmpty(absolutePath)) {
                absolutePath = matcher.group(2);
                log.error("Was not able to get the page by jcr:uuid nor by mgnl:uuid. Will use the saved path ["
                    + absolutePath
                    + "]");
            }

            // to relative path
            String relativePath = makeRelativePath(absolutePath, page);
            matcher.appendReplacement(res, relativePath);
        }
        matcher.appendTail(res);
        return res.toString();
    }

    /**
     * Transforms a uuid to a absolute path beginning with a /. This path is used to get the page from the repository.
     * The editor needs this kind of links
     * @param uuid uuid
     * @return path
     */
    public static String makeAbsolutePathFromUUID(String uuid) {
        Content content = null;

        // first use the jcr:uuid (since 2.2)
        try {
            content = hm.getContentByUUID(uuid);
        }

        // then the old mgnl:uuid
        catch (Exception e) {
            log.error("Was not able to get the page by the jcr:uuid. will try the old mgnl:uuid");

            QueryManager qmanager = hm.getQueryManager();

            if (qmanager != null) {
                // this uses magnolia uuid
                content = getContentByMgnlUUID(qmanager, uuid);
            }
            else {
                log
                    .info("SearchManager not configured for website repositoy, unable to generate absolute path for UUID ["
                        + uuid
                        + "]");
            }
        }

        if (content != null) {
            return content.getHandle();
        }
        return null;
    }

    /**
     * Make a absolute path relative. It adds ../ until the root is reached
     * @param absolutePath absolute path
     * @param page page to be relative to
     * @return relative path
     */
    public static String makeRelativePath(String absolutePath, Content page) {
        StringBuffer relativePath = new StringBuffer();
        int level;
        try {
            level = page.getLevel();
        }
        catch (RepositoryException e) {
            level = 0;
        }

        for (int i = 1; i < level; i++) {
            relativePath.append("../"); //$NON-NLS-1$
        }

        if (absolutePath.startsWith("/")) {
            relativePath.append(StringUtils.substringAfter(absolutePath, "/"));
        }
        else {
            relativePath.append(absolutePath);
        }

        relativePath.append(".html"); //$NON-NLS-1$

        return relativePath.toString();
    }

    /**
     * Convert a path to a uuid
     * @param path path to the page
     * @return the uuid if found
     */
    public static String makeUUIDFromAbsolutePath(String path) {
        try {
            return hm.getContent(path).getUUID();
        }
        catch (RepositoryException e) {
            return path;
        }
    }

    /**
     * Util has no public constructor
     */
    private LinkUtil() {
    }

    /**
     * Used for old content
     * @param queryManager
     * @param uuid
     * @return
     * @deprecated
     */
    private static Content getContentByMgnlUUID(QueryManager queryManager, String uuid) {
        try {
            String statement = "SELECT * FROM nt:base where mgnl:uuid like '" + uuid + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            Query q = queryManager.createQuery(statement, Query.SQL);
            QueryResult result = q.execute();
            Iterator it = result.getContent().iterator();
            while (it.hasNext()) {
                Content foundObject = (Content) it.next();
                return foundObject;
            }
        }
        catch (RepositoryException e) {
            log.error("Exception caught", e);
        }
        return null;
    }

}