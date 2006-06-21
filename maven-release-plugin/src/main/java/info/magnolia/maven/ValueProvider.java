package info.magnolia.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

/**
 * Use in the SetPropertyMojo to pass calculated values. 
 * @author philipp
 *
 */
public interface ValueProvider {
	/**
	 * Calculate the value
	 * @param project the maven project
	 * @param session the maven session
	 * @return the calculated value
	 */
	public Object getValue(MavenProject project, MavenSession session);
}
