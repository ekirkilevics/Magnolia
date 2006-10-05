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
package info.magnolia.maven.bundle;

import info.magnolia.maven.util.ExecUtil;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;

/**
 * @goal bundle
 * @requiresProject false
 * @execute goal=tomcat
 * @author philipp
 *
 */
public class BundleMojo extends AbstractMojo {

    /**
     * @parameter expression="${basedir}/magnolia"
     * @required
     * @readonly
     */
    private File magnoliaWarProjectDirectory;
    
    /**
     * @parameter expression="target/release"
     * @required
     */
    private File releaseDest;

    /**
     * @parameter expression="${basedir}/magnolia/target/magnolia-${project.version}"
     * @required
     * @readonly
     */
    private File magnoliaWarExplodedDirectory;
    
    /**
     * @parameter expression="${basedir}/magnolia/target/magnolia-${project.version}.war"
     * @required
     * @readonly
     */
    private File magnoliaWarFile;
    
	public void execute() throws MojoExecutionException, MojoFailureException {
        createWebapp("author");
        createWebapp("public");
	}

    protected void createWebapp(String name) throws MojoExecutionException {
        ExecUtil.execGoal("info.magnolia:maven-bundle-plugin:bootstrap -P" + name, this.getLog(), magnoliaWarProjectDirectory);
          
        // copy it
        try {
            File dest = new File(this.releaseDest, "bundle/tomcat/webapps/magnolia" + StringUtils.capitalize(name));
            FileUtils.deleteDirectory(new File(this.magnoliaWarExplodedDirectory, "logs"));
            FileUtils.copyDirectory(this.magnoliaWarExplodedDirectory, dest);
            
            // create war
            JarArchiver archiver = new JarArchiver();
            archiver.setDestFile(new File(this.releaseDest, "wars/magnolia" + StringUtils.capitalize(name) + ".war"));
            archiver.addDirectory(this.magnoliaWarExplodedDirectory);
            archiver.createArchive();
        }
        catch (IOException e) {
            throw new MojoExecutionException("can't copy webapp to bundle directory", e);
        }
        catch (ArchiverException e) {
            throw new MojoExecutionException("can't create war file", e);
        }
    }

}
