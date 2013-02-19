/**
 * This file Copyright (c) 2013 Magnolia International
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

import java.util.HashMap;
import java.util.Map;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.MapModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import info.magnolia.freemarker.FreemarkerConfig;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;

/**
 * Test case for {@link MagnoliaObjectWrapper}.
 */
public class MagnoliaObjectWrapperTest {

    public static class CustomMap extends HashMap {
    }

    public static class CustomMapModel extends MapModel {
        public CustomMapModel(Map map, BeansWrapper wrapper) {
            super(map, wrapper);
        }
    }

    public static class CustomMapModelFactory implements MagnoliaModelFactory {

        @Override
        public Class factoryFor() {
            return CustomMap.class;
        }

        @Override
        public TemplateModel create(Object object, ObjectWrapper wrapper) {
            return new CustomMapModel((Map) object, (MagnoliaObjectWrapper) wrapper);
        }
    }

    @Test
    public void testModelFactoryHasPrecedence() throws TemplateModelException {

        FreemarkerConfig freemarkerConfig = new FreemarkerConfig();
        freemarkerConfig.getModelFactories().add(new CustomMapModelFactory());
        MagnoliaObjectWrapper objectWrapper = new MagnoliaObjectWrapper(freemarkerConfig);

        TemplateModel templateModel = objectWrapper.wrap(new CustomMap());

        assertTrue(templateModel instanceof CustomMapModel);
    }

    @Test
    public void testWrapsContentMapWithContentMapModel() throws TemplateModelException {

        FreemarkerConfig freemarkerConfig = new FreemarkerConfig();
        MagnoliaObjectWrapper objectWrapper = new MagnoliaObjectWrapper(freemarkerConfig);

        TemplateModel templateModel = objectWrapper.wrap(new ContentMap(new MockNode()));

        assertTrue(templateModel instanceof ContentMapModel);
    }

    @Test
    public void testWrapsContextWithMapModel() throws TemplateModelException {

        FreemarkerConfig freemarkerConfig = new FreemarkerConfig();
        MagnoliaObjectWrapper objectWrapper = new MagnoliaObjectWrapper(freemarkerConfig);

        TemplateModel templateModel = objectWrapper.wrap(new MockContext());

        assertTrue(templateModel instanceof MapModel);
    }

    private static class SimpleBean {

    }

    @Test
    public void testWrapsBeanAsBeanModel() throws TemplateModelException {

        FreemarkerConfig freemarkerConfig = new FreemarkerConfig();
        MagnoliaObjectWrapper objectWrapper = new MagnoliaObjectWrapper(freemarkerConfig);

        TemplateModel templateModel = objectWrapper.wrap(new SimpleBean());

        assertTrue(templateModel instanceof BeanModel);
    }

    @Test
    public void testWrapsMapAsSimpleHash() throws TemplateModelException {

        FreemarkerConfig freemarkerConfig = new FreemarkerConfig();
        MagnoliaObjectWrapper objectWrapper = new MagnoliaObjectWrapper(freemarkerConfig);

        TemplateModel templateModel = objectWrapper.wrap(new HashMap());

        assertTrue(templateModel instanceof SimpleHash);
    }
}
