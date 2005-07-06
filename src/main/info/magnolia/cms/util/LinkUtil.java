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
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


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
     * Logger.
     */
    private static Logger log = Logger.getLogger(LinkUtil.class);

    /**
     * The HierarchyManager to get the uuid
     */
    private static HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);

    /**
     * Pattern to find a link
     */
    private static Pattern linkPattern = Pattern.compile("(<a[^>]+href[ ]*=[ ]*\")(/[^\"]*).html(\"[^>]*>)");

    /**
     * Pattern to find a magnolia formatted link
     */
    private static Pattern uuidPattern = Pattern.compile("\\$\\{link:\\{uuid:\\{([^\\}]*)\\},"
        + "repository:\\{[^\\}]*\\}," // has value website unless we support it
        + "workspace:\\{[^\\}]*\\}," // has value default unless we support it
        + "path:\\{([^\\}]*)\\}\\}\\}");

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
            matcher.appendReplacement(res, "$1\\${link:{"
                + "uuid:{"
                + uuid
                + "},"
                + "repository:{website},"
                + "workspace:{default},"
                + "path:{"
                + path
                + "}}}$3");
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
            String uuid = matcher.group(1);
            String absolutePath = LinkUtil.makeAbsolutePathFromUUID(uuid);
            // can't find the uuid
            if (absolutePath.equals(uuid)) {
                absolutePath = matcher.group(2);
            }
            matcher.appendReplacement(res, absolutePath + ".html");
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
            String uuid = matcher.group(1);
            String absolutePath = LinkUtil.makeAbsolutePathFromUUID(uuid);

            // can't find the uuid
            if (absolutePath.equals(uuid)) {
                absolutePath = matcher.group(2);
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
        try {
            // this uses magnolia uuid
            return Search.getContentByUUID(hm.getQueryManager(), uuid).getHandle();

            // this uses the jcr uuid
            // return hm.getContentByUUID(uuid).getHandle();
        }
        catch (Exception e) {
            return uuid;
        }
    }

    /**
     * Make a absolute path relative. It adds ../ until the root is reached
     * @param absolutePath absolute path
     * @param page page to be relative to
     * @return relative path
     */
    public static String makeRelativePath(String absolutePath, Content page) {
        String relativePath = StringUtils.EMPTY;
        int level;
        try {
            level = page.getLevel();
        }
        catch (Exception e) {
            level = 0;
        }

        for (int i = 1; i < level; i++) {
            relativePath += "../";
        }

        if (absolutePath.startsWith("/")) {
            relativePath += absolutePath.substring(1);
        }
        else {
            relativePath += absolutePath;
        }

        return relativePath + ".html";
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
        catch (Exception e) {
            return path;
        }
    }

    /**
     * Util has no public constructor
     */
    private LinkUtil() {
    }
}