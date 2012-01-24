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
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.templating.elements.AbstractContentTemplatingElement;
import info.magnolia.templating.elements.TemplatingElement;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import javax.jcr.Node;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.tldgen.annotations.Attribute;

/**
 * Base class for jsp tags.
 *
 * @param <C> the templating element the tag is operating on
 * @version $Id$
 */
public abstract class AbstractTag<C extends TemplatingElement> extends SimpleTagSupport {

    //Common Variable.
    private Object content = null;
    private String workspace = null;
    private String uuid = null;
    private String path = null;


    @Attribute(required=false, rtexprvalue=true)
    public void setContent(Object content) {
        this.content = content;
    }

    @Attribute(required=false, rtexprvalue=true)
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Attribute(required=false, rtexprvalue=true)
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Attribute(required=false, rtexprvalue=true)
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void doTag() throws JspException, IOException {

        final C templatingElement = createTemplatingElement();

        prepareTemplatingElement(templatingElement);

        try {
            templatingElement.begin(getJspContext().getOut());

            try {
                doBody();
            } finally {
                templatingElement.end(getJspContext().getOut());
            }
        } catch (RenderException e) {
            throw new JspException(e);
        }
    }

    protected void doBody() throws JspException, IOException {
        final JspFragment body = getJspBody();
        if (body != null) {
            body.invoke(null);
        }
    }

    /**
     * Implementations of this method should prepare the TemplatingElement with the known parameters.
     */
    protected abstract void prepareTemplatingElement(C templatingElement) throws JspException;

    /**
     * Init attributes common to all {@link AbstractContentTemplatingElement}.
     */
    protected void initContentElement(AbstractContentTemplatingElement element) {
        if(content instanceof Node) {
            element.setContent((Node)content);
        } else if (content instanceof ContentMap) {
            element.setContent(((ContentMap)content).getJCRNode());
        }
        element.setWorkspace(workspace);
        element.setNodeIdentifier(uuid);
        element.setPath(path);
    }



    protected C createTemplatingElement() {

        // FIXME use scope instead of fetching the RenderingContext for passing it as an argument
        final RenderingEngine renderingEngine = Components.getComponent(RenderingEngine.class);
        final RenderingContext renderingContext = renderingEngine.getRenderingContext();

        return Components.getComponentProvider().newInstance(getTemplatingElementClass(), renderingContext);
    }

    protected Class<C> getTemplatingElementClass() {
        // TODO does this support more than one level of subclasses?
        return (Class<C>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }


    // ---- utility methods to convert parameters ----

    /**
     * Currently only support Map attrValue.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> mapConvertor(Object attrValue, String attributeName, boolean isMandatory) throws JspException {

        if (isMandatory && attrValue == null) {
            throw new JspException(attributeName + " is mandatory and must be a Map. No value passed, or passed value was null.");
        }
        if (attrValue != null) {
            if(attrValue instanceof Map<?, ?>) {
                return (Map<String, Object>) attrValue;
            }
            throw new JspException(attributeName + " must be a Map. Passed value was a " + attrValue.getClass().getSimpleName() + ".");
        }
        return (Map<String, Object>)attrValue;
    }

    /**
     * Currently only support Map attrValue.
     */
    protected Map<String, Object> mandatoryMapConvertor(Object attrValue, String attributeName) throws JspException {
       return mapConvertor(attrValue, attributeName, true);
    }
}
