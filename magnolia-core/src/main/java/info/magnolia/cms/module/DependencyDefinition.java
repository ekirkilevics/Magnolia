/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.module;

/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DependencyDefinition {

    /**
     * The name of the module
     */
    private String name;

    /**
     * The version of the module
     */
    private String version;
    
    /**
     * If this dependency is optional but should get loaded before this module
     */
    private boolean optional = false;

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    
    /**
     * @return Returns the optional.
     */
    public boolean isOptional() {
        return this.optional;
    }

    
    /**
     * @param optional The optional to set.
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

}
