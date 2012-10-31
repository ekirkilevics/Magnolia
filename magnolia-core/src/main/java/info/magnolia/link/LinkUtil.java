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

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.apache.jackrabbit.spi.commons.conversion.PathParser;
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
    public static final Pattern EXTERNAL_LINK_PATTERN = Pattern.compile("^(\\w*://|mailto:|javascript:|tel:).*");

    public static final String DEFAULT_EXTENSION = "html";

    public static final String DEFAULT_REPOSITORY = RepositoryConstants.WEBSITE;

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
     * Pattern to find a magnolia formatted uuid link.
     */
    public static Pattern UUID_PATTERN = Pattern.compile(
        "\\$\\{link:\\{uuid:\\{([^\\}]*)\\}," // the uuid of the node
        + "repository:\\{([^\\}]*)\\},"
        + "(workspace:\\{[^\\}]*\\},)?" // is not supported anymore
        + "(path|handle):\\{([^\\}]*)\\}"        // fallback handle should not be used unless the uuid is invalid
        + "(,nodeData:\\{([^\\}]*)\\}," // in case we point to a binary (node data has no uuid!)
        + "extension:\\{([^\\}]*)\\})?" // the extension to be used in rendering
        + "\\}\\}"  // the handle
        + "(#([^\\?\"]*))?" // anchor
        + "(\\?([^\"]*))?"); // parameters

    /**
     * Pattern to find a link.
     */
    public static final Pattern LINK_PATTERN = Pattern.compile(
        "(/[^\\.\"#\\?]*)" + // the handle
        "(\\.([\\w[^#\\?]]+))?" + // extension (if any)
        "(#([^\\?\"]*))?" + // anchor
        "(\\?([^\"]*))?" // parameters
    );

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(LinkUtil.class);


    //-- conversions from UUID - singles
    /**
     * Transforms a uuid to a handle beginning with a /. This path is used to get the page from the repository.
     * The editor needs this kind of links.
     * @throws RepositoryException 
     */
    public static String convertUUIDtoHandle(String uuid, String repository) throws LinkException, RepositoryException {
        return createLinkInstance(repository, uuid).getHandle();
    }

    /**
     * Transforms a uuid to an uri. It does not add the context path. In difference from {@link Link#getHandle()},
     * this method will apply all uri to repository mappings as well as i18n.
     */
    public static String convertUUIDtoURI(String uuid, String repository) throws LinkException {
        return LinkTransformerManager.getInstance().getAbsolute(false).transform(createLinkInstance(repository, uuid));
    }

    //-- conversions to UUID - bulk
    /**
     * Parses provided html and transforms all the links to the magnolia format. Used during storing.
     * @param html html code with links to be converted
     * @return html with changed hrefs
     * @throws RepositoryException 
     */
    public static String convertAbsoluteLinksToUUIDs(String html) {
        // get all link tags
        Matcher matcher = LINK_OR_IMAGE_PATTERN.matcher(html);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            final String href = matcher.group(4);
            if (!isExternalLinkOrAnchor(href)) {
                try {
                    Link link = parseLink(href);
                    String linkStr = toPattern(link);
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
        Matcher matcher = UUID_PATTERN.matcher(str);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            Link link = createLinkInstance(matcher.group(1), matcher.group(2), matcher.group(5), matcher.group(7), matcher.group(8), matcher.group(10), matcher.group(12));
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
        //String repository = URI2RepositoryManager.getInstance().getRepository(path);
        String repository = getURI2RepositoryManager().getRepository(path);
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
     * 
     * @param nodedata
     *            Node data to create link for.
     * @return Absolute link to the provided node data.
     * @throws RepositoryException 
     * @see info.magnolia.cms.i18n.AbstractI18nContentSupport
     */
    public static String createAbsoluteLink(NodeData nodedata) throws LinkException, RepositoryException {
        if(nodedata == null || !nodedata.isExist()){
            return null;
        }
        return LinkTransformerManager.getInstance().getAbsolute().transform(createLinkInstance(nodedata));
    }

    /**
     * Creates absolute link including context path to the provided content and performing all URI2Repository mappings and applying locales.
     * 
     * @param uuid
     *            UUID of content to create link to.
     * @param repository
     *            Name of the repository where content is located.
     * @return Absolute link to the provided content.
     * @see info.magnolia.cms.i18n.AbstractI18nContentSupport
     */
    public static String createAbsoluteLink(String repository, String uuid) throws RepositoryException {
        Node jcrNode = MgnlContext.getJCRSession(repository).getNodeByIdentifier(uuid);
        /*TODO update with Node method*/
        return createAbsoluteLink(ContentUtil.asContent(jcrNode));
    }

    /**
     * Creates absolute link including context path to the provided content and performing all URI2Repository mappings and applying locales.
     * 
     * @param content
     *            content to create link to.
     * @return Absolute link to the provided content.
     * @throws RepositoryException 
     * @see info.magnolia.cms.i18n.AbstractI18nContentSupport
     */
    public static String createAbsoluteLink(Content content) throws RepositoryException {
        if(content == null){
            return null;
        }
        return LinkTransformerManager.getInstance().getAbsolute().transform(createLinkInstance(content));
    }

    /**
     * Creates a complete url to access given content from external systems applying all the URI2Repository mappings and locales.
     * @param content
     * @return
     * @throws RepositoryException
     */
    public static String createExternalLink(Content content) throws RepositoryException {
        if(content == null){
            return null;
        }
        return LinkTransformerManager.getInstance().getCompleteUrl().transform(createLinkInstance(content));
    }

    /**
     * Creates link guessing best possible link format from current site and provided node.
     * 
     * @param nodedata
     *            Node data to create link for.
     * @return Absolute link to the provided node data.
     * @throws RepositoryException 
     * @see info.magnolia.cms.i18n.AbstractI18nContentSupport
     */
    public static String createLink(Content node) throws RepositoryException {
        if(node == null){
            return null;
        }
        if (node.isNodeType(MgnlNodeType.NT_RESOURCE)) {
            try {
                // This content is what was formerly hidden as binary node data and still needs to be treated as such until new Link API is ready.
                String name = node.getName();
                Content parent = node.getParent();
                return LinkUtil.createLink(parent.getNodeData(name));
            } catch (RepositoryException e) {
                log.debug(e.getMessage(), e);
            } catch (LinkException e) {
                log.debug(e.getMessage(), e);
            }
        }
        return LinkTransformerManager.getInstance().getBrowserLink(node.getHandle()).transform(createLinkInstance(node));
    }

    /**
     * Creates link guessing best possible link format from current site and provided node data.
     * 
     * @param nodedata
     *            Node data to create link for.
     * @return Absolute link to the provided node data.
     * @see info.magnolia.cms.i18n.AbstractI18nContentSupport
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
     * 
     * @param uuid
     *            UUID of content to create link to.
     * @param repository
     *            Name of the repository where content is located.
     * @return Absolute link to the provided content.
     * @see info.magnolia.cms.i18n.AbstractI18nContentSupport
     */
    public static String createLink(String repository, String uuid) throws RepositoryException {
        Node node = MgnlContext.getJCRSession(repository).getNodeByIdentifier(uuid);
        /*TODO update with Node method*/
        return createLink(ContentUtil.asContent(node));
    }
    
    public static Link createLinkInstance(Content node) throws RepositoryException{
        return new Link(node);
    }
    
    public static Link createLinkInstance(NodeData nodeData) throws RepositoryException, LinkException{
        try {
            return new Link(nodeData.getParent().getWorkspace().getName(), nodeData.getParent(), nodeData);
        } catch (RepositoryException e) {
            throw new LinkException("can't find node " + nodeData , e);
        }
    }
    
    /**
     * Creates link to the content denoted by repository and uuid.
     * @param repository Parent repository of the content of interest.
     * @param uuid UUID of the content to create link to.
     * @return link to the content with provided UUID.
     */
    public static Link createLinkInstance(String repository, String uuid) throws LinkException {
        try {
            return new Link(MgnlContext.getHierarchyManager(repository).getContentByUUID(uuid));
        } catch (RepositoryException e) {
            throw new LinkException("can't get node with uuid " + uuid + " and repository " + repository);
        }
    }
    
    /**
     * Creates link to the content identified by the repository and path. Link will use specified extension and will also contain the anchor and parameters if specified.
     * @param repository Source repository for the content.
     * @param path Path to the content of interest.
     * @param extension Optional extension to be used in the link
     * @param anchor Optional link anchor.
     * @param parameters Optional link parameters.
     * @return Link pointing to the content denoted by repository and path including extension, anchor and parameters if such were provided.
     * @throws LinkException
     */
    public static Link createLinkInstance(String repository, String path, String extension, String anchor, String parameters) throws LinkException {
        Content node = null;
        String fileName = null;
        String nodeDataName = null;
        NodeData nodeData = null;
        try {
            HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
            boolean exists = false;
            try {
                // jackrabbit own path parser
                // TODO: rewrite this as Magnolia method or allow configuration of parser per JCR impl
                PathParser.checkFormat(path);
            } catch (MalformedPathException e) {
                // we first check for path incl. the file name. While file name might not be necessarily part of the path, it might contain also non ascii chars. If that is the case, parsing exception will occur so we know that path with filename can't exist.
                    exists = false;
            }
            exists = hm.isExist(path) && !hm.isNodeData(path);
            if (exists) {
                node = hm.getContent(path);
            }
            if (node == null) {
                // this is a binary containing the name at the end
                // this name is stored as an attribute but is not part of the handle
                if (hm.isNodeData(StringUtils.substringBeforeLast(path, "/"))) {
                    fileName = StringUtils.substringAfterLast(path, "/");
                    path = StringUtils.substringBeforeLast(path, "/");
                }

                // link to the binary node data
                if (hm.isNodeData(path)) {
                    nodeDataName = StringUtils.substringAfterLast(path, "/");
                    path = StringUtils.substringBeforeLast(path, "/");
                    node = hm.getContent(path);
                    nodeData = node.getNodeData(nodeDataName);
                }
            }
            if (node == null) {
                throw new LinkException("can't find node " + path + " in repository " + repository);
            }
        } catch (RepositoryException e) {
            throw new LinkException("can't get node with path " + path + " from repository " + repository);
        }

        Link link = new Link(node);
        link.setAnchor(anchor);
        link.setExtension(extension);
        link.setParameters(parameters);
        link.setFileName(fileName);
        link.setNodeDataName(nodeDataName);
        link.setNodeData(nodeData);
        link.setHandle(path);
        return link;
    }
    
    /**
     * Creates link based on provided parameters. Should the uuid be non existent or the fallback handle invalid, creates nonetheless an <em>"undefined"</em> {@link Link} object,
     * pointing to the non existing uuid so that broken link detection tools can find it.
     * @param uuid UUID of the content
     * @param repository Content repository name.
     * @param fallbackHandle Optional fallback content handle.
     * @param nodeDataName Content node data name for binary data.
     * @param extension Optional link extension.
     * @param anchor Optional link anchor.
     * @param parameters Optional link parameters.
     * @return Link pointing to the content denoted by uuid and repository. Link is created using all provided optional values if present.
     * @throws LinkException
     */
    public static Link createLinkInstance(String uuid, String repository, String fallbackHandle, String nodeDataName, String extension, String anchor, String parameters) throws LinkException {
        final String defaultRepository = StringUtils.defaultIfEmpty(repository, RepositoryConstants.WEBSITE);
        Link link;
        try {
            link = createLinkInstance(defaultRepository, uuid);
        } catch (LinkException e) {
            try {
                final Content node = MgnlContext.getHierarchyManager(defaultRepository).getContent(fallbackHandle != null? fallbackHandle:"");
                link = createLinkInstance(node);
            } catch (PathNotFoundException pnfe) {
                log.warn("Can't find node with uuid {} or handle {} in repository {}", new Object[]{ uuid, fallbackHandle, defaultRepository});
                link = new Link();
                link.setUUID(uuid);
            } catch (RepositoryException re) {
                log.warn("Can't find node with uuid {} or handle {} in repository {}", new Object[]{ uuid, fallbackHandle, defaultRepository});
                link = new Link();
                link.setUUID(uuid);
            }
        }
        link.setFallbackHandle(fallbackHandle);
        link.setNodeDataName(nodeDataName);
        link.setExtension(extension);
        link.setAnchor(anchor);
        link.setParameters(parameters);

        return link;
    }
    
    /**
     * Parses UUID link pattern string and converts it into a Link object.
     * @param uuidLink String containing reference to content as a UUID link pattern.
     * @return Link to content referenced in the provided text.
     */
    public static Link parseUUIDLink(String uuidLink) throws LinkException{
        Matcher matcher = UUID_PATTERN.matcher(uuidLink);
        if(matcher.matches()){
            return createLinkInstance(matcher.group(1), matcher.group(2), matcher.group(5), matcher.group(7), matcher.group(8), matcher.group(10), matcher.group(12));
        }
        throw new LinkException("can't parse [ " + uuidLink + "]");
    }
    
    /**
     * Parses provided URI to the link.
     * @param link URI representing path to piece of content
     * @return Link pointing to the content represented by provided URI
     */
    public static Link parseLink(String link) throws LinkException{
        // ignore context handle if existing
        link = StringUtils.removeStart(link, MgnlContext.getContextPath());

        Matcher matcher = LINK_PATTERN.matcher(link);
        if(matcher.matches()){
            String orgHandle = matcher.group(1);
            orgHandle = Components.getComponent(I18nContentSupport.class).toRawURI(orgHandle);
            String repository = getURI2RepositoryManager().getRepository(orgHandle);
            String handle = getURI2RepositoryManager().getHandle(orgHandle);
            return createLinkInstance(repository, handle, matcher.group(3),matcher.group(5),matcher.group(7));
        }
        throw new LinkException("can't parse [ " + link + "]");
    }
    
    /**
     * Converts provided Link to an UUID link pattern.
     * @param link Link to convert.
     * @return UUID link pattern representation of provided link.
     * @throws RepositoryException 
     */
    public static String toPattern(Link link) {
        return "${link:{"
            + "uuid:{" + link.getUUID() + "},"
            + "repository:{" + link.getRepository() + "},"
            + "handle:{" + link.getHandle() + "}," // original handle represented by the uuid
            + "nodeData:{" + StringUtils.defaultString(link.getNodeDataName()) + "}," // in case of binaries
            + "extension:{" + StringUtils.defaultString(link.getExtension()) + "}" // the extension to use if no extension can be resolved otherwise
            + "}}"
            + (StringUtils.isNotEmpty(link.getAnchor())? "#" + link.getAnchor():"")
            + (StringUtils.isNotEmpty(link.getParameters())? "?" + link.getParameters() : "");
    }
    
    private static URI2RepositoryManager getURI2RepositoryManager(){
        return Components.getComponent(URI2RepositoryManager.class);
    }
}
