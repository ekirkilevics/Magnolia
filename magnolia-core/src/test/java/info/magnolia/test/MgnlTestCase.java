/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.test;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.PropertiesInitializer;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.TypeMapping;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.content2bean.impl.TypeMappingImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.DependencyCheckerImpl;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author philipp
 * @version $Id$
 */
public abstract class MgnlTestCase extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        // ignore mapping warnings
        org.apache.log4j.Logger.getLogger(ContentRepository.class).setLevel(org.apache.log4j.Level.ERROR);

        ComponentsTestUtil.clear();
        setMagnoliaProperties();
        initDefaultImplementations();
        initContext();
    }

    protected void initContext() {
        MockUtil.initMockContext();
    }

    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    protected void setMagnoliaProperties() throws Exception {
        setMagnoliaProperties(getMagnoliaPropertiesStream());
    }

    protected void setMagnoliaProperties(InputStream propertiesStream) throws IOException {
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties(propertiesStream));
    }

    protected InputStream getMagnoliaPropertiesStream() throws IOException {
        return this.getClass().getResourceAsStream(getMagnoliaPropertiesFileName());
    }

    protected String getMagnoliaPropertiesFileName() {
        return "/test-magnolia.properties";
    }

    protected void initDefaultImplementations() throws IOException, ModuleManagementException {
        final List<ModuleDefinition> modules = getModuleDefinitionsForTests();
        final ModuleRegistryImpl mr = new ModuleRegistryImpl();
        final ModuleManagerImpl mm = new ModuleManagerImpl(null, new FixedModuleDefinitionReader(modules), mr, new DependencyCheckerImpl());
        mm.loadDefinitions();
        final PropertiesInitializer pi = new PropertiesInitializer(mr);
        pi.loadBeanProperties();
        pi.loadAllModuleProperties();

        /*
        final TestMagnoliaConfigurationProperties configurationProperties = new TestMagnoliaConfigurationProperties(
                new ModulePropertiesSource(mr),
                new ClasspathPropertySource("/test-magnolia.properties"),
                new InitPathsPropertySource(new TestMagnoliaInitPaths())
        );
        SystemProperty.setMagnoliaConfigurationProperties(configurationProperties);
        */

        // these are not in mgnl-beans.properties anymore at the moment, they are registered via info.magnolia.cms.servlets.MgnlServletContextListener#populateRootContainer
        final TypeMappingImpl typeMapping = new TypeMappingImpl();
        ComponentsTestUtil.setInstance(TypeMapping.class, typeMapping);
        ComponentsTestUtil.setInstance(Content2BeanProcessor.class, new Content2BeanProcessorImpl(typeMapping));
        ComponentsTestUtil.setImplementation(info.magnolia.content2bean.Content2BeanTransformer.class, info.magnolia.content2bean.impl.Content2BeanTransformerImpl.class);
        // ComponentsTestUtil.setImplementation(info.magnolia.content2bean.Bean2ContentProcessor.class, info.magnolia.content2bean.Bean2ContentProcessorImpl.class);
    }

    /**
     * Override this method to provide the appropriate list of modules your tests need.
     */
    protected List<ModuleDefinition> getModuleDefinitionsForTests() throws ModuleManagementException {
        final ModuleDefinition core = new BetwixtModuleDefinitionReader().readFromResource("/META-INF/magnolia/core.xml");
        return Collections.singletonList(core);
    }

    protected MockHierarchyManager initMockConfigRepository(String properties) throws IOException, RepositoryException, UnsupportedRepositoryOperationException {

        MockHierarchyManager hm = MockUtil.createAndSetHierarchyManager(ContentRepository.CONFIG, properties);

        return hm;
    }

    /**
     * Utility assertion that will match a String against a regex,
     * <strong>with the DOTALL flag enabled, which means the . character will also match new lines</strong>.
     */
    public static void assertMatches(String message, String s, String regex) {
        assertMatches(message, s, regex, Pattern.DOTALL);
    }

    /**
     * Utility assertion that will match a String against a regex.
     */
    public static void assertMatches(String message, String s, String regex, int flags) {
        final StringBuffer completeMessage = new StringBuffer();
        if (message!=null) {
            completeMessage.append(message).append(":\n");
        }
        completeMessage.append("Input:\n    ");
        completeMessage.append(s);
        completeMessage.append("did not match regex:\n    ");
        completeMessage.append(regex);
        assertTrue(completeMessage.toString(), Pattern.compile(regex, flags).matcher(s).matches());
    }

}
