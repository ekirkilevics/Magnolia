package info.magnolia.maven;

import java.util.Date;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public class CurrentDateValueProvider implements ValueProvider {
	
	private boolean asString = false;

	public Object getValue(MavenProject project, MavenSession session) {
		Date date =  new Date();
		if(this.isAsString()){
			return date.toString();
		}
		else{
			return date;
		}
	}

	public boolean isAsString() {
		return asString;
	}

	public void setAsString(boolean asString) {
		this.asString = asString;
	}

}
