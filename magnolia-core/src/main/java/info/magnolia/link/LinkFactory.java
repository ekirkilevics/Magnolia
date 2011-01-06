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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.context.MgnlContext;

/**
 * Factory processing various input into the Link objects and back.
 * For parsing html and converting multiple link instances on the fly use {@link LinkUtil}.
 *
 * @author had
 * @version $Id:$
 */
public class LinkFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LinkFactory.class);

    /**
     * Creates new link from the content node.
     * @param node Target content for the link.
     * @return Link pointing to the provided content.
     */
    public static Link createLink(Content node) {
        return new Link(node);
    }

    /**
     * Creates new link from the node data.
     * @param nodeData Target node data for the link.
     * @return Link pointing to the provided node data.
     */
    public static Link createLink(NodeData nodeData) throws LinkException {
        try {
            return new Link(nodeData.getHierarchyManager().getName(), nodeData.getParent(), nodeData);
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
    public static Link createLink(String repository, String uuid) throws LinkException {
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
    public static Link createLink(String repository, String path, String extension, String anchor, String parameters) throws LinkException {
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
                Class parser = Class.forName("org.apache.jackrabbit.spi.commons.conversion.PathParser");
                parser.getMethod("checkFormat", new Class[] {String.class}).invoke(null, new Object[] {path});
            } catch (Exception e) {
                if ("org.apache.jackrabbit.spi.commons.conversion.MalformedPathException".equals(e.getClass().getName())) {
                    // we first check for path incl. the file name. While file name might not be necessarily part of the path, it might contain also non ascii chars. If that is the case, parsing exception will occur so we know that path with filename can't exist.
                    exists = false;
                } else {
                    // ignore - parser doesn't exists
                }
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
     * Creates link based on provided parameters. Should the uuid be non existent or the fallback handle invalid, creates nonetheless an <em>"undefined"</em> {@link Link} object.
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
    public static Link createLink(String uuid, String repository, String fallbackHandle, String nodeDataName, String extension, String anchor, String parameters) throws LinkException {
        final String defaultRepository = StringUtils.defaultIfEmpty(repository, ContentRepository.WEBSITE);
        Link link;
        try {
            link = createLink(defaultRepository, uuid);
        } catch (LinkException e) {
            try {
                final Content node = MgnlContext.getHierarchyManager(defaultRepository).getContent(fallbackHandle != null? fallbackHandle:"");
                link = createLink(node);
            } catch (PathNotFoundException pnfe) {
                log.warn("Can't find node with uuid {} or handle {} in repository {}", new Object[]{ uuid, fallbackHandle, defaultRepository});
                link = new Link();
            } catch (RepositoryException re) {
                log.warn("Can't find node with uuid {} or handle {} in repository {}", new Object[]{ uuid, fallbackHandle, defaultRepository});
                link = new Link();
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
        Matcher matcher = LinkFactory.UUID_PATTERN.matcher(uuidLink);
        if(matcher.matches()){
            return createLink(matcher.group(1), matcher.group(2), matcher.group(5), matcher.group(7), matcher.group(8), matcher.group(10), matcher.group(12));
        }
        else{
            throw new LinkException("can't parse [ " + uuidLink + "]");
        }
    }

    /**
     * Parses provided URI to the link.
     * @param link URI representing path to piece of content
     * @return Link pointing to the content represented by provided URI
     */
    public static Link parseLink(String link) throws LinkException{
        // ignore context handle if existing
        link = StringUtils.removeStart(link, MgnlContext.getContextPath());

        Matcher matcher = LinkFactory.LINK_PATTERN.matcher(link);
        if(matcher.matches()){
            String orgHandle = matcher.group(1);
            orgHandle = I18nContentSupportFactory.getI18nSupport().toRawURI(orgHandle);
            String repository = URI2RepositoryManager.getInstance().getRepository(orgHandle);
            String handle = URI2RepositoryManager.getInstance().getHandle(orgHandle);
            return createLink(repository, handle, matcher.group(3),matcher.group(5),matcher.group(7));
        }
        else{
            throw new LinkException("can't parse [ " + link + "]");
        }
    }

    /**
     * Converts provided Link to an UUID link pattern.
     * @param link Link to convert.
     * @return UUID link pattern representation of provided link.
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
}
