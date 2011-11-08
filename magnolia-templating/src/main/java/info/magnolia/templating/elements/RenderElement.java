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
package info.magnolia.templating.elements;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.AppendableOnlyOutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;

/**
 * Renders a piece of content.
 *
 * @version $Id$
 */
public class RenderElement extends AbstractContentTemplatingElement {

    private boolean editable;
    private String template;
    private Map<String, Object> contextAttributes = new HashMap<String, Object>();
    private final RenderingEngine renderingEngine;

    public RenderElement(ServerConfiguration server, RenderingContext renderingContext, RenderingEngine renderingEngine) {
        super(server, renderingContext);
        this.renderingEngine = renderingEngine;
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {
        Node content = getTargetContent();

        // TODO not sure how to pass editable

        WebContext webContext = MgnlContext.getWebContext();
        webContext.push(webContext.getRequest(), webContext.getResponse());
        setAttributesInWebContext(contextAttributes, WebContext.LOCAL_SCOPE);
        try {
            renderingEngine.render(content, new AppendableOnlyOutputProvider(out));
        } finally {
            webContext.pop();
            webContext.setPageContext(null);
            restoreAttributesInWebContext(contextAttributes, WebContext.LOCAL_SCOPE);
        }
    }

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }


    public Map<String, Object> getContextAttributes() {
        return contextAttributes;
    }

    public void setContextAttributes(Map<String, Object> contextAttributes) {
        this.contextAttributes = contextAttributes;
    }

}
