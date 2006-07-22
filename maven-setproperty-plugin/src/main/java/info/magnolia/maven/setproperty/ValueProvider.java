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
    Object getValue(MavenProject project, MavenSession session);

    /**
     * Getter for <code>name</code>.
     * @return Returns the name.
     */
    String getName();

    /**
     * Setter for <code>name</code>.
     * @param name The name to set.
     */
    void setName(String name);

    /**
     * Getter for <code>scope</code>.
     * @return Returns the scope.
     */
    String getScope();

    /**
     * Setter for <code>scope</code>.
     * @param scope The scope to set.
     */
    void setScope(String scope);

    /**
     * Getter for <code>value</code>.
     * @return Returns the value.
     */
    String getValue();

    /**
     * Setter for <code>value</code>.
     * @param value The value to set.
     */
    void setValue(String value);

    /**
     * Getter for <code>defaultValue</code>.
     * @return Returns the defaultValue.
     */
    public String getDefaultValue();

    /**
     * Setter for <code>defaultValue</code>.
     * @param defaultValue The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue);

}
