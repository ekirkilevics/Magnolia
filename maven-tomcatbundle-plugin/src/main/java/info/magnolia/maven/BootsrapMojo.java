package info.magnolia.maven;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.core.SystemProperty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.MavenSettingsBuilder;

import com.mockrunner.mock.web.MockServletContext;
/**
 * @goal bootstrap
 * @author philipp
 *
 */
public class BootsrapMojo extends AbstractMojo{
	
    /**
     * @parameter expression="${session}"
     */
	private MavenSession session;
	

    /**
     * The project whose project files to create.
     * 
     * @parameter expression="${project}"
     */
    protected MavenProject  project;
    
    
	public void execute() throws MojoExecutionException, MojoFailureException {
	    
	    System.out.println(project.getProperties());

	    System.out.println(session.getExecutionProperties());
	   
	    

		/*String testResourcesDir = MagnoliaTestUtils.getTestResourcesDir();
	        String baseTestDir = testResourcesDir + "/bootstrap-test";

	        MockServletContext context = new MockServletContext();

	        Map config = new HashMap();
	        context.setRealPath(StringUtils.EMPTY, baseTestDir);

	        config.put(SystemProperty.MAGNOLIA_REPOSITORIES_CONFIG, baseTestDir + "/WEB-INF/config/repositories.xml");

	        config.put(SystemProperty.MAGNOLIA_BOOTSTRAP_ROOTDIR, MagnoliaTestUtils.getProjectRoot()
	            + "/src/webapp/WEB-INF/bootstrap");

	        ConfigLoader loader = new ConfigLoader(context, config);
	        */
	}
	
	public static void main(String[] args) throws Exception {
		new BootsrapMojo().execute();
	}

}
