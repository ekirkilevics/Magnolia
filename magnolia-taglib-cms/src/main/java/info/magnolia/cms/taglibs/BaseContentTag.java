/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import javax.jcr.RepositoryException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public abstract class BaseContentTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private final static Logger log = LoggerFactory.getLogger(BaseContentTag.class);

    private String nodeDataName;

    private Content contentNode;

    private String contentNodeName;

    private String contentNodeCollectionName;

    private String uuid;

    private String path;

    private String repository = ContentRepository.WEBSITE;

    private boolean inherit;

    /**
     * This is historically. Meaning that we work on the page node itself even when we are in an iterator.
     */
    private boolean actpage;

    /**
     * Set the node data name, e.g. "mainText".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * Inside a "contentNodeIterator": if not set, the current content node is taken. If set empty
     * (contentNodeName=""), the top level content is taken. If specified, the named content node is taken. Outside
     * a "contentNodeIterator": if not set or empty: the top level content is taken. If specified, the named content
     * node is taken.
     *
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentNodeName(String name) {
        this.contentNodeName = name;
    }

    /**
     * Name of the collection holding the content node, e.g. "mainColumnParagraphs".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * Inherit the value from parent pages, if not set in the current one.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    /**
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
        if (actpage) {
            return getCurrentPage();
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
            Content currentPage = getCurrentPage();
            contentNode = resolveNode(currentPage);

            try {
                while (inherit && currentPage.getLevel() > 0 && contentNode == null) {
                    currentPage = currentPage.getParent();
                    contentNode = resolveNode(currentPage);
                }
            }
            catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }

        if (contentNode == null) {
            return null;
        }

        if (StringUtils.isNotEmpty(this.nodeDataName)) {
            Content currentPage = getCurrentPage();
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

    protected Content getCurrentPage() {
        return Resource.getCurrentActivePage();
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
                    else if (contentNodeCollectionName != null && !StringUtils.isEmpty(contentNodeCollectionName)) {
                        return currentParagraph.getContent(contentNodeCollectionName);
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
        this.contentNode = null;
        this.inherit = false;
        this.actpage = false;
        this.uuid = null;
        this.path = null;
        this.repository = ContentRepository.WEBSITE;
    }

    public boolean isActpage() {
        return this.actpage;
    }

    /**
     * If true we work on the current active page instead of any other node.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     * @deprecated
     */
    public void setActpage(boolean actpage) {
        this.actpage = actpage;
    }

    public String getRepository() {
        return this.repository;
    }

    /**
     * Used if the uuid or path attribute is set. Defaults to "website".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getUuid() {
        return this.uuid;
    }

    /**
     * The uuid to use for finding the content.
     * You must define the repository attribute if the content is not stored in the website repository.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPath() {
        return this.path;
    }

    /**
     * The absolute path to the content.
     * You must define the repository attribute if the content is not stored in the website repository.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setPath(String path) {
        this.path = path;
    }

    public Content getContentNode() {
        return this.contentNode;
    }

    /**
     * The content object to use.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentNode(Content content) {
        this.contentNode = content;
    }

    protected String getNodeDataName() {
        return nodeDataName;
    }

    protected String getContentNodeName() {
        return contentNodeName;
    }

    protected String getContentNodeCollectionName() {
        return contentNodeCollectionName;
    }
}
