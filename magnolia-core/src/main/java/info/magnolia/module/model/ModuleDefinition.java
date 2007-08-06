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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleDefinition extends info.magnolia.cms.module.ModuleDefinition {
    private final Collection dependencies = new ArrayList();
    private Class versionHandler;

    public ModuleDefinition() {
    }

    public ModuleDefinition(String name, String version, String className, Class versionHandler) {
        super(name, version, className);
        this.versionHandler = versionHandler;
    }

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
        return Version.parseVersion(getVersion());
    }

    public void addDependency(DependencyDefinition dep) {
        dependencies.add(dep);
    }

    /**
     * making sure betwixt adds the right type
     * @deprecated
     */
    public void addDependency(info.magnolia.cms.module.DependencyDefinition dep) {
        this.addDependency(new DependencyDefinition(dep.getName(), dep.getVersion(), dep.isOptional()));
    }

    public Collection getDependencies() {
        return dependencies;
    }

    /** @deprecated should not be used */
    public File getModuleRoot() {
        return super.getModuleRoot();
    }

    /** @deprecated should not be used */
    public void setModuleRoot(File moduleRoot) {
        super.setModuleRoot(moduleRoot);
    }

    public String toString() {
        return getDisplayName() + " version " + getVersionDefinition();
    }
}
