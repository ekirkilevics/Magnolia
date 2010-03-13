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
package info.magnolia.freemarker;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import info.magnolia.freemarker.models.MagnoliaModelFactory;
import info.magnolia.freemarker.models.MagnoliaObjectWrapper;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Observed bean holding Freemarker configuration. Not to be confused with
 * Freemarker's own {@link freemarker.template.Configuration}. This only exposes the few
 * properties that Magnolia allows to configure and is able to handle properly.
 * It also provides a few additional methods used internally.
 *
 * @see info.magnolia.freemarker.FreemarkerHelper
 * @see info.magnolia.freemarker.models.MagnoliaObjectWrapper
 * @see info.magnolia.freemarker.models.MagnoliaModelFactory
 */
public class FreemarkerConfig {

    /**
     * @deprecated since 4.3 should not be needed - components using this can keep their instance
     */
    public static FreemarkerConfig getInstance() {
        return Components.getSingleton(FreemarkerConfig.class);
    }

    /**
     * The MagnoliaModelFactory implementations explicitly registered by modules.
     */
    private final List<MagnoliaModelFactory> registeredModelFactories;
    private final List<TemplateLoader> templateLoaders;
    private final Map<String, TemplateModel> sharedVariables;

    private MagnoliaObjectWrapper objectWrapper;
    private TemplateLoader multiTL;

    public FreemarkerConfig() {
        this.registeredModelFactories = new ArrayList<MagnoliaModelFactory>();
        this.templateLoaders = new ArrayList<TemplateLoader>();
        this.sharedVariables = new HashMap<String, TemplateModel>();
    }

    public ObjectWrapper getObjectWrapper() {
        if (objectWrapper == null) {
            objectWrapper = Components.getComponentProvider().newInstance(MagnoliaObjectWrapper.class);
        }
        return objectWrapper;
    }

    public TemplateLoader getTemplateLoader() {
        if (multiTL == null) {
            // using getTemplateLoaders() instead of the variable to make sure we go to the proxied object!?
            final List<TemplateLoader> loaders = getTemplateLoaders();
            final int s = loaders.size();
            // add a ClassTemplateLoader as our last loader
            final TemplateLoader[] tl = loaders.toArray(new TemplateLoader[s + 1]);
            tl[s] = new ClassTemplateLoader(getClass(), "/");
            multiTL = new MultiTemplateLoader(tl);
        }
        return multiTL;
    }

    public List<MagnoliaModelFactory> getModelFactories() {
        return registeredModelFactories;
    }

    public void addModelFactory(MagnoliaModelFactory modelFactory) {
        this.registeredModelFactories.add(modelFactory);
    }

    public List<TemplateLoader> getTemplateLoaders() {
        return templateLoaders;
    }

    public void addTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoaders.add(templateLoader);
    }

    public Map<String, TemplateModel> getSharedVariables() {
        return sharedVariables;
    }

    public void addSharedVariable(String name, Object value) throws TemplateModelException {
        addSharedVariable(name, getObjectWrapper().wrap(value));
    }

    public void addSharedVariable(String name, TemplateModel value) {
        sharedVariables.put(name, value);
    }
}
