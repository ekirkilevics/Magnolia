package info.magnolia.maven.bundle;

import java.io.File;

import info.magnolia.maven.bootstrap.BootstrapMojo;

import org.apache.maven.BuildFailureException;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.reactor.MavenExecutionException;
import org.apache.tools.ant.taskdefs.War;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * Bootstraps and creates the war files for author and public
 * @author philipp
 * @goal wars
 * @execute phase=package
 */
public class WarsMojo extends AbstractMojo {
	
	/**
	 * @parameter expression="${project}"
	 */
	private MavenProject project;
	
    /**
     * The directory for the generated WAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#war}"
     * @required
     */
    private WarArchiver warArchiver;


    /**
     * @component
     */
    private MavenProjectHelper projectHelper;
	
    /**
     * The directory where the webapp is built.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    public File webappDir = new File(System.getProperty("user.dir"));
    
   
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		/*BootstrapMojo bootstrapMojo = new BootstrapMojo();
		bootstrapMojo.webappDir = this.webappDir.toString();
		bootstrapMojo.execute();
		*/
		createWar("author");

		/*
		bootstrapMojo.execute();
		 */
		createWar("public");
	}


	private void createWar(String name) throws MojoExecutionException {
		File warFile = new File(this.webappDir + "_" + name + ".war");
		
        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver( warArchiver );

        archiver.setOutputFile( warFile );

        try {
			warArchiver.addDirectory(webappDir);
	        warArchiver.setWebxml( new File( webappDir, "WEB-INF/web.xml" ) );
	        archiver.createArchive(project, new MavenArchiveConfiguration());
	        projectHelper.attachArtifact( project, "war", name, warFile );
        } catch (Exception e) {
			throw new MojoExecutionException("can't create war file", e);
		}
	}

}
