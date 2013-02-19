/**
 * This file Copyright (c) 2003-2012 Magnolia International
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

import info.magnolia.freemarker.FreemarkerConfig;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import freemarker.ext.util.ModelFactory;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleDate;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

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
        add(ContentMapModel.FACTORY);
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
    @Override
    public Object unwrap(TemplateModel model, Class hint) throws TemplateModelException {
        // all our models implement either AdapterTemplateModel or a already handled by super.unwrap() (boolean, string, ..)
        return super.unwrap(model, hint);
    }

    @Override
    public TemplateModel wrap(Object obj) throws TemplateModelException {

        if (obj == null) {
            return super.wrap(null);
        }

        // wrap enums
        // can't do this later, as the Class instance passed to getModelFactory() lost the information about its enum.
        if ((obj instanceof Class) && ((Class) obj).isEnum()) {
            final String enumClassName = ((Class) obj).getName();
            return getEnumModels().get(enumClassName);
        }

        // We let our own model factories have precedence over freemarker defaults, typically used to prevent classes
        // implementing Map or Collection from being wrapped as SimpleHash and SimpleSequence. We intentionally don't
        // consider model factories created in the super class method getModelFactory(Class) here because that would
        // side step default wrapping in super.wrap(Object). I.e. the model factory returned for Map is a MapModel
        // instead of SimpleHash returned by super.wrap(Object).
        Class<?> clazz = obj.getClass();
        ModelFactory modelFactory = getModelFactory(clazz, freemarkerConfig.getModelFactories());
        if (modelFactory == null) {
            modelFactory = getModelFactory(clazz, DEFAULT_MODEL_FACTORIES);
        }
        if (modelFactory != null) {
            return handleUnknownType(obj);
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
    @Override
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
    @Deprecated
    protected SimpleDate handleCalendar(Calendar cal) {
        return new SimpleDate(cal.getTime(), TemplateDateModel.DATETIME);
    }

}
