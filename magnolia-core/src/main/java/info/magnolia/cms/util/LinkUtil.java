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
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.context.MgnlContext;

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

    public static final String DEFAULT_EXTENSION = "html";

    public static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

    public interface PathToLinkTransformer {
        String transform(String uuid, String absolutePath);
    }

    /**
     * Pattern to find a link
     */
    private static final Pattern linkPattern = Pattern
        .compile("(<a[^>]+href[ ]*=[ ]*\")(/[^\"]*).html((#[^\"]*)?\"[^>]*>)"); //$NON-NLS-1$

    /**
     * Pattern that matches external and mailto: links.
     */
    private static final Pattern externalLinkPattern = Pattern.compile("^(\\w*://|mailto:|javascript:).*");

    /**
     * Pattern to find a magnolia formatted link
     */
    private static Pattern uuidPattern = Pattern.compile("\\$\\{link:\\{uuid:\\{([^\\}]*)\\}," //$NON-NLS-1$
        + "repository:\\{([^\\}]*)\\}," // has value website unless we support it //$NON-NLS-1$
        + "workspace:\\{[^\\}]*\\}," // has value default unless we support it //$NON-NLS-1$
        + "path:\\{([^\\}]*)\\}\\}\\}"); //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(LinkUtil.class);

    /**
     * Determines if the given link is internal and relative.
     */
    public static boolean isInternalRelativeLink(String href) {
        // TODO : this could definitely be improved
        return !externalLinkPattern.matcher(href).matches() && !href.startsWith("/") && !href.startsWith("#");
    }

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
            String repository = mapPathToRepository(path);
            String uuid = makeUUIDFromAbsolutePath(path, repository);
            matcher.appendReplacement(res, "$1\\${link:{" //$NON-NLS-1$
                + "uuid:{" //$NON-NLS-1$
                + uuid
                + "}," //$NON-NLS-1$
                + "repository:{" + repository + "}," //$NON-NLS-1$
                + "workspace:{default}," //$NON-NLS-1$
                + "path:{" //$NON-NLS-1$
                + path
                + "}}}$3"); //$NON-NLS-1$
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
        return convertUUIDsToLinks(str, new PathToLinkTransformer(){
            public String transform(String absolutePath, String repository) {
                return absolutePath;
            }
        });

    }

    /**
     * Transforms stored magnolia style links to relative links. This is used to display them in the browser
     * @param str html
     * @param page the links are relative to this page
     * @return html with proper links
     */
    public static String convertUUIDsToRelativeLinks(String str, final Content page) {
        return convertUUIDsToLinks(str, new PathToLinkTransformer(){
            public String transform(String absolutePath, String repository) {
                 return makeRelativePath(absolutePath, page);
            }
        });
    }

    public static String convertUUIDsToLinks(String str, PathToLinkTransformer transformer) {
        Matcher matcher = uuidPattern.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            String uuid = matcher.group(1);
            String repository = StringUtils.defaultIfEmpty(matcher.group(2), DEFAULT_REPOSITORY);
            String absolutePath = matcher.group(3);

            String link = null;

            if (StringUtils.isNotEmpty(uuid)) {
                link = LinkUtil.makeAbsolutePathFromUUID(uuid, repository);
            }

            // can't find the uuid
            if (StringUtils.isEmpty(link)) {
                link = absolutePath;
                log.warn("Was not able to get the page by uuid. Will use the saved path {}", absolutePath);
            }

            // to relative path
            link = transformer.transform(absolutePath, repository);
            // TODO support other extensions than html. used for MGNLDMS-84
            link += "." + DEFAULT_EXTENSION;
            matcher.appendReplacement(res, link);
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
        Content content = null;

        // first use the jcr:uuid (since 3.0)
        try {
            content = MgnlContext.getHierarchyManager(repository).getContentByUUID(uuid);
        }

        // then the old mgnl:uuid
        // TODO remove this in later versions
        catch (Exception e) {
            log.debug("Was not able to get the page by the jcr:uuid. will try the old mgnl:uuid");

            QueryManager qmanager = MgnlContext.getHierarchyManager(repository).getQueryManager();

            if (qmanager != null) {
                // this uses magnolia uuid
                content = getContentByMgnlUUID(qmanager, uuid);
            }
            else {
                log.info(
                    "SearchManager not configured for website repositoy, unable to generate absolute path for UUID {}",
                    uuid);
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

        return relativePath.toString();
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
        try {
            return MgnlContext.getHierarchyManager(repository).getContent(path).getUUID();
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