/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ParagraphRenderingFacade;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
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
     * file to be included (e.g. /templates/jsp/x.jsp).
     */
    private String path;

    /**
     * Attributes to be passed to the included template (set by nested Attribute tags)
     */
    private transient List attributes;

    /**
     * the instance contentNode (i.e. paragraph) you wish to show.
     */
    private transient Content contentNode;

    /**
     * the name of the contentNode (i.e. paragraph) you wish to show.
     */
    private String contentNodeName;

    /**
     * @deprecated
     * @see #setContentNode(Content)
     */
    public void setContainer(Content contentNode) {
        this.setContentNode(contentNode);
    }

    /**
     * Set the content object.
     * @param contentNode the instance contentNode (i.e. paragraph) you wish to show
     */
    public void setContentNode(Content contentNode) {
        this.contentNode = contentNode;
    }

    /**
     * Set the file to be included.
     * @param path file to be included (e.g. /templates/jsp/x.jsp)
     * @deprecated this only includes a jsp file, use the default jsp tags to do this
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * If this parameter is passed the include tag uses the defined node of the page
     * @param contentNodeName the name of the contentNode (i.e. paragraph) you wish to show
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

            if (this.path!=null) { // TODO
                log.warn("You are using the deprecated path attribute of the include tag. Your jsp will be included for now, but you might want to update your code to avoid bad surprises in the future.");
                pageContext.include(this.path);
            }

            ParagraphRenderingFacade.getInstance().render(content, pageContext.getOut(), pageContext);

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