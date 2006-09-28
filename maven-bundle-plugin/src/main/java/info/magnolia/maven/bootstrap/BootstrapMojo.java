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

import info.magnolia.maven.util.DelegateableClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;


/**
 * Bootstrap magnolia. It creates a new class laoder using the jars in the WEB-INF/lib. To do so a object of type
 * BootstrapInnerMojo is created using the custom classloader. This garanties that the magnolia code will find the
 * resources.
 * @goal bootstrap
 * @author philipp
 */
public class BootstrapMojo extends AbstractMojo {

    /**
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    public String webappDir = System.getProperty("user.dir");

    /**
     * @parameter default="WEB-INF/config/default/magnolia.properties"
     */
    public String configFile = "WEB-INF/config/default/magnolia.properties";

    /**
     * The class name for a post bootstrap configurator. This can't be 
     * @parameter
     */
    public String postBootstrapConfigurator;
    
    /**
     * Load those classes from the original class loader
     */
    public String[] loadByOrgClassLoaderPatterns = {
        "java.*",
        "javax.xml.*",
        "org.apache.commons.logging.*",
        "org.apache.log4j.*",
        "org.w3c.dom.*",
        "org.apache.xerces.*",
        "org.xml.sax.*",
        "com.sun.org.apache.xalan.*",
        "info.magnolia.maven.bootstrap.BootstrapMojo"
    };

    /**
     * Create the custom class loader and create the inner mojo object using it.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        // delete the repositories
        if (new File(webappDir + "/repositories").exists()) {
            try {
                FileUtils.deleteDirectory(new File(webappDir + "/repositories"));
            }
            catch (IOException e) {
                getLog().error("can't delete repositories directory");
            }
        }
        
        boolean restart = bootstrap();
        
        while(restart){
            restart = bootstrap();
        }
    }

    protected boolean bootstrap() throws MojoExecutionException {
        URLClassLoader customLoader;
        URLClassLoader orgLoader = (URLClassLoader) this.getClass().getClassLoader();

        customLoader = creatCustomClassLoader(orgLoader);

        // make the custom loader gernal available
        Thread.currentThread().setContextClassLoader(customLoader);

        boolean restart = executeInnerMojo(customLoader);

        // set again the original class loader
        Thread.currentThread().setContextClassLoader(orgLoader);
        
        return restart;
    }

    /**
     * Make it runnable as an executable
     * @param args no args taken
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        BootstrapMojo mojo = new BootstrapMojo();
        mojo.execute();
    }

    /**
     * Create the custom class loader
     * @param orgLoader the original loader used as parent
     * @return the custom loader
     * @throws MojoExecutionException
     */
    private URLClassLoader creatCustomClassLoader(URLClassLoader orgLoader) throws MojoExecutionException {
        URLClassLoader customLoader;
        if (this.getLog().isDebugEnabled()) {
            this.getLog().debug(
                "jars loaded by the maven class loader:"
                    + StringUtils.join(((URLClassLoader) orgLoader).getURLs(), "\n"));
        }
    
        // get a list of jars in WEB-INF/Lib
        Collection jars = FileUtils.listFiles(new File(webappDir + "/WEB-INF/lib"), new IOFileFilter() {
    
            public boolean accept(File pathname, String name) {
                return accept(pathname);
            }
    
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        }, FileFilterUtils.trueFileFilter());
    
        // create the urls for the jars (used by the custom class laoder
        URL[] urls = null;
        try {
            urls = FileUtils.toURLs((File[]) jars.toArray(new File[jars.size()]));
        }
        catch (IOException e) {
            throw new MojoExecutionException("can't create the urls for the jars in WEB-INF/Lib", e);
        }
    
        // make the inner mojo class reachalbe through the custom loader
        try {
            URL innerMojoURL = orgLoader.findResource("info/magnolia/maven/bootstrap/BootstrapInnerMojo.class");
            String urlString = innerMojoURL.toString();
            urlString = StringUtils.removeEnd(urlString, "info/magnolia/maven/bootstrap/BootstrapInnerMojo.class");
            // we need to do this if the class was found in a jar
            urlString = StringUtils.removeStart(StringUtils.removeEnd(urlString, "!/"), "jar:");
            urls = (URL[]) ArrayUtils.add(urls, new URL(urlString));
        }
        catch (MalformedURLException e) {
            throw new MojoExecutionException("can't add the url for the inner class to the custom class loader", e);
        }
    
        customLoader = new DelegateableClassLoader(urls, orgLoader, this.loadByOrgClassLoaderPatterns);
    
        if (this.getLog().isDebugEnabled()) {
            this.getLog().debug(
                "jars loaded by the custom class loader:" + StringUtils.join((customLoader).getURLs(), "\n"));
        }
        return customLoader;
    }

    /**
     * Execute the inner mojo: calls execute()
     * @param customLoader the custom loader to use for instantiating the inner mojo
     * @throws MojoExecutionException
     */
    private boolean executeInnerMojo(URLClassLoader customLoader) throws MojoExecutionException {
        // create the inner mojo using the custom loader
        Class klass;
        Object innerMojo;
    
        try {
            klass = customLoader.loadClass("info.magnolia.maven.bootstrap.BootstrapInnerMojo");
        }
        catch (ClassNotFoundException e) {
            throw new MojoExecutionException("can't find class for the inner mojo through the custom class laoder", e);
        }
    
        try {
            innerMojo = klass.newInstance();
        }
        catch (InstantiationException e) {
            throw new MojoExecutionException("can't instantiate the inner mojo", e);
        }
        catch (IllegalAccessException e) {
            throw new MojoExecutionException("can't instantiate the inner mojo", e);
        }
    
        try {
        	// use this if MethodUtils couldn't get used
        	// Method executeMethod = klass.getMethod("execute", new Class[]{this.getClass()});
            // executeMethod.invoke(innerMojo, new Object[]{this});
        	// introduce a dependency to log4j here
            Boolean res = (Boolean) MethodUtils.invokeMethod(innerMojo, "execute", new Object[]{this});
            return res.booleanValue();
            
        }
        catch (Exception e) {
            throw new MojoExecutionException("can't call execute() method on the inner mojo", e);
        }
    }

}
