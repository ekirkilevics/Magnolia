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
 *
 */
package info.magnolia.maven.setproperty;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;


/**
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class VersionNameValueProvider extends ValueProviderImpl {

    /**
     * Maven can not handle beans without any properties. This seam to be a bug according reading the configuration
     */
    public String dummy;

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
    
    public Object getValue(MavenProject project, MavenSession session) {
        String pomVersion = project.getVersion();
        String niceVersion = StringUtils.replace(pomVersion, "-", " ");
        niceVersion = niceVersion.toUpperCase();
        if (niceVersion.endsWith("SNAPSHOT")) {
            niceVersion = StringUtils.removeEnd(niceVersion, "SNAPSHOT");
            niceVersion += "(Snapshot: " + DateFormatUtils.format(new Date(), "dd.MM.yyyy HH:mm:ss") + ")";
        }
        return niceVersion;
    }

    
    /**
     * Getter for <code>defaultValue</code>.
     * @return Returns the defaultValue.
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    
    /**
     * Setter for <code>defaultValue</code>.
     * @param defaultValue The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    
  
}
