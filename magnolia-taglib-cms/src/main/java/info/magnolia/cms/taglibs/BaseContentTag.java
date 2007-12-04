/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class BaseContentTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private final static Logger log = LoggerFactory.getLogger(BaseContentTag.class);

    protected String nodeDataName;

    protected Content contentNode;

    protected String contentNodeName;

    protected String contentNodeCollectionName;

    protected String uuid;

    protected String path;

    protected String repository = ContentRepository.WEBSITE;

    protected boolean inherit;

    /**
     * This is historically. Meaning that we work on the page node itself even when we are in an iterator.
     */
    protected boolean actpage;

    /**
     * Set the node data name, e.g. "mainText".
     * @param name
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * Set the content node name name, e.g. "01".
     * @param name
     */
    public void setContentNodeName(String name) {
        this.contentNodeName = name;
    }

    /**
     * Set the content node collection name name, e.g. "mainColumnParagraphs".
     * @param name
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * Setter for <code>inherit</code>.
     * @param inherit <code>true</code> to inherit from parent pages if value is not set.
     */
    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    /**
     * Get the first matching node containing a NodeData named <code>nodeDataName</code>
     * @return the active node, or the first matching one if nodedata is null and inherit is set.
     * @deprecated Use {@link #getFirstMatchingNode()} instead
     */
    protected Content getFirtMatchingNode() {
        return getFirstMatchingNode();
    }

    /**
     * Get the first matching node containing a NodeData named <code>nodeDataName</code>
     * @return the active node, or the first matching one if nodedata is null and inherit is set.
     */
    protected Content getFirstMatchingNode() {
        Content currentPage = Resource.getCurrentActivePage();
        if (actpage) {
            return currentPage;
        }

        Content contentNode = null;
        if (this.getContentNode() != null) {
            contentNode = this.getContentNode();
        }
        if (StringUtils.isNotEmpty(this.getUuid())) {
            contentNode = ContentUtil.getContentByUUID(this.getRepository(), this.getUuid());
        }

        if (StringUtils.isNotEmpty(this.getPath())) {
            contentNode = ContentUtil.getContent(this.getRepository(), this.getPath());
        }

        if (contentNode == null) {
            contentNode = resolveNode(currentPage);
        }

        if (contentNode == null) {
            return null;
        }

        if (StringUtils.isNotEmpty(this.nodeDataName)) {
            NodeData nodeData = contentNode.getNodeData(this.nodeDataName);

            try {
                while (inherit && currentPage.getLevel() > 0 && !nodeData.isExist()) {
                    currentPage = currentPage.getParent();
                    contentNode = resolveNode(currentPage);
                    nodeData = contentNode.getNodeData(this.nodeDataName);
                }
            }
            catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }

        return contentNode;
    }

    protected Content resolveNode(Content currentPage) {
        Content currentParagraph = Resource.getLocalContentNode();

        try {
            if (StringUtils.isNotEmpty(contentNodeName)) {
                // contentNodeName is defined
                if (StringUtils.isEmpty(contentNodeCollectionName)) {
                    // e.g. <cms:out nodeDataName="title" contentNodeName="footer"/>
                    return currentPage.getContent(contentNodeName);
                }

                // e.g. <cms:out nodeDataName="title" contentNodeName="01" contentNodeCollectionName="mainPars"/>
                // e.g. <cms:out nodeDataName="title" contentNodeName="footer" contentNodeCollectionName=""/>
                return currentPage.getContent(contentNodeCollectionName).getContent(contentNodeName);
            }
            else {
                if (currentParagraph == null) {
                    // outside collection iterator
                    if (StringUtils.isEmpty(contentNodeCollectionName)) {
                        // e.g. <cms:out nodeDataName="title"/>
                        // e.g. <cms:out nodeDataName="title" contentNodeName=""/>
                        // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                        return currentPage;
                    }
                    else {
                        // ERROR: no content node assignable because contentNodeName is empty
                        // e.g. <cms:out nodeDataName="title" contentNodeCollectionName="mainPars"/>

                        // but in this case we return contentNodeCollection if existent
                        if (currentPage.hasContent(contentNodeCollectionName)) {
                            return currentPage.getContent(contentNodeCollectionName);
                        }
                    }
                }
                else {
                    // inside collection iterator
                    if (contentNodeName == null && contentNodeCollectionName == null) {
                        // e.g. <cms:out nodeDataName="title"/>
                        return currentParagraph;
                    }
                    else if ((contentNodeName != null && StringUtils.isEmpty(contentNodeName))
                        || (contentNodeCollectionName != null && StringUtils.isEmpty(contentNodeCollectionName))) {
                        // empty collection name -> use actpage
                        // e.g. <cms:out nodeDataName="title" contentNodeCollectionName=""/>
                        return currentPage;
                    }
                }
            }
        }
        catch (RepositoryException re) {
            if (log.isDebugEnabled()) {
                log.debug(re.getMessage());
            }
        }
        return null;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();

        this.nodeDataName = null;
        this.contentNodeName = null;
        this.contentNodeCollectionName = null;
        this.inherit = false;
    }

    public boolean isActpage() {
        return this.actpage;
    }

    public void setActpage(boolean actpage) {
        this.actpage = actpage;
    }

    public String getRepository() {
        return this.repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Content getContentNode() {
        return this.contentNode;
    }

    public void setContentNode(Content content) {
        this.contentNode = content;
    }

}
