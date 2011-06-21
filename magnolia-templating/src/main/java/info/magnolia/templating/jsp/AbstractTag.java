/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.jsp;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.collections.EnumerationUtils;

import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.templating.elements.TemplatingElement;

/**
 * Base class for jsp tags.
 *
 * @param <C> the templating element the tag is operating on
 * @version $Id$
 */
public abstract class AbstractTag<C extends TemplatingElement> extends SimpleTagSupport {

    @Override
    public void doTag() throws JspException, IOException {

        final TemplatingElement element = createTemplatingElement();

        try {
            element.begin(getJspContext().getOut());

            try {
                doBody();
            } finally {
                element.end(getJspContext().getOut());
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
     * Converts (split) a single comma-separated string, or returns a copy of the given collection or String array.
     */
    protected List<String> mandatoryStringList(Object attrValue, String attributeName) throws JspException {
        if (attrValue == null) {
            throw new JspException(attributeName + " is mandatory and must a comma-separated String or a Collection<String> instance. No value passed, or passed value was null.");
        }
        if (attrValue instanceof String) {
            return split((String) attrValue);
        } else if (attrValue instanceof Collection) {
            return new ArrayList<String>((Collection) attrValue);
        } else if (attrValue instanceof String[]) {
            return Arrays.asList((String[]) attrValue);
        }

        throw new JspException(attributeName + " must a comma-separated String or a Collection<String> instance. Passed value was a " + attrValue.getClass().getSimpleName() + ".");
    }

    protected List<String> split(String s) {
        if (s == null) {
            return Collections.emptyList();
        }
        final StringTokenizer st = new StringTokenizer(s, ",");
        return EnumerationUtils.toList(st);
    }
}
