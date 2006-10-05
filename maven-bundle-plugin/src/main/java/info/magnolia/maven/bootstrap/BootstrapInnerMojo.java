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

import info.magnolia.cms.beans.config.ModuleRegistration;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.servlets.PropertyInitializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

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
     * @throws MojoExecutionException 
     */
    public boolean execute(BootstrapMojo mojo) throws MojoExecutionException {

        // create the mock context
        MockServletContext context = new MockServletContext();
        context.setRealPath(StringUtils.EMPTY, mojo.webappDir);
        
        // configure the config files
        context.setInitParameter(PropertyInitializer.MAGNOLIA_INITIALIZATION_FILE, mojo.configFile);
        
        ServletContextEvent event = new ServletContextEvent(context);

        ServletContextListener propertyInitializer = new PropertyInitializer();
        ServletContextListener shutdownManager = new ShutdownManager();

        propertyInitializer.contextInitialized(event);
        shutdownManager.contextInitialized(event);
        
        boolean restart = ModuleRegistration.getInstance().isRestartNeeded();
        
        if (!restart) {
            for (int i = 0; i < mojo.postBootstrappers.length; i++) {
                PostBootstrapper postBootstrapper = mojo.postBootstrappers[i];
                try {
                    // clone the bootstrapper but use the custom classloader
                    Class klass = this.getClass().forName(postBootstrapper.getClass().getName());
                    Object obj = klass.newInstance();
                    BeanUtils.copyProperties(obj, postBootstrapper);
                    MethodUtils.invokeExactMethod(obj, "execute", mojo.webappDir);
                    mojo.getLog().info("executing post bootstrapper: " + postBootstrapper );
                }
                catch (Exception e) {
                    throw new MojoExecutionException("can't execute post-bootstrapper", e);
                }
            }
        }

        shutdownManager.contextDestroyed(event);
        propertyInitializer.contextDestroyed(event);
        return restart;
    }

}
