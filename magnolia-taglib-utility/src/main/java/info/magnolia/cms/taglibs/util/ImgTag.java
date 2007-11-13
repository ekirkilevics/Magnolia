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

        if (StringUtils.isBlank((String) attributes.get("width"))
            || StringUtils.isBlank((String) attributes.get("height"))) {
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
                out.write("<param name=\"wmode\" value=\"transparent\"/>");
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
