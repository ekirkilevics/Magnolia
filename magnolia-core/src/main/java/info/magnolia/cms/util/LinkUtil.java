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
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.link.AbsolutePathTransformer;
import info.magnolia.cms.link.LinkHelper;
import info.magnolia.cms.link.PathToLinkTransformer;
import info.magnolia.cms.link.RelativePathTransformer;
import info.magnolia.cms.link.UUIDLink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static final String DEFAULT_EXTENSION = "html";

    public static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

    /**
     * Pattern to find a link
     */
    public static final Pattern LINK_OR_IMAGE_PATTERN = Pattern.compile(
        "(<(a|img) " + // start <a or <img
        "[^>]*" +  // some attributes
        "(href|src)[ ]*=[ ]*\")" + // start href or src
        "([^\"]*)" + // the link
        "(\"" + // ending "
        "[^>]*" + // any attributes
        ">)"); // end the tag

    /**
     * Pattern that matches external and mailto: links.
     */
    public static final Pattern EXTERNAL_LINK_PATTERN = Pattern.compile("^(\\w*://|mailto:|javascript:).*");

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LinkUtil.class);

    /**
     * Util has no public constructor
     */
    private LinkUtil() {
    }

    /**
     * Determines if the given link is internal and relative.
     * @deprecated Use {@link LinkHelper#isInternalRelativeLink(String)} instead
     */
    public static boolean isInternalRelativeLink(String href) {
        return LinkHelper.isInternalRelativeLink(href);
    }

    /**
     * Transforms all the links to the magnolia format. Used during storing.
     * @param str html
     * @return html with changed hrefs
     */
    public static String convertAbsoluteLinksToUUIDs(String str) {
        // get all link tags
        Matcher matcher = LINK_OR_IMAGE_PATTERN.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
           UUIDLink link = new UUIDLink().parseLink(matcher.group(4));
           matcher.appendReplacement(res, "$1" + StringUtils.replace(link.toPattern(), "$", "\\$") + "$5");
        }
        matcher.appendTail(res);
        return res.toString();
    }

    /**
     * Maps a path to a repository. The URI2RepositoryManager is used.
     * @param path
     * @return
     */
    public static String mapPathToRepository(String path) {
        String repository = URI2RepositoryManager.getInstance().getRepository(path);
        if(StringUtils.isEmpty(repository)){
            repository = DEFAULT_REPOSITORY;
        }
        return repository;
    }

    /**
     * Convert the mangolia format to absolute (repository friendly) pathes
     * @param str html
     * @return html with absolute links
     */
    public static String convertUUIDsToAbsoluteLinks(String str) {
        return convertUUIDsToAbsoluteLinks(str, false);
    }

    public static String convertUUIDsToAbsoluteLinks(String str, boolean addContextPath) {
        return convertUUIDsToLinks(str, new AbsolutePathTransformer(addContextPath, true, true));
    }

    /**
     * Transforms stored magnolia style links to relative links. This is used to display them in the browser
     * @param str html
     * @param page the links are relative to this page
     * @return html with proper links
     */
    public static String convertUUIDsToRelativeLinks(String str, final Content page) {
        return convertUUIDsToLinks(str, new RelativePathTransformer(page, true, true));
    }

    public static String convertUUIDsToLinks(String str, PathToLinkTransformer transformer) {
        Matcher matcher = UUIDLink.UUID_PATTERN.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            String pattern = matcher.group();
            UUIDLink link = new UUIDLink().parseUUIDLink(pattern);
            matcher.appendReplacement(res, transformer.transform(link));
        }
        matcher.appendTail(res);
        return res.toString();
    }


    /**
     * @deprecated pass the repository name
     */
    public static String makeAbsolutePathFromUUID(String uuid) {
        return makeAbsolutePathFromUUID(uuid, DEFAULT_REPOSITORY);
    }

    /**
     * Transforms a uuid to a absolute path beginning with a /. This path is used to get the page from the repository.
     * The editor needs this kind of links
     * @param uuid uuid
     * @return path
     */
    public static String makeAbsolutePathFromUUID(String uuid, String repository) {
        UUIDLink link = new UUIDLink();
        link.setRepository(repository);
        link.setUUID(uuid);
        return link.getHandle();
    }

    /**
     * Make a absolute path relative. It adds ../ until the root is reached
     * @param absolutePath absolute path
     * @param page page to be relative to
     * @return relative path
     */
    public static String makeRelativePath(String absolutePath, Content page) {
       return  LinkHelper.makePathRelative(page.getHandle(), absolutePath);
    }

    /**
     * @deprecated pass the repository name
     * @param path
     * @return
     */
    public static String makeUUIDFromAbsolutePath(String path) {
        return makeUUIDFromAbsolutePath(path, DEFAULT_REPOSITORY);
    }

    /**
     * Convert a path to a uuid
     * @param path path to the page
     * @return the uuid if found
     */
    public static String makeUUIDFromAbsolutePath(String path, String repository) {
        UUIDLink link = new UUIDLink();
        link.setRepository(repository);
        link.setHandle(path);
        return link.getUUID();
    }

}