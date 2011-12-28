/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.samples.model;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.link.LinkUtil;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

/**
 * Resolving internal links, either identifier or path link.
 *
 * @version $Id$
 */
public class InternalLinkModel extends RenderingModelImpl<RenderableDefinition> {


    public InternalLinkModel(Node content, RenderableDefinition definition, RenderingModel<?> parent) {
        super(content, definition, parent);
    }

    public ContentMap getTarget() throws ValueFormatException, PathNotFoundException, RepositoryException{
        return new LinkItem(content, "target").getNode();
    }

    public String getTargetLink() throws ValueFormatException, PathNotFoundException, RepositoryException{
        return new LinkItem(content, "target").getLink();
    }

    /**
     * Represents a Link.
     */
    public class LinkItem {

        private ContentMap targetContentMap = null;
        private Node targetNode = null;
        private String targetLink = null;

        //TODO cringele: should be refactored when LinkUtil returns Node (SCRUM-242)
        public LinkItem(Node componentNode, String propertyName) throws ValueFormatException, PathNotFoundException, RepositoryException{
            if(componentNode.hasProperty(propertyName)){
                String targetValue = componentNode.getProperty(propertyName).getString();
                Session session = MgnlContext.getJCRSession(ContentRepository.WEBSITE);

                //Link by path
                if(targetValue.startsWith("/")){
                    targetNode = SessionUtil.getNode(ContentRepository.WEBSITE, targetValue);
                }
                else {
                    //Link by identifier
                    targetNode = session.getNodeByIdentifier(targetValue);
                }
                if(targetNode != null) {
                    targetContentMap = new ContentMap(targetNode);
                    targetLink = LinkUtil.createLink(new DefaultContent(targetNode));
                }
            }

        }

        public String getLink() {
            return targetLink;
        }

        public ContentMap getNode() {
            return targetContentMap;
        }
    }

}
