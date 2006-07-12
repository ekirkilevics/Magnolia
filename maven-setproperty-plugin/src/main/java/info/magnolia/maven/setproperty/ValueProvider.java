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
package info.magnolia.maven.setproperty;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;


/**
 * Use in the SetPropertyMojo to pass calculated values.
 * @author philipp
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
