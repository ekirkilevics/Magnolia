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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ParagraphRenderingFacade;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.JspWriter;

/**
 * Delegates to an appropriate ParagraphRenderer, or include a JSP.
 * This is typically used to render a paragraph. Within contentNodeIterator, parameters are provided
 * automatically by the loop.
 * @jsp.tag name="includeTemplate" body-content="JSP"
 *
 * @author marcel Salathe
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class Include extends BodyTagSupport {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Include.class);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * File to be included (e.g. /templates/jsp/x.jsp).
     * @deprecated
     */
    private String path;

    /**
     * Attributes to be passed to the included template (set by nested Attribute tags).
     */
    private transient List attributes;

    /**
     * The instance contentNode (i.e. paragraph) you wish to show.
     */
    private transient Content contentNode;

    /**
     * The name of the contentNode (i.e. paragraph) you wish to show.
     */
    private String contentNodeName;

    /**
     * @deprecated use the contentNode attribute instead
     * @see #setContentNode(Content)
     * @jsp.attribute required="false" rtexprvalue="true" type="info.magnolia.cms.core.Content"
     */
    public void setContainer(Content contentNode) {
        this.setContentNode(contentNode);
    }

    /**
     * @param contentNode the instance contentNode (i.e. paragraph) you wish to show
     * @jsp.attribute required="false" rtexprvalue="true" type="info.magnolia.cms.core.Content"
     */
    public void setContentNode(Content contentNode) {
        this.contentNode = contentNode;
    }

    /**
     * @deprecated file to be included (e.g. "/templates/jsp/x.jsp").
     * Just use basic jsp tags (i.e. <jsp:include/>) if you need to include a jsp in your templates. 
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The name of the contentNode (i.e. paragraph) you wish to show.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentNodeName(String contentNodeName) {
        this.contentNodeName = contentNodeName;
    }

    /**
     * @param name name of attribute to pass with the include
     * @param value value of attribute to pass with the include
     */
    public void setAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new ArrayList();
        }
        Object[] attributesArray = new Object[]{name, value};
        attributes.add(attributesArray);
    }

    /**
     * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
     */
    public int doAfterBody() {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        if ((attributes != null) && (attributes.size() > 0)) {
            Iterator i = attributes.iterator();
            while (i.hasNext()) {
                Object[] s = (Object[]) i.next();
                req.setAttribute((String) s[0], s[1]);
            }
        }
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        boolean localContentNodeSet = false;
        try {

            // get content
            Content content = this.contentNode;
            if (content == null) {
                // was there a node name passed
                if (this.contentNodeName != null) {
                    content = Resource.getCurrentActivePage().getContent(this.contentNodeName);
                    if (content != null) {
                        Resource.setLocalContentNode(content);
                        localContentNodeSet = true;
                    }
                }
                // use current (first local then global)
                else {
                    content = Resource.getLocalContentNode();
                    if (content == null) {
                        content = Resource.getGlobalContentNode();
                        if (content != null) {
                            Resource.setLocalContentNode(content);
                            localContentNodeSet = true;
                        }
                    }
                }
                if (content == null) {
                    throw new Exception("no content node found"); //$NON-NLS-1$
                }
            }
            
            if (content != Resource.getCurrentActivePage() && !localContentNodeSet && content != null) {
                Resource.setLocalContentNode(content);
                localContentNodeSet = true;
            }

            if (this.path != null) { // TODO
                log.warn("You are using the deprecated path attribute of the include tag. Your jsp will be included for now, but you might want to update your code to avoid bad surprises in the future.");
                pageContext.include(this.path);
            } else {
                // MAGNOLIA-2395 : wrapping pageContext.getOut() to work around a freemarker issue - see TagTransformModel
                final JspWriter out = pageContext.getOut();
                final Writer wrappedOut = new BufferedWriter(out);
                ParagraphRenderingFacade.getInstance().render(content, wrappedOut, pageContext);
                out.flush();
            }

        } catch (IOException e) {
            // should never happen
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        finally {
            // if we set the local content node we have to reset it again else we keep the node
            if(localContentNodeSet){
                Resource.removeLocalContentNode();

            }
        }

        this.removeAttributes();
        return EVAL_PAGE;
    }

    /**
     *
     */
    private void removeAttributes() {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        if ((attributes != null) && (attributes.size() > 0)) {
            Iterator i = attributes.iterator();
            while (i.hasNext()) {
                Object[] s = (Object[]) i.next();
                req.removeAttribute((String) s[0]);
            }
        }
        attributes = null;
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTagSupport#release()
     */
    public void release() {
        this.path = null;
        this.attributes = null;
        this.contentNode = null;
        super.release();
    }

}
