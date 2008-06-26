/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.module;

import info.magnolia.cms.beans.config.ModuleRegistration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;


/**
 * Defines a module to register. The definition is constructed by the modules xml definition (using betwixt).
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 * @deprecated since 3.5 use info.magnolia.module.model.ModuleDefinition
 */
public class ModuleDefinition {

    /**
     * The dependencies to other modules. This modules are loaded or registerd in advance
     * @deprecated
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
     * Properties to set in the magnolia system properties
     */
    private Collection properties = new ArrayList();

    /**
     * The name of the module
     */
    private String name;

    /**
     * A nice name for displaying
     */
    private String displayName;

    /**
     * The version of the module
     */
    private String version;

    /**
     * A full description of the module
     */
    private String description;

    /**
     * The className of the engine
     */
    private String className;

    /**
     * The root directory or jar file for the module.
     */
    private File moduleRoot;

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
     * @return Returns the displayName. (or the name if displayName wasn't set)
     */
    public String getDisplayName() {
        if (StringUtils.isEmpty(this.displayName)) {
            return this.name;
        }

        return this.displayName;
    }

    /**
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for <code>moduleRoot</code>.
     * @return Returns the moduleRoot.
     */
    public File getModuleRoot() {
        return this.moduleRoot;
    }

    /**
     * Setter for <code>moduleRoot</code>.
     * @param moduleRoot The moduleRoot to set.
     */
    public void setModuleRoot(File moduleRoot) {
        this.moduleRoot = moduleRoot;
    }

    /**
     * @return the properties
     */
    public Collection getProperties() {
        return properties;
    }

    public void addProperty(PropertyDefinition property) {
        properties.add(property);
    }

    /**
     * Convenience method which returns the value of the given property,
     * or null if it does not exist.
     */
    public String getProperty(String propertyName) {
        final Iterator it = properties.iterator();
        while (it.hasNext()) {
            final PropertyDefinition p = (PropertyDefinition) it.next();
            if (propertyName.equals(p.getName())) {
                return p.getValue();
            }
        }
        return null;
    }
}
