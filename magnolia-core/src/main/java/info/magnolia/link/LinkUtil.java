/**
 * This file Copyright (c) 2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.link;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.AbstractI18nContentSupport;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for various operations necessary for link transformations and handling.
 * @author had
 *
 */
public class LinkUtil {

    /**
     * Pattern that matches external and mailto: links.
     */
    public static final Pattern EXTERNAL_LINK_PATTERN = Pattern.compile("^(\\w*://|mailto:|javascript:).*");

    public static final String DEFAULT_EXTENSION = "html";

    public static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

    /**
     * Pattern to find a link.
     */
    public static final Pattern LINK_OR_IMAGE_PATTERN = Pattern.compile(
        "(<(a|img|embed) " + // start <a or <img
        "[^>]*" +  // some attributes
        "(href|src)[ ]*=[ ]*\")" + // start href or src
        "([^\"]*)" + // the link
        "(\"" + // ending "
        "[^>]*" + // any attributes
        ">)"); // end the tag

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LinkUtil.class);


    //-- conversions to UUID - singles
    /**
     * Converts provided path to an uuid.
     * @param path path to the page
     * @param repoName Repository in which to look for given path
     * @return the uuid 
     * @throws UUIDLinkException When node described by provided path doesn't exists or can't be retrieved.
     */
    public static String convertAbsolutePathToUUID(String path, String repoName) throws UUIDLinkException {
        UUIDLink link = new UUIDLink();
        link.setRepository(repoName);
        return link.parseLink(path).getUUID();
    }

    //-- conversions from UUID - singles
    /**
     * Transforms a uuid to a handle beginning with a /. This path is used to get the page from the repository.
     * The editor needs this kind of links
     * @param uuid uuid
     * @return path
     */
    public static String convertUUIDtoHandle(String uuid, String repository) {
        UUIDLink link = new UUIDLink();
        link.setRepository(repository);
        link.setUUID(uuid);
        return link.getHandle();
    }

    /**
     * Transforms a uuid to an uri. It does not add the context path. In difference from {@link #convertUUIDtoHandle(String, String)}, 
     * this method will apply all uri to repository mappings as well as i18n.
     */
    public static String convertUUIDtoURI(String uuid, String repository) {
        UUIDLink link = new UUIDLink();
        link.setRepository(repository);
        link.setUUID(uuid);
        return LinkTransformerManager.getInstance().getAbsolute(false, true, true).transform(link);
    }

    //-- conversions to UUID - bulk 
    /**
     * Parses provided html and transforms all the links to the magnolia format. Used during storing.
     * @param html html code with links to be converted
     * @return html with changed hrefs
     */
    public static String convertAbsoluteLinksToUUIDs(String html) {
        // get all link tags
        Matcher matcher = LINK_OR_IMAGE_PATTERN.matcher(html);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            final String href = matcher.group(4);
            if (!isExternalLinkOrAnchor(href)) {
                UUIDLink link = new UUIDLink();
                try {
                    link.parseLink(href);
                    matcher.appendReplacement(res, "$1" + StringUtils.replace(link.toPattern(), "$", "\\$") + "$5");
                }
                catch (info.magnolia.link.UUIDLinkException e) {
                    // this is expected if the link is an absolute path to something else
                    // than content stored in the repository
                    matcher.appendReplacement(res, "$0");
                    log.debug("can't parse link", e);
                }
            }
            else{
                matcher.appendReplacement(res, "$0");
            }
        }
        matcher.appendTail(res);
        return res.toString();
    }

    //-- conversions from UUID - bulk
    /**
     * Converts links in provided html from Magnolia UUID pattern links to links suitable for editor.
     * @param str provided html
     * @return converted html
     * @see EditorLinkTransformer
     */
    public static String convertToEditorLinks(String str) {
        return convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getEditorLink());
    }

    /**
     * Convert all links in provided html from the UUID format to absolute paths.
     * @param html html with UUIDs
     * @return html with absolute links
     */
    public static String convertToAbsoluteLinks(String html, boolean addContextPath) {
        return convertLinksFromUUIDPattern(html, LinkTransformerManager.getInstance().getAbsolute(addContextPath, true, true));
    }

    /**
     * Transforms all links in provided html from UUID format to relative links. Links will be made relative to the path provided 
     * in the {@code currentPath} attribute. This method is used to convert stored html to form suitable for display in the browser.
     * @param str html
     * @param currentPath url/path to make links relative to
     * @return html with transformed links
     */
    public static String convertToRelativeLinks(String str, String currentPath) {
        return convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getRelative(currentPath, true, true));
    }

    /**
     * Transforms all links in provided html to external links so the Magnolia content linked in the html can be accessed even if 
     * generated content is displayed on systems others then Magnolia itself.
     * @param str html
     * @return converted html with complete universally accessible links.
     * @see CompleteUrlPathTransformer
     */
    public static String convertToExternalLinks(String str) {
        return convertLinksFromUUIDPattern(str, LinkTransformerManager.getInstance().getCompleteUrl(true, true));
    }

    /**
     * Converts all links in provided html to either relative or absolute links with or without context path from the UUID patterns.
     * The outputted html will contain relative or absolute links based on the settings in /server/rendering/linkManagement
     * @param html html with links in UUID format
     * @param currentPath path to make links relative to. If {@code makeBrowserLinksRelative} is set to false, this value can be null.
     * @return html with translated links.
     * @see #convertToRelativeLinks(String, String)
     * @see #convertToAbsoluteLinks(String, boolean)
     */
    public static String convertToBrowserLinks(String html, String currentPath) {
        return convertLinksFromUUIDPattern(html, LinkTransformerManager.getInstance().getBrowserLink(currentPath));

    }

    /**
     * Converts provided html with links in UUID pattern format to any other kind of links based on provided link transformer.
     * @param str Html with UUID links
     * @param transformer Link transformer
     * @return converted html with links as created by provided transformer.
     * @see LinkTransformerManager
     */
    public static String convertLinksFromUUIDPattern(String str, LinkTransformer transformer) {
        Matcher matcher = UUIDLink.UUID_PATTERN.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            UUIDLink link = new UUIDLink().initByUUIDPatternMatcher(matcher);
            String replacement = transformer.transform(link);
            // Replace "\" with "\\" and "$" with "\$" since Matcher.appendReplacement treats these characters specially
            replacement = StringUtils.replace(replacement, "\\", "\\\\");
            replacement = StringUtils.replace(replacement,"$", "\\$");
            matcher.appendReplacement(res, replacement);
        }
        matcher.appendTail(res);
        return res.toString();
    }

    /**
     * Determines if the given link is internal and relative.
     */
    public static boolean isInternalRelativeLink(String href) {
        // TODO : this could definitely be improved
        return !isExternalLinkOrAnchor(href) && !href.startsWith("/");
    }

    /**
     * Determines whether the given link is external link or anchor (i.e. returns true for all non translatable links).
     */
    public static boolean isExternalLinkOrAnchor(String href) {
       return LinkUtil.EXTERNAL_LINK_PATTERN.matcher(href).matches() || href.startsWith("#");
    }

    /**
     * Make a absolute path relative. It adds ../ until the root is reached
     * @param absolutePath absolute path
     * @param url page to be relative to
     * @return relative path
     */
    public static String makePathRelative(String url, String absolutePath){
        String fromPath = StringUtils.substringBeforeLast(url, "/");
        String toPath = StringUtils.substringBeforeLast(absolutePath, "/");

        String[] fromDirectories = StringUtils.split(fromPath, "/");
        String[] toDirectories = StringUtils.split(toPath, "/");

        int pos=0;
        while(pos < fromDirectories.length && pos < toDirectories.length && fromDirectories[pos].equals(toDirectories[pos])){
            pos++;
        }

        String rel = "";
        for(int i=pos; i < fromDirectories.length; i++ ){
            rel += "../";
        }

        for(int i=pos; i < toDirectories.length; i++ ){
            rel = rel + toDirectories[i] + "/";
        }

        rel += StringUtils.substringAfterLast(absolutePath, "/");

        return rel;
    }

    /**
     * Maps a path to a repository.
     * @param path URI
     * @return repository denoted by the provided URI.
     */
    public static String mapPathToRepository(String path) {
        String repository = URI2RepositoryManager.getInstance().getRepository(path);
        if(StringUtils.isEmpty(repository)){
            repository = DEFAULT_REPOSITORY;
        }
        return repository;
    }

    /**
     * Appends a parameter to the given url, using ?, or & if there are already
     * parameters in the given url. <strong>Warning:</strong> It does not
     * <strong>replace</strong> an existing parameter with the same name.
     */
    public static void addParameter(StringBuffer uri, String name, String value) {
        if (uri.indexOf("?") < 0) {
            uri.append('?');
        } else {
            uri.append('&');
        }
        uri.append(name).append('=');
        try {
            uri.append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("It seems your system does not support UTF-8 !?", e);
        }
    }
    
    /**
     * Creates absolute link including context path for provided node data.
     * @param nodedata Node data to create link for.
     * @param i18n Flag denoting whether or not to add i18n info to the link based on current locale.
     * @return Absolute link to the provided node data.
     * @see AbstractI18nContentSupport
     */
    public static String createAbsoluteLink(NodeData nodedata, boolean i18n) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
        if(nodedata == null || !nodedata.isExist()){
            return null;
        }
        UUIDLink link = new UUIDLink();
        link.setNode(nodedata.getParent());
        link.setRepository(nodedata.getHierarchyManager().getName());
        link.setNodeData(nodedata);
        link.setNodeDataName(nodedata.getName());
        return LinkTransformerManager.getInstance().getAbsolute(true, true, i18n).transform(link);
    }

    /**
     * Creates absolute link including context path to the content denoted by repository and uuid. 
     * @param repositoryId Parent repository of the content of interest.
     * @param uuid UUID of the content to create link to.
     * @param i18n Flag denoting whether or not to add i18n info to the link based on current locale.
     * @return Absolute link to the content with provided UUID.
     * @see AbstractI18nContentSupport
     */
    public static String createLink(String repositoryId, String uuid, boolean i18n) throws RepositoryException {
        HierarchyManager hm = MgnlContext.getHierarchyManager(repositoryId);
        Content node = hm.getContentByUUID(uuid);
        return createAbsoluteLink(node, i18n);
    }

    /**
     * Creates absolute link including context path to the provided content. 
     * @param content content to create link to.
     * @param i18n Flag denoting whether or not to add i18n info to the link based on current locale.
     * @return Absolute link to the provided content.
     * @see AbstractI18nContentSupport
     */
    public static String createAbsoluteLink(Content content, boolean i18n) {
        if(content == null){
            return null;
        }
        UUIDLink link = new UUIDLink();
        link.setNode(content);
        link.setRepository(content.getHierarchyManager().getName());
        return LinkTransformerManager.getInstance().getAbsolute(true, true, i18n).transform(link);
    }

}
