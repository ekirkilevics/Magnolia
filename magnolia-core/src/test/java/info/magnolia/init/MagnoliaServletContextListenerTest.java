/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.init;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.context.ContextFactory;
import info.magnolia.module.ModuleManager;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.TestMagnoliaInitPaths;

import javax.servlet.ServletContextEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MagnoliaServletContextListenerTest {

    private ServletContextEvent sce;
    
    private MagnoliaServletContextListener ctxListener;
    
    private ModuleManager mm;
    
    private ModuleRegistry mr;
    
    private ConfigLoader cl;
    
    private MagnoliaInitPaths testInitPath;
    
    private MagnoliaConfigurationProperties initProperties;

    private ContextFactory ctxFactory;
    
    @Before
    public void setUp() {
        mm = mock(ModuleManager.class);
        mr = mock(ModuleRegistry.class);
        cl = mock(ConfigLoader.class);
        ctxFactory = mock(ContextFactory.class);                
        sce = mock(ServletContextEvent.class);
    }
    
    @After
    public void tearDown() {
        ctxListener.contextDestroyed(sce);
    }
    
    @Test
    public void testNullSeverNameIsSetToDeault() throws Exception {      
        testInitPath = new TestMagnoliaInitPaths(null, "/tmp/magnoliaTests", "magnoliaTests", "/test");
        initProperties  = new TestMagnoliaConfigurationProperties();

        ctxListener = new MagnoliaServletContextListener() {
            protected ComponentProviderConfiguration getPlatformComponents() {
                return configurationSetUp();
                };
            };              
        ctxListener.contextInitialized(sce);
        
        assertEquals("default", System.getProperty("server"));
    }
    
    public ComponentProviderConfiguration configurationSetUp() {
        ComponentProviderConfiguration config = new ComponentProviderConfiguration();
        config.registerInstance(MagnoliaInitPaths.class, testInitPath);
        config.registerInstance(ModuleManager.class, mm);
        config.registerInstance(MagnoliaConfigurationProperties.class, initProperties);
        config.registerInstance(ModuleRegistry.class, mr);
        config.registerInstance(ConfigLoader.class, cl);
        config.registerInstance(ContextFactory.class, ctxFactory);
        return config;
    }
    
}