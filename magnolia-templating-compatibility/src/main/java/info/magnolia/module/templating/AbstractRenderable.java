/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.module.templating;

import java.lang.reflect.InvocationTargetException;

import info.magnolia.cms.core.Content;
import info.magnolia.templating.template.definition.TemplateDefinitionImpl;

/**
 * Base implementation for paragraph and template definitions. Provides the
 * {@link #modelClass} property which is used in the method
 * {@link #newModel(Content, RenderableDefinition , RenderingModel)}
 *
 * @version $Id$
 * @deprecated since 5.0, use {@link RenderableDefinitionImpl} instead.
 */
public class AbstractRenderable extends TemplateDefinitionImpl implements RenderableDefinition{

    /**
     * Return always the {@link #templateScript} property.
     */
    @Override
    public String determineTemplatePath(String actionResult, RenderingModel<?> model ) {
        return this.getTemplateScript();
    }

    /**
     * @deprecated since 5.0 - use {@link #getTemplateScript()} instead
     */
    @Override
    public String getTemplatePath() {
        return getTemplateScript();
    }

    /**
     * @deprecated since 5.0 - use {@link #getRenderType()} instead
     */
    @Override
    public String getType() {
        return getRenderType();
    }

    /**
     * @deprecated since 5.0 - use {@link #setTemplateScript(String)} instead
     */
    public void setTemplatePath(String templatePath) {
        setTemplateScript(templatePath);
    }

    /**
     * @deprecated since 5.0 - use {@link #setRenderType(String)} instead
     */
    public void setType(String type) {
        setRenderType(type);
    }

    @Override
    public RenderingModel<?> newModel(Content content, RenderableDefinition definition, RenderingModel<?> parentModel) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return (RenderingModel<?>) newModel(content.getJCRNode(), definition, parentModel);
    }
}
