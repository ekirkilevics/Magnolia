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
    private Version versionDefinition;


    public ModuleDefinition() {
    }

    public ModuleDefinition(String name, String version, String className, Class versionHandler) {
        super(name, version, className);
        this.versionHandler = versionHandler;
        this.versionDefinition = Version.parseVersion(version);
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
        return versionDefinition;
    }

    public void addDependency(DependencyDefinition dep) {
        dependencies.add(dep);
    }

    /**
     * {@inheritDoc}
     */
    public void setVersion(String version) {
        super.setVersion(version);
        this.versionDefinition = Version.parseVersion(version);
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
        return getDisplayName() + " (version " + versionDefinition + ")";
    }
}
