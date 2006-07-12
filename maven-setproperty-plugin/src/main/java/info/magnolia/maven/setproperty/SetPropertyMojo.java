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

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;


/**
 * A mojo to help setting properties.
 * @author philipp
 * @goal set-property
 */
public class SetPropertyMojo extends AbstractMojo {

    /**
     * Mavens session scope
     */
    public static final String SCOPE_SESSION = "session";

    /**
     * Mavens project scope
     */
    public static final String SCOPE_PROJECT = "project";

    /**
     * System scope
     */
    public static final String SCOPE_SYSTEM = "system";

    /**
     * Define the scope in which you like to set the property. session, project or system.
     * @parameter default-value="project"
     * @required
     */
    protected String scope;

    /**
     * Define the scope in which you like to set the property
     * @parameter expression="${propertyName}"
     * @required
     */
    protected String name;

    /**
     * This value gets only set if the property is not set yet
     * @parameter expression="${propertyDefaultValue}"
     */
    protected String defaultValue;

    /**
     * The value to set. The value is optional if you use defaultValue or a ValueProvider
     * @parameter expression="${propertyValue}"
     */
    protected String value;

    /**
     * Use a value provider if you need to set calculated values. You define the implementation with an implementation
     * attribute in the pom.xml
     * @parameter
     */
    protected ValueProvider valueProvider;

    /**
     * Use a value provider if you need to set calculated values. You define the implementation with an implementation
     * attribute in the pom.xml
     * @parameter
     */
    protected ValueProvider defaultValueProvider;

    /**
     * @parameter expression="${session}"
     */
    protected MavenSession session;

    /**
     * The project whose project files to create.
     * @parameter expression="${project}"
     */
    protected MavenProject project;

    /**
     * Set the property
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        Properties properties = getProperties();

        Object valueObj = value;
        Object defaultValueObj = defaultValue;

        // use the value poviders if defined
        if (this.valueProvider != null) {
            valueObj = this.valueProvider.getValue(project, session);
        }

        // use the value poviders if defined
        if (this.defaultValueProvider != null) {
            defaultValueObj = this.defaultValueProvider.getValue(project, session);
        }

        if (valueObj == null && defaultValueObj != null && !properties.containsKey(this.name)) {
            properties.put(this.name, defaultValueObj);
            this.getLog().info("property " + this.name + " set to default value " + defaultValueObj + " (scope: " + this.scope + ")");
        }

        if (valueObj != null) {
            properties.put(this.name, valueObj);
            this.getLog().info("property " + this.name + " set to value " + valueObj + " (scope: " + this.scope + ")");
        }
    }

    /**
     * Get the properties form the specified scope
     * @return the properties
     * @throws MojoExecutionException
     */
    protected Properties getProperties() throws MojoExecutionException {
        Properties properties;
        if (StringUtils.equals(this.scope, SCOPE_SESSION)) {
            properties = session.getExecutionProperties();
        }
        else if (StringUtils.equals(this.scope, SCOPE_PROJECT)) {
            properties = project.getProperties();
        }
        else if (StringUtils.equals(this.scope, SCOPE_SYSTEM)) {
            properties = System.getProperties();
        }
        else {
            throw new MojoExecutionException(this.scope + " is not a valid scope");
        }
        return properties;
    }
}
