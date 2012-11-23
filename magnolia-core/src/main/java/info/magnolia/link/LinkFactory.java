/**
 * This file Copyright (c) 2009-2012 Magnolia International
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

import java.util.regex.Pattern;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

/**
 * Factory processing various input into the Link objects and back.
 * For parsing html and converting multiple link instances on the fly use {@link LinkUtil}.
 *
 * @deprecated Since 5.0 use LinkUtil.
 */
public class LinkFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LinkFactory.class);

    /**
     * Creates new link from the content node.
     * @param node Target content for the link.
     * @return Link pointing to the provided content
     * @deprecated Since 5.0 use LinkUtil.createLinkInstance(Content) instead.
     */
    public static Link createLink(Content node) {
        return LinkUtil.createLinkInstance(node);
    }

    /**
     * Creates new link from the node data.
     * @param nodeData Target node data for the link.
     * @return Link pointing to the provided node data.
     * @deprecated Since 5.0 use LinkUtil.createLinkInstance(NodeData) instead.
     */
    public static Link createLink(NodeData nodeData) throws LinkException{
        return LinkUtil.createLinkInstance(nodeData);
    }

    /**
     * Creates link to the content denoted by repository and uuid.
     * @param repository Parent repository of the content of interest.
     * @param uuid UUID of the content to create link to.
     * @return link to the content with provided UUID.
     * @deprecated Since 5.0 use LinkUtil.createLinkInstance(String, String) instead.
     */
    public static Link createLink(String repository, String uuid) throws LinkException {
        return LinkUtil.createLinkInstance(repository, uuid);
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
     * @deprecated Since 5.0 use info.magnolia.link.LinkUtil.createLink(String, String, String, String, String)
     */
    public static Link createLink(String repository, String path, String extension, String anchor, String parameters) throws LinkException {
        return LinkUtil.createLinkInstance(repository, path, extension, anchor, parameters);
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
     * @deprecated Since 5.0 use info.magnolia.link.LinkUtil.createLinkInstance(String, String, String, String, String, String, String) instead.
     */
    public static Link createLink(String uuid, String repository, String fallbackHandle, String nodeDataName, String extension, String anchor, String parameters) throws LinkException {
        return LinkUtil.createLinkInstance(uuid, repository, fallbackHandle, nodeDataName, extension, anchor, parameters);
    }

    /**
     * Parses UUID link pattern string and converts it into a Link object.
     * @param uuidLink String containing reference to content as a UUID link pattern.
     * @return Link to content referenced in the provided text.
     * @deprecated Since 5.0 use info.magnolia.link.LinkUtil.parseUUIDLink(String) instead.
     */
    public static Link parseUUIDLink(String uuidLink) throws LinkException{
        return LinkUtil.parseUUIDLink(uuidLink);
    }

    /**
     * Parses provided URI to the link.
     * @param link URI representing path to piece of content
     * @return Link pointing to the content represented by provided URI
     * @deprecated Since 5.0 use info.magnolia.link.LinkUtil.parseLink(String) instead.
     */
    public static Link parseLink(String link) throws LinkException{
        return LinkUtil.parseLink(link);
    }

    /**
     * Converts provided Link to an UUID link pattern.
     * @param link Link to convert.
     * @return UUID link pattern representation of provided link.
     * @deprecated Since 5.0 use info.magnolia.link.LinkUtil.toPattern(Link) instead.
     */
    public static String toPattern(Link link) {
        return LinkUtil.toPattern(link);
    }

    /**
     * Pattern to find a magnolia formatted uuid link.
     * @deprecated Since 5.0.
     */
    public static Pattern UUID_PATTERN = LinkUtil.UUID_PATTERN;

    /**
     * Pattern to find a link.
     * @deprecated Since 5.0.
     */
    public static final Pattern LINK_PATTERN = LinkUtil.LINK_PATTERN;
}