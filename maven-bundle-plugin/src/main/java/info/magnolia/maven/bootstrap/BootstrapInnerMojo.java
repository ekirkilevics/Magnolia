/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */

package info.magnolia.maven.bootstrap;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ModuleRegistration;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.servlets.PropertyInitializer;
import info.magnolia.context.MgnlContext;

import java.io.File;
import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.mockrunner.mock.web.MockServletContext;


/**
 * The main Mojo creates this object using the custom class laoder. This avoids using reflection to create magnolia
 * objects. It also garantees that the resources (like module descriptors) are found.
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class BootstrapInnerMojo {

    /**
     * Execute the bootstrapping now. Use the passed mojo to get the parameters
     * @param mojo
     */
    public void execute(BootstrapMojo mojo) {
        // delete the repositories
        if (new File(mojo.webappDir + "/repositories").exists()) {
            try {
                FileUtils.deleteDirectory(new File(mojo.webappDir + "/repositories"));
            }
            catch (IOException e) {
                mojo.getLog().error("can't delete repositories directory");
            }
        }

        // create the mock context
        MockServletContext context = new MockServletContext();
        context.setRealPath(StringUtils.EMPTY, mojo.webappDir);
        
        // configure the properties
        context.setInitParameter(PropertyInitializer.MAGNOLIA_INITIALIZATION_FILE, mojo.configFile);
        

        
        ServletContextEvent event = new ServletContextEvent(context);

        ServletContextListener propertyInitializer = new PropertyInitializer();
        ServletContextListener shutdownManager = new ShutdownManager();

        boolean restart = true;
        while (restart) {
            propertyInitializer.contextInitialized(event);
            shutdownManager.contextInitialized(event);

            restart = ModuleRegistration.getInstance().isRestartNeeded();
            ModuleRegistration.getInstance().setRestartNeeded(false);

            if (restart) {
                shutdownManager.contextDestroyed(event);
                propertyInitializer.contextDestroyed(event);
            }
        }

        if (StringUtils.isNotEmpty(mojo.postBootstrapConfigurator)) {
            try {
                Class postConfigClass = this.getClass().getClassLoader().loadClass(mojo.postBootstrapConfigurator);

                PostBootstrapConfigurator postConfig = (PostBootstrapConfigurator) postConfigClass.newInstance();
                HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);

                postConfig.configure(mojo.webappDir, hm);
                try {
                    hm.save();
                }
                catch (RepositoryException e) {
                    mojo.getLog().error("can't save changes of the post bootstrap configurator", e);
                }
            }
            catch (Exception e) {
                mojo.getLog().error("can't instantiate post configurator", e);
            }
        }

        shutdownManager.contextDestroyed(event);
        propertyInitializer.contextDestroyed(event);

    }

}
