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
     * Use a value provider if you need to set calculated values. You define the implementation with an implementation
     * attribute in the pom.xml. Example:
     * 
     * <pre>
     *   &lt;configuration>
     *     &lt;properties>
     *       &lt;property implementation="info.magnolia.maven.setproperty.CurrentDateValueProvider">
     *          &lt;name>magnolia.currentDate&lt;/name>
     *          &lt;format>d. MMMM yyyy&lt;/format>
     *        &lt;/property>
     *        &lt;property implementation="info.magnolia.maven.setproperty.VersionNameValueProvider">
     *          &lt;name>magnolia.version&lt;/name>
     *        &lt;/property>
     *     &lt;/properties>
     *   &lt;/configuration>
     * </pre>
     * 
     * @parameter
     */
    protected ValueProvider[] properties;

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

        for (int j = 0; j < properties.length; j++) {
            ValueProvider provider = properties[j];
            String scope = StringUtils.defaultString(provider.getScope(), SCOPE_PROJECT);
            Properties properties = getProperties(scope);
            Object valueObj = provider.getValue(project, session);

            if (valueObj == null && provider.getDefaultValue() != null && !properties.containsKey(provider.getName())) {
                properties.put(provider.getName(), provider.getDefaultValue());
                this.getLog().info(
                    "property "
                        + provider.getName()
                        + " set to default value "
                        + provider.getDefaultValue()
                        + " (scope: "
                        + scope
                        + ")");
            }

            if (valueObj != null) {
                properties.put(provider.getName(), valueObj);
                this.getLog().info(
                    "property " + provider.getName() + " set to value " + valueObj + " (scope: " + scope + ")");
            }
        }

    }

    /**
     * Get the properties form the specified scope
     * @return the properties
     */
    protected Properties getProperties(String scope) {
        Properties properties;
        if (StringUtils.equals(scope, SCOPE_SESSION)) {
            properties = session.getExecutionProperties();
        }
        else if (StringUtils.equals(scope, SCOPE_SYSTEM)) {
            properties = System.getProperties();
        }
        else {
            properties = project.getProperties();
        }
        return properties;
    }
}
