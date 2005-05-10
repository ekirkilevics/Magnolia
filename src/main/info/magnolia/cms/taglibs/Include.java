/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;


/**
 * @author marcel Salathe
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class Include extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Include.class);

    private String path;

    private transient List attributes;

    private transient Content contentNode;

    private String contentNodeName;

    /**
     * @deprecated
     */
    public void setContainer(Content contentNode) {
        this.setContentNode(contentNode);
    }

    /**
     * Set the content object.
     * @param contentNode
     */
    public void setContentNode(Content contentNode) {
        this.contentNode = contentNode;
    }

    /**
     * Set the jsp path.
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * If this parameter is passed the include tag uses the defined node of the page
     * @param contentNodeName
     */
    public void setContentNodeName(String contentNodeName) {
        this.contentNodeName = contentNodeName;
    }

    /**
     * @param name , name of attribute to pass with the include
     * @param value , value of attribute to pass with the include
     */
    public void setAttribute(String name, String value) {
        if (attributes == null) {
            attributes = new ArrayList();
        }
        String[] attributesArray = new String[]{name, value};
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
                String[] s = (String[]) i.next();
                req.setAttribute(s[0], s[1]);
            }
        }
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        try {
            HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
            // get content
            Content content = this.contentNode;
            if (content == null) {
                // was there a node name passed
                if (this.contentNodeName != null) {
                    content = Resource.getCurrentActivePage(req).getContent(this.contentNodeName);
                    if (content != null) {
                        Resource.setLocalContentNode(req, content);
                    }
                }
                // use current (first local then global)
                else {
                    content = Resource.getLocalContentNode(req);
                    if (content == null) {
                        content = Resource.getGlobalContentNode(req);
                        if (content != null) {
                            Resource.setLocalContentNode(req, content);
                        }
                    }
                }
                if (content == null) {
                    throw new Exception("no content node found");
                }
            }

            String template = this.path;
            if (template == null) {
                String templateName = content.getNodeData("paragraph").getString();
                template = Paragraph.getInfo(templateName).getTemplatePath();
            }

            pageContext.include(template);
        }
        catch (IOException e) {
            // should never happen
            throw new NestableRuntimeException(e);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            Resource.removeLocalContentNode((HttpServletRequest) pageContext.getRequest());
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
                String[] s = (String[]) i.next();
                req.removeAttribute(s[0]);
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