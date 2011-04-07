/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
import info.magnolia.context.MgnlContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // is this proxied or not? Tests says no.
    //private static final LinkTransformerManager linkManager = LinkTransformerManager.getInstance();

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


    //-- conversions from UUID - singles
    /**
     * Transforms a uuid to a handle beginning with a /. This path is used to get the page from the repository.
     * The editor needs this kind of links.
     */
    public static String convertUUIDtoHandle(String uuid, String repository) throws LinkException {
        return LinkFactory.createLink(repository, uuid).getHandle();
    }

    /**
     * Transforms a uuid to an uri. It does not add the context path. In difference from {@link Link#getHandle()},
     * this method will apply all uri to repository mappings as well as i18n.
     */
    public static String convertUUIDtoURI(String uuid, String repository) throws LinkException {
        return LinkTransformerManager.getInstance().getAbsolute(false).transform(LinkFactory.createLink(repository, uuid));
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
                try {
                    Link link = LinkFactory.parseLink(href);
                    String linkStr = LinkFactory.toPattern(link);
                    linkStr = StringUtils.replace(linkStr, "\\", "\\\\");
                    linkStr = StringUtils.replace(linkStr, "$", "\\$");
                    matcher.appendReplacement(res, "$1" + linkStr + "$5");
                }
                catch (LinkException e) {
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
     * Converts provided html with links in UUID pattern format to any other kind of links based on provided link transformer.
     * @param str Html with UUID links
     * @param transformer Link transformer
     * @return converted html with links as created by provided transformer.
     * @see LinkTransformerManager
     */
    public static String convertLinksFromUUIDPattern(String str, LinkTransformer transformer) throws LinkException {
        Matcher matcher = LinkFactory.UUID_PATTERN.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            Link link = LinkFactory.createLink(matcher.group(1), matcher.group(2), matcher.group(5), matcher.group(7), matcher.group(8), matcher.group(10), matcher.group(12));
            String replacement = transformer.transform(link);
            // Replace "\" with "\\" and "$" with "\$" since Matcher.appendReplacement treats these characters specially
            replacement = StringUtils.replace(replacement, "\\", "\\\\");
            replacement = StringUtils.replace(replacement,"$", "\\$");
            matcher.appendReplacement(res, replacement);
        }
        matcher.appendTail(res);
        return res.toString();
    }

    public static String convertLinksFromUUIDPattern(String str) throws LinkException {
        LinkTransformer transformer = LinkTransformerManager.getInstance().getBrowserLink(null);
        return convertLinksFromUUIDPattern(str, transformer);
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

        // reference to parent folder
        if (StringUtils.equals(fromPath, toPath) && StringUtils.endsWith(absolutePath, "/")) {
            return ".";
        }

        String[] fromDirectories = StringUtils.split(fromPath, "/");
        String[] toDirectories = StringUtils.split(toPath, "/");

        int pos=0;
        while(pos < fromDirectories.length && pos < toDirectories.length && fromDirectories[pos].equals(toDirectories[pos])){
            pos++;
        }

        StringBuilder rel = new StringBuilder();
        for(int i=pos; i < fromDirectories.length; i++ ){
            rel.append("../");
        }

        for(int i=pos; i < toDirectories.length; i++ ){
            rel.append(toDirectories[i] + "/");
        }

        rel.append(StringUtils.substringAfterLast(absolutePath, "/"));

        return rel.toString();
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
     * @return Absolute link to the provided node data.
     * @see AbstractI18nContentSupport
     */
    public static String createAbsoluteLink(NodeData nodedata) throws LinkException {
        if(nodedata == null || !nodedata.isExist()){
            return null;
        }
        return LinkTransformerManager.getInstance().getAbsolute().transform(LinkFactory.createLink(nodedata));
    }

    /**
     * Creates absolute link including context path to the provided content and performing all URI2Repository mappings and applying locales.
     * @param uuid UUID of content to create link to.
     * @param repository Name of the repository where content is located.
     * @return Absolute link to the provided content.
     * @see AbstractI18nContentSupport
     */
    public static String createAbsoluteLink(String repository, String uuid) throws RepositoryException {
        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
        Content node = hm.getContentByUUID(uuid);
        return createAbsoluteLink(node);
    }

    /**
     * Creates absolute link including context path to the provided content and performing all URI2Repository mappings and applying locales.
     * @param content content to create link to.
     * @return Absolute link to the provided content.
     * @see AbstractI18nContentSupport
     */
    public static String createAbsoluteLink(Content content) {
        if(content == null){
            return null;
        }
        return LinkTransformerManager.getInstance().getAbsolute().transform(LinkFactory.createLink(content));
    }

    /**
     * Creates a complete url to access given content from external systems applying all the URI2Repository mappings and locales.
     * @param content
     * @return
     */
    public static String createExternalLink(Content content) {
        if(content == null){
            return null;
        }
        return LinkTransformerManager.getInstance().getCompleteUrl().transform(LinkFactory.createLink(content));
    }

    /**
     * Creates link guessing best possible link format from current site and provided node.
     * @param nodedata Node data to create link for.
     * @return Absolute link to the provided node data.
     * @see AbstractI18nContentSupport
     */
    public static String createLink(Content node) {
        if(node == null){
            return null;
        }
        return LinkTransformerManager.getInstance().getBrowserLink(node.getHandle()).transform(LinkFactory.createLink(node));
    }

    /**
     * Creates link guessing best possible link format from current site and provided node data.
     * @param nodedata Node data to create link for.
     * @return Absolute link to the provided node data.
     * @see AbstractI18nContentSupport
     */
    public static String createLink(NodeData nodedata) throws LinkException {
        if(nodedata == null || !nodedata.isExist()){
            return null;
        }
        try {
            return LinkTransformerManager.getInstance().getBrowserLink(nodedata.getParent().getHandle()).transform(LinkFactory.createLink(nodedata));
        } catch (RepositoryException e) {
            throw new LinkException(e.getMessage(), e);
        }
    }

    /**
     * Creates link guessing best possible link format from current site and provided content.
     * @param uuid UUID of content to create link to.
     * @param repository Name of the repository where content is located.
     * @return Absolute link to the provided content.
     * @see AbstractI18nContentSupport
     */
    public static String createLink(String repository, String uuid) throws RepositoryException {
        HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
        Content node = hm.getContentByUUID(uuid);
        return createLink(node);
    }
}
