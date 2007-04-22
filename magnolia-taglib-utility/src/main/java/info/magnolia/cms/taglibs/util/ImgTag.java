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

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.taglibs.BaseContentTag;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class ImgTag extends BaseContentTag {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private Map htmlAttributes = new HashMap();

    private String altNodeDataName;

    /**
     * Setter for <code>nodeDataName</code>.
     * @param nodeDataName The nodeDataName to set.
     */
    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    /**
     * Setter for <code>altNodeDataName</code>.
     * @param altNodeDataName The altNodeDataName to set.
     */
    public void setAltNodeDataName(String altNodeDataName) {
        this.altNodeDataName = altNodeDataName;
    }

    /**
     * Setter for <code>height</code>.
     * @param height html attribute
     */
    public void setHeight(String value) {
        this.htmlAttributes.put("height", value);
    }

    /**
     * Setter for <code>width</code>.
     * @param width html attribute
     */
    public void setWidth(String value) {
        this.htmlAttributes.put("width", value);
    }

    /**
     * Setter for <code>class</code>.
     * @param class html attribute
     */
    public void setClass(String value) {
        this.htmlAttributes.put("class", value);
    }

    /**
     * Setter for <code>style</code>.
     * @param style html attribute
     */
    public void setStyle(String value) {
        this.htmlAttributes.put("style", value);
    }

    /**
     * Setter for <code>id</code>.
     * @param id html attribute
     */
    public void setId(String value) {
        this.htmlAttributes.put("id", value);
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        Content contentNode = getFirstMatchingNode();
        if (contentNode == null) {
            return EVAL_PAGE;
        }

        NodeData imageNodeData = contentNode.getNodeData(this.nodeDataName);

        if (!imageNodeData.isExist()) {
            return EVAL_PAGE;
        }

        FileProperties props = new FileProperties(contentNode, this.nodeDataName);
        String imgSrc = props.getProperty(FileProperties.PATH);

        String altNodeDataNameDef = this.altNodeDataName;
        if (StringUtils.isEmpty(altNodeDataNameDef)) {
            altNodeDataNameDef = nodeDataName + "Alt";
        }

        String alt = contentNode.getNodeData(altNodeDataNameDef).getString();

        if (StringUtils.isEmpty(alt)) {
            alt = props.getProperty(FileProperties.NAME_WITHOUT_EXTENSION);
        }

        JspWriter out = pageContext.getOut();

        // don't modify the original map, remember tag pooling
        Map attributes = new HashMap(htmlAttributes);
        attributes.put("title", alt);

        if (!attributes.containsKey("width") && !attributes.containsKey("height")) {
            String width = props.getProperty(FileProperties.PROPERTY_WIDTH);
            if (StringUtils.isNotEmpty(width)) {
                attributes.put("width", width);
            }

            String height = props.getProperty(FileProperties.PROPERTY_HEIGHT);
            if (StringUtils.isNotEmpty(height)) {
                attributes.put("height", height);
            }
        }

        try {
            if (StringUtils.lowerCase(imgSrc).endsWith(".swf")) {
                // handle flash movies

                out.write("<object type=\"application/x-shockwave-flash\" data=\"");
                out.write(request.getContextPath());
                out.write(imgSrc);
                out.write("\" ");
                writeAttributes(out, attributes);
                out.write(">");

                out.write("<param name=\"movie\" value=\"");
                out.write(request.getContextPath());
                out.write(imgSrc);
                out.write("\"/>");
                out.write("</object>");

            }
            else {

                attributes.put("alt", alt);

                out.write("<img src=\"");
                out.write(request.getContextPath());
                out.write(imgSrc);
                out.write("\" ");

                writeAttributes(out, attributes);
                out.write("/>");
            }
        }
        catch (IOException e) {
            // should never happen
            throw new NestableRuntimeException(e);
        }

        return super.doEndTag();
    }

    /**
     * @param out
     * @throws IOException
     */
    private void writeAttributes(JspWriter out, Map attributes) throws IOException {
        for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String value = (String) attributes.get(name);
            out.write(name);
            out.write("=\"");
            out.write(value);
            out.write("\" ");
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        altNodeDataName = null;
        htmlAttributes.clear();
    }

}
