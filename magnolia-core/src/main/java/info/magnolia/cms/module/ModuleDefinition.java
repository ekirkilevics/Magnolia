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

import info.magnolia.cms.beans.config.ModuleRegistration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;


/**
 * Defines a module to register. The definition is constructed by the modules xml definition (using betwixt).
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ModuleDefinition {

    /**
     * The dependencies to other modules. This modules are loaded or registerd in advance
     */
    private Collection dependencies = new ArrayList();

    /**
     * The servlets used by this module. They will get registerd in the web.xml
     */
    private Collection servlets = new ArrayList();

    /**
     * The additional repostiories used by this module
     */
    private Collection repositories = new ArrayList();

    /**
     * The name of the module
     */
    private String name;

    /**
     * A nice name for displaying
     */
    private String displayName = "";

    /**
     * The version of the module
     */
    private String version;

    /**
     * A full descrpition of the module
     */
    private String description;

    /**
     * The className of the engine
     */
    private String className;

    /**
     * Empty constructor used by betwixt
     */
    public ModuleDefinition() {
    }

    /**
     * Minimal definition
     * @param name
     * @param version
     * @param className
     */
    public ModuleDefinition(String name, String version, String className) {
        setName(name);
        setVersion(version);
        setClassName(className);
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * If the displayName is empty the displayName will get set too.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
        if (StringUtils.isEmpty(this.getDisplayName())) {
            this.setDisplayName(name);
        }
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
     * @return Returns the dependencies.
     */
    public Collection getDependencies() {
        return this.dependencies;
    }

    public void addDependency(DependencyDefinition dep) {
        this.dependencies.add(dep);
    }

    /**
     * True if def is a direct or inderect dipendency of this module
     * @param def
     * @return
     */
    public boolean isDependent(ModuleDefinition def) {
        // direct dipendency
        for (Iterator iter = this.dependencies.iterator(); iter.hasNext();) {
            DependencyDefinition dep = (DependencyDefinition) iter.next();
            if (dep.getName().equals(def.getName())) {
                return true;
            }
        }

        // indirect dipendency
        for (Iterator iter = this.dependencies.iterator(); iter.hasNext();) {
            DependencyDefinition dep = (DependencyDefinition) iter.next();
            ModuleDefinition depDef = ModuleRegistration.getInstance().getModuleDefinition(dep.getName());
            if (depDef.isDependent(def)) {
                return true;
            }
        }
        // no dependency
        return false;
    }

    /**
     * @return Returns the servlets.
     */
    public Collection getServlets() {
        return this.servlets;
    }

    /**
     * Add a servlet definition
     */
    public void addServlet(ServletDefinition def) {
        if (StringUtils.isEmpty(def.getComment())) {
            def.setComment("a servlet used by the " + this.getName() + " module");
        }
        this.servlets.add(def);
    }

    /**
     * @return Returns the repositories.
     */
    public Collection getRepositories() {
        return this.repositories;
    }

    /**
     * Add a repository definition
     * @param repository
     */
    public void addRepository(RepositoryDefinition repository) {
        this.repositories.add(repository);
    }

    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
