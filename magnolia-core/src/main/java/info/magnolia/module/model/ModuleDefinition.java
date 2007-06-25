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
package info.magnolia.module.model;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleDefinition extends info.magnolia.cms.module.ModuleDefinition {
    private Class versionHandler;

    public Class getVersionHandler() {
        return versionHandler;
    }

    public void setVersionHandler(Class versionHandler) {
        this.versionHandler = versionHandler;
    }

    /**
     * TODO : rename to getVersion once we got rid of info.magnolia.cms.module.ModuleDefinition
     */
    public Version getVersionDefinition() {
        return new Version(getVersion());
    }

    // making sure betwixt adds the right type
    public void addDependency(info.magnolia.cms.module.DependencyDefinition dep) {
        this.addDependency((DependencyDefinition)dep);
    }

    public void addDependency(DependencyDefinition dep) {
        super.addDependency(dep);
    }

}
