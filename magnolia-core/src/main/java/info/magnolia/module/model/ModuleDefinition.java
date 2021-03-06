/**
 * This file Copyright (c) 2003-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.model;

import info.magnolia.module.ModuleVersionHandler;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * Describes a module. Bean representation of a module's xml descriptor.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleDefinition {
    private String name;
    private String displayName;
    private String description;
    private String className;
    private Class<? extends ModuleVersionHandler> versionHandler;
    private Version version;
    private Collection<DependencyDefinition> dependencies = new ArrayList<DependencyDefinition>();
    private Collection<ServletDefinition> servlets = new ArrayList<ServletDefinition>();
    private Collection<RepositoryDefinition> repositories = new ArrayList<RepositoryDefinition>();
    private Collection<PropertyDefinition> properties = new ArrayList<PropertyDefinition>();
    private Collection<ComponentsDefinition> components = new ArrayList<ComponentsDefinition>();

    public ModuleDefinition() {
    }

    public ModuleDefinition(String name, Version version, String className, Class<? extends ModuleVersionHandler> versionHandler) {
        this.name = name;
        this.version = version;
        this.className = className;
        this.versionHandler = versionHandler;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the displayName or the name if displayName wasn't set.
     */
    public String getDisplayName() {
        if (StringUtils.isEmpty(this.displayName)) {
            return this.name;
        }

        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Class<? extends ModuleVersionHandler> getVersionHandler() {
        return versionHandler;
    }

    public void setVersionHandler(Class<? extends ModuleVersionHandler> versionHandler) {
        this.versionHandler = versionHandler;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }

    public Collection<DependencyDefinition> getDependencies() {
        return this.dependencies;
    }

    public void setDependencies(Collection<DependencyDefinition> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(DependencyDefinition dep) {
        dependencies.add(dep);
    }

    public Collection<ServletDefinition> getServlets() {
        return this.servlets;
    }

    public void setServlets(Collection<ServletDefinition> servlets) {
        for (ServletDefinition def : servlets) {
            this.addServlet(def);
        }
    }

    public void addServlet(ServletDefinition def) {
        if (StringUtils.isEmpty(def.getComment())) {
            def.setComment("a servlet used by the " + this.getName() + " module");
        }
        this.servlets.add(def);
    }

    public Collection<RepositoryDefinition> getRepositories() {
        return this.repositories;
    }

    public void setRepositories(Collection<RepositoryDefinition> repositories) {
        this.repositories = repositories;
    }

    public void addRepository(RepositoryDefinition repository) {
        this.repositories.add(repository);
    }

    public Collection<PropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(Collection<PropertyDefinition> properties) {
        this.properties = properties;
    }

    public void addProperty(PropertyDefinition property) {
        properties.add(property);
    }

    public Collection<ComponentsDefinition> getComponents() {
        return components;
    }

    public void setComponents(Collection<ComponentsDefinition> components) {
        this.components = components;
    }

    public boolean addComponents(ComponentsDefinition components) {
        return this.components.add(components);
    }

    /**
     * Convenience method which returns the value of the given property,
     * or null if it does not exist.
     */
    public String getProperty(String propertyName) {
        for (PropertyDefinition p : properties) {
            if (propertyName.equals(p.getName())) {
                return p.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getDisplayName() + " (version " + version + ")";
    }
}
