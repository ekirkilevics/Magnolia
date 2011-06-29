/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.templating.functions;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.link.LinkUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an object exposing a couple of methods useful for templates; it's exposed in templates as "cmsfn".
 *
 * @version $Id$
 */
public class TemplatingFunctions {

    private static final Logger log = LoggerFactory.getLogger(TemplatingFunctions.class);


    //TODO cringele : test missing
    //TODO cringele : check with PH, if it can be done without using deprecated objects
    public Content asContent(Node node) throws RepositoryException {
        return node == null ? null : new DefaultContent(node, null);
    }

    public Node asJCRNode(Content content) {
        return content == null ? null : content.getJCRNode();
    }

    public Node asJCRNode(ContentMap contentMap) {
        return contentMap == null ? null : contentMap.getJCRNode();
    }

    public Node parent(Node content) throws RepositoryException {
        return content == null ? null : content.getParent();
    }

    public ContentMap parent(ContentMap contentMap) throws RepositoryException {
        if(contentMap == null) {
            return null;
        }
        Node parentContent = this.parent(contentMap.getJCRNode());
        return new ContentMap(parentContent);
    }

    //TODO cringele : test missing
    public String uuid(Node content) throws RepositoryException {
        return content == null ? null : content.getIdentifier();
    }

    //TODO cringele : test missing
    public String uuid(ContentMap contentMap) throws RepositoryException {
        return contentMap == null ? null : this.uuid(asJCRNode(contentMap));
    }

    //TODO cringele : test missing
    public String link(Node content) throws RepositoryException{
        if(content == null) {
            return null;
        }
        //TODO cringele : LinkUtil should accept Node and not Content
        Content asContent = this.asContent(content);
        return LinkUtil.createLink(asContent);
    }

    //TODO cringele : test missing
    public String link(ContentMap contentMap) throws RepositoryException{
        return contentMap == null ? null : this.link(asJCRNode(contentMap));
    }

    //TODO cringele : test missing
    public String linkExteral(Node content) throws RepositoryException{
        if(content == null) {
            return null;
        }
        //TODO cringele : LinkUtil should accept Node and not Content
        Content asContent = this.asContent(content);
        return LinkUtil.createExternalLink(asContent);
    }

    //TODO cringele : test missing
    public String linkExternal(ContentMap contentMap) throws RepositoryException{
        return contentMap == null ? null : this.linkExteral(asJCRNode(contentMap));
    }

    //TODO cringele : test missing
    public String linkAbsolute(Node content) throws RepositoryException{
        if(content == null) {
            return null;
        }
        //TODO cringele : LinkUtil should accept Node and not Content
        Content asContent = this.asContent(content);
        return LinkUtil.createAbsoluteLink(asContent);
    }

    //TODO cringele : test missing
    public String linkAbsolute(ContentMap contentMap) throws RepositoryException{
        return contentMap == null ? null : this.linkAbsolute(asJCRNode(contentMap));
    }

}
