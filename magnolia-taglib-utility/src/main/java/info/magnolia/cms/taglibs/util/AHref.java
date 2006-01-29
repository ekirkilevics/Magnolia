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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility tag which can be used to print out a link based on the value of a node data.
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
     * @deprecated nodeDataName
     */
    public void setAtomName(String name) {
        this.setNodeDataName(name);
    }

    /**
     * Setter for the <code>nodeDataName</code> tag attribute.
     * @param name name of nodeData to evaluate
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * Setter for the <code>preHref</code> tag attribute.
     * @param preHref href part that is added before the nodeData content
     */
    public void setPreHref(String preHref) {
        this.preHref = preHref;
    }

    /**
     * Setter for the <code>postHref</code> tag attribute.
     * @param postHref href part that is added after the nodeData content
     */
    public void setPostHref(String postHref) {
        this.postHref = postHref;
    }

    /**
     * Setter for the <code>templateName</code> tag attribute.
     * @param templateName template name to search for
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Setter for the <code>level</code> tag attribute.
     * @param level level from where to start the template search
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
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        if (StringUtils.isEmpty(this.templateName)) {
            if (this.nodeDataName == null) {
                this.writeLink(StringUtils.EMPTY);
                return EVAL_BODY_BUFFERED;
            }
            Content contentNode = Resource.getLocalContentNode(req);
            if (contentNode == null) {
                contentNode = Resource.getGlobalContentNode(req);
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
                startPage = Resource.getCurrentActivePage(req).getAncestor(this.level);
                HierarchyManager hm = SessionAccessControl.getHierarchyManager(req);
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
                if (SessionAccessControl
                    .getHierarchyManager((HttpServletRequest) pageContext.getRequest())
                    .isPage(path)) {
                    out.print("."); //$NON-NLS-1$
                    out.print(Server.getDefaultExtension());
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
