package info.magnolia.maven.bundle;

import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.resources.ResourcesMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;

/**
 * @goal bundle
 * @execute goal=wars,tomcat
 * @author philipp
 *
 */
public class BundleMojo extends AbstractMojo {

	/**
	 * @parameter experssion="${plugin}"
	 */
	private PluginDescriptor plugin;

	/**
	 * @parameter experssion="${project}"
	 */
	private MavenProject project;

	/**
	 * @parameter experssion="${session}"
	 */
	private MavenSession session;
	
	/**
	 * @component
	 */
	private PluginManager manager;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// execute tomcat bundel first
		/*MojoDescriptor mojo = plugin.getMojo("tomcat");
		MojoExecution execution = new MojoExecution(mojo);
		try {
			manager.executeMojo(project, execution , session);
		} catch (Exception e) {
			throw new MojoExecutionException("can't call tomcat download goal", e);
		}
		
		// filter the resources
		ResourcesMojo resourcesMojo = new ResourcesMojo();
		
		resourcesMojo.execute();
		resourcesMojo.
		*/
		
	}

}
