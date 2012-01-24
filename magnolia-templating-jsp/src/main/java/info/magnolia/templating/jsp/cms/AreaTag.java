/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.templating.jsp.cms;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.templating.elements.AreaElement;

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;


/**
 * A jsp tag for rendering an arbitrary piece of content.
 *
 * @version $Id$
 *
 */
@Tag(name="area", bodyContent=BodyContent.EMPTY)

public class AreaTag extends AbstractTag<AreaElement> {

    // Edit Tag Variable
    private Object area;
    private String name;
    private String components;
    private String dialog;
    private String type;
    private Object contextAttributes;

    @Attribute(required=false, rtexprvalue=true)
    public void setArea(Object area) {
        this.area = area;
    }

    @Attribute(required=true, rtexprvalue=true)
    public void setName(String name) {
        this.name = name;
    }

    @Attribute(required=false, rtexprvalue=true)     /**
     * @Attribute required="false" rtexprvalue="true"
     */
    public void setComponents(String components) {
        this.components = components;
    }

    @Attribute(required=false, rtexprvalue=true)
    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    @Attribute(required=false, rtexprvalue=true)
    public void setType(String type) {
        this.type = type;
    }

    @Attribute(required=false, rtexprvalue=true)
    public void setContextAttributes(Object contextAttributes) {
        this.contextAttributes = contextAttributes;
    }

    @Override
    protected void prepareTemplatingElement(AreaElement templatingElement) throws JspException{
        initContentElement(templatingElement);

        Map<String,Object> contextAttributes = mapConvertor(this.contextAttributes, "contextAttributes",false);
        templatingElement.setArea((AreaDefinition)area);
        templatingElement.setName(name);
        templatingElement.setAvailableComponents(components);
        templatingElement.setDialog(dialog);
        templatingElement.setType(type);
        templatingElement.setContextAttributes(contextAttributes);
    }

}
