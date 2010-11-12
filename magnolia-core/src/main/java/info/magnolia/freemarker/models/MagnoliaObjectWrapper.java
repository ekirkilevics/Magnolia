/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.freemarker.models;

import freemarker.ext.util.ModelFactory;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleDate;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.context.Context;
import info.magnolia.freemarker.FreemarkerConfig;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A FreeMarker ObjectWrapper that knows about Magnolia specific objects.
 *
 * @author Chris Miner
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MagnoliaObjectWrapper extends DefaultObjectWrapper {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MagnoliaObjectWrapper.class);

    private final static List<MagnoliaModelFactory> DEFAULT_MODEL_FACTORIES = new ArrayList<MagnoliaModelFactory>() {{
        add(NodeDataModelFactory.INSTANCE);
        add(ContentModel.FACTORY);
        add(CalendarModel.FACTORY);
        add(UserModel.FACTORY);
        add(ContextModelFactory.INSTANCE);
    }};

    private final FreemarkerConfig freemarkerConfig;

    public MagnoliaObjectWrapper(FreemarkerConfig freemarkerConfig) {
        super();
        this.freemarkerConfig = freemarkerConfig;
    }

    /**
     * Unwraps our custom wrappers, let the default wrapper do the rest.
     */
    public Object unwrap(TemplateModel model, Class hint) throws TemplateModelException {
        // all our models implement either AdapterTemplateModel or a already handled by super.unwrap() (boolean, string, ..)
        return super.unwrap(model, hint);
    }

    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof Context) {
            // bypass the default SimpleHash wrapping, we need a MapModel.
            // handleUnknownType() will relay to our ContextModelFactory.
            return handleUnknownType(obj);
        }

        // wrap enums
        // can't do this later, as the Class instance passed to getModelFactory() lost the information about its enum.
        if (obj != null && (obj instanceof Class) && ((Class) obj).isEnum()) {
            final String enumClassName = ((Class) obj).getName();
            return getEnumModels().get(enumClassName);
        }

        return super.wrap(obj);
    }

    /**
     * Checks the ModelFactory instances registered in FreemarkerConfig, then
     * the default ones. If no appropriate ModelFactory was found, delegates to
     * Freemarker's implementation. This is called by {@link freemarker.ext.beans.BeansModelCache},
     * which is itself called by {@link freemarker.ext.beans.BeansWrapper#wrap}.
     * These factories are cached by Freemarker, so this method only gets called
     * once per type of object.
     *
     * @see #DEFAULT_MODEL_FACTORIES
     * @see info.magnolia.freemarker.FreemarkerConfig
     */
    protected ModelFactory getModelFactory(Class clazz) {
        final List<MagnoliaModelFactory> registeredModelFactories = freemarkerConfig.getModelFactories();
        ModelFactory modelFactory = getModelFactory(clazz, registeredModelFactories);
        
        if (modelFactory == null) {
            modelFactory = getModelFactory(clazz, DEFAULT_MODEL_FACTORIES);
        }
        if (modelFactory == null) {
            modelFactory = super.getModelFactory(clazz);
        }
        return modelFactory;
    }

    private ModelFactory getModelFactory(Class clazz, List<MagnoliaModelFactory> factories) {
        for (MagnoliaModelFactory factory : factories) {
            if (factory.factoryFor().isAssignableFrom(clazz)) {
                return factory;
            }
        }
        return null;
    }

    /**
     * Exposes a Calendar as a SimpleDate.
     * @deprecated since 4.3 use CalendarModel instead.
     */
    protected SimpleDate handleCalendar(Calendar cal) {
        return new SimpleDate(cal.getTime(), TemplateDateModel.DATETIME);
    }

}
