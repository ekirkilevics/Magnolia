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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import javax.jcr.PropertyType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.apache.log4j.Logger;


/**
 * @author Marcel Salathe
 * @version $Revision: $ ($Author: $)
 */
public class AHref extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(AHref.class);

    private String preHref = "";

    private String postHref = "";

    private String level = "0";

    private String templateName = "";

    private String nodeDataName;

    private ContentNode contentNode;

    private NodeData nodeData;

    private ArrayList attributes;

    private HttpServletRequest req;

    /**
     * <p>
     * end of tag
     * </p>
     * @return int
     */
    public int doEndTag() {
        req = (HttpServletRequest) pageContext.getRequest();
        if (this.templateName.equals("")) {
            if (this.nodeDataName == null) {
                this.writeLink("");
                return EVAL_BODY_BUFFERED;
            }
            this.contentNode = Resource.getLocalContentNode(req);
            if (this.contentNode == null) {
                this.contentNode = Resource.getGlobalContentNode(req);
                if (this.contentNode == null) {
                    this.writeLink("");
                    return EVAL_BODY_BUFFERED;
                }
            }
            try {
                this.nodeData = this.contentNode.getNodeData(this.nodeDataName);
            }
            catch (AccessDeniedException e) {
                log.error(e.getMessage(), e);
            }
            if ((this.nodeData == null) || !this.nodeData.isExist()) {
                this.writeLink("");
                return EVAL_BODY_BUFFERED;
            }
            int type = this.nodeData.getType();
            if (type == PropertyType.STRING) {
                if (this.nodeData.getString().equals(""))
                    this.writeLink("");
                else
                    this.writeLink(this.nodeData.getString());
            }
        }
        else {
            int digree = new Integer(this.level).intValue();
            Content startPage;
            try {
                startPage = Resource.getCurrentActivePage(req).getAncestor(digree);
                HierarchyManager hm = Resource.getHierarchyManager(req);
                Content resultPage = hm.getPage(startPage.getHandle(), this.templateName);
                this.writeLink(resultPage.getHandle());
            }
            catch (Exception e) {
                log.error(e.getMessage());
                this.writeLink("");
            }
        }
        return EVAL_BODY_BUFFERED;
    }

    /**
     * <p>
     * </p>
     */
    private void writeLink(String path) {
        JspWriter out = pageContext.getOut();
        try {
            if (!path.equals("")) {
                if (Resource.getHierarchyManager(req).isPage(path)) {
                    path += "." + Server.getDefaultExtension();
                }
                out.print("<a href=\"");
                out.print(this.preHref + path + this.postHref);
                out.print("\"");
                if ((attributes != null) && (attributes.size() > 0)) {
                    Iterator i = attributes.iterator();
                    while (i.hasNext()) {
                        String[] s = (String[]) i.next();
                        out.print(" " + s[0] + "=\"" + s[1] + "\"");
                    }
                }
                out.print(">");
            }
            out.print(getBodyContent().getString());
            if (!path.equals("")) {
                out.print("</a>");
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        attributes = null;
    }

    /**
     * @deprecated
     */
    public void setAtomName(String name) {
        this.setNodeDataName(name);
    }

    /**
     * @param name , antom name to evaluate
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

    /**
     * @param preHref , href part that is added before the nodeData content
     */
    public void setPreHref(String preHref) {
        this.preHref = preHref;
    }

    /**
     * @param postHref , href part that is added after the nodeData content
     */
    public void setPostHref(String postHref) {
        this.postHref = postHref;
    }

    /**
     * @param templateName , template name to search for
     */
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * @param level , level from where to start the template search
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * @param name , name of attribute to add to the a element
     * @param value , value of attribute to add to the a element
     */
    public void setAttribute(String name, String value) {
        if (attributes == null)
            attributes = new ArrayList();
        String[] attributeArray = new String[]{name, value};
        attributes.add(attributeArray);
    }
}
