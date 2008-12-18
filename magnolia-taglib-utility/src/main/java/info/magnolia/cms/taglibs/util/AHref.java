/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.taglibs.Resource;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility tag which can be used to print out a link based on the value of a node data or tries to find the first page
 * with a specific template name, starting from a specific page.
 * @jsp.tag name="aHref" body-content="JSP"
 * @jsp.tag-example <cmsu:aHref ... />
 *
 * @author Marcel Salathe
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class AHref extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(AHref.class);

    /**
     * href part that is added before the nodeData content.
     */
    private String preHref;

    /**
     * href part that is added after the nodeData content.
     */
    private String postHref;

    /**
     * level from where to start the template search.
     */
    private int level;

    /**
     * template name to search for.
     */
    private String templateName;

    /**
     * name of nodeData to evaluate.
     */
    private String nodeDataName;

    /**
     * link attributes, added using child tags.
     */
    private transient List attributes;

    /**
     * @param name name of nodeData to evaluate
     * @deprecated use the nodeDataName attribute instead.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setAtomName(String name) {
        this.setNodeDataName(name);
    }

    /**
     * node containing the link information
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * href part that is added before the nodeData content.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setPreHref(String preHref) {
        this.preHref = preHref;
    }

    /**
     * href part that is added after the nodeData content.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setPostHref(String postHref) {
        this.postHref = postHref;
    }

    /**
     * template name to search for.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * level from where to start the template search.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Adds a link parameter.
     * @param name name of attribute to add to the a element
     * @param value value of attribute to add to the a element
     */
    public void setAttribute(String name, String value) {
        if (attributes == null) {
            attributes = new ArrayList();
        }
        String[] attributeArray = new String[]{name, value};
        attributes.add(attributeArray);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        if (StringUtils.isEmpty(this.templateName)) {
            if (this.nodeDataName == null) {
                this.writeLink(StringUtils.EMPTY);
                return EVAL_BODY_BUFFERED;
            }
            Content contentNode = Resource.getLocalContentNode();
            if (contentNode == null) {
                contentNode = Resource.getGlobalContentNode();
                if (contentNode == null) {
                    this.writeLink(StringUtils.EMPTY);
                    return EVAL_BODY_BUFFERED;
                }
            }

            NodeData nodeData = contentNode.getNodeData(this.nodeDataName);

            if ((nodeData == null) || !nodeData.isExist()) {
                this.writeLink(StringUtils.EMPTY);
                return EVAL_BODY_BUFFERED;
            }
            int type = nodeData.getType();
            if (type == PropertyType.STRING) {
                if (StringUtils.isEmpty(nodeData.getString())) {
                    this.writeLink(StringUtils.EMPTY);
                }
                else {
                    this.writeLink(nodeData.getString());
                }
            }
        }
        else {
            Content startPage;
            try {
                startPage = Resource.getCurrentActivePage().getAncestor(this.level);
                HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
                Content resultPage = hm.getPage(startPage.getHandle(), this.templateName);
                this.writeLink(resultPage.getHandle());
            }
            catch (RepositoryException e) {
                log.error(e.getMessage());
                this.writeLink(StringUtils.EMPTY);
            }
        }
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Write a link.
     * @param path link path
     */
    private void writeLink(String path) {
        JspWriter out = pageContext.getOut();
        try {
            if (StringUtils.isNotEmpty(path)) {

                out.print("<a href=\""); //$NON-NLS-1$
                if (this.preHref != null) {
                    out.print(this.preHref);
                }
                out.print(path);
                if (MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).isPage(path)) {
                    out.print("."); //$NON-NLS-1$
                    out.print(ServerConfiguration.getInstance().getDefaultExtension());
                }
                if (this.postHref != null) {
                    out.print(this.postHref);
                }
                out.print("\""); //$NON-NLS-1$
                if ((attributes != null) && (attributes.size() > 0)) {
                    Iterator i = attributes.iterator();
                    while (i.hasNext()) {
                        String[] s = (String[]) i.next();
                        out.print(" "); //$NON-NLS-1$
                        out.print(s[0]);
                        out.print("=\""); //$NON-NLS-1$
                        out.print(s[1]);
                        out.print("\""); //$NON-NLS-1$
                    }
                }
                out.print(">"); //$NON-NLS-1$
            }
            out.print(getBodyContent().getString());
            if (StringUtils.isNotEmpty(path)) {
                out.print("</a>"); //$NON-NLS-1$
            }
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
        attributes = null;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#release()
     */
    public void release() {
        this.preHref = null;
        this.postHref = null;
        this.level = 0;
        this.templateName = null;
        this.nodeDataName = null;
        this.attributes = null;
        super.release();
    }

}
