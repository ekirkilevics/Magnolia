/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.templating.freemarker;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.cms.beans.config.RenderableDefinition;
import info.magnolia.freemarker.models.MagnoliaModelFactory;

/**
 * Make parameters directly available (as if they were properties of the definition itself).
 * i.e, if the RenderableDefinitionModel doesn't have a 'foo' bean-property, one can still do
 * ${def.foo} or ${def['foo']}, which would be the equivalent of ${def.parameters.foo}
 *
 * @author pbracher
 * @version $Id$
 */
public class RenderableDefinitionModel extends BeanModel {
    public static final class Factory implements MagnoliaModelFactory {
        public Class factoryFor() {
            return RenderableDefinition.class;
        }

        public TemplateModel create(Object object, ObjectWrapper wrapper) {
            // make parameters directly available (as if they were properties of the definition itself)
            return new RenderableDefinitionModel((RenderableDefinition) object, (BeansWrapper) wrapper);
        }
    }

    /**
     * The hash model for the parameters.
     */
    private final SimpleHash paramHash;

    RenderableDefinitionModel(RenderableDefinition definition, BeansWrapper wrapper) {
        super(definition, wrapper);
        paramHash = new SimpleHash(definition.getParameters(), wrapper);
    }

    /**
     * Fall back on the parameters' hash model if no bean properry has been found.
     */
    public TemplateModel get(String key) throws TemplateModelException {
        TemplateModel templateModel = super.get(key);
        if (templateModel == null) {
            templateModel = paramHash.get(key);
        }
        return templateModel;
    }
}
