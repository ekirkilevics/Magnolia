/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.storeclient.pages;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.model.ModuleDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Lists all installed modules.
 * @author dschivo
 */
public class InstalledModulesListPage extends TemplatedMVCHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(InstalledModulesListPage.class);

    private List<ModuleDefinition> installedModules = new ArrayList<ModuleDefinition>();

    /**
     *
     */
    public InstalledModulesListPage(String name, HttpServletRequest request, HttpServletResponse response) {

        super(name, request, response);
    }

    /**
     * Returns the installedModules.
     * @return the installedModules
     */
    public List<ModuleDefinition> getInstalledModules() {
        return installedModules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String show() {
        ModuleRegistry registry = ModuleRegistry.Factory.getInstance();
        List<String> moduleNames = new ArrayList<String>(registry.getModuleNames());
        Collections.sort(moduleNames);
        for (String name : moduleNames) {
            ModuleDefinition def = registry.getDefinition(name);
            if (def != null) {
                installedModules.add(def);
            }
        }
        return super.show();
    }
}
