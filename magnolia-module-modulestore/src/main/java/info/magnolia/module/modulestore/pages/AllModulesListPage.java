/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.modulestore.pages;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.modulestore.ModuleStoreModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.taglibs.standard.functions.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author dschivo
 */
public class AllModulesListPage extends TemplatedMVCHandler {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(AllModulesListPage.class);

    private String remoteUrl;

    private String postdata;

    /**
     * 
     */
    public AllModulesListPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Returns the remoteUrl.
     * @return the remoteUrl
     */
    public String getRemoteUrl() {
        return remoteUrl;
    }

    /**
     * Returns the postdata.
     * @return the postdata
     */
    public String getPostdata() {
        return postdata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String show() {
        remoteUrl = ModuleStoreModule.getInstance().getAllmoduleslisturl();
        JSONArray installedModules = new JSONArray();
        ModuleRegistry registry = ModuleRegistry.Factory.getInstance();
        List<String> moduleNames = new ArrayList<String>(registry.getModuleNames());
        Collections.sort(moduleNames);
        for (String name : moduleNames) {
            ModuleDefinition def = registry.getDefinition(name);
            if (def != null) {
                JSONObject module = new JSONObject();
                module.set("name", def.getName());
                module.set("version", def.getVersion().toString());
                installedModules.put(module);
            }
        }
        JSONObject json = new JSONObject();
        json.set("installedModules", installedModules);
        postdata = Functions.escapeXml(json.toString());
        return super.show();
    }
}
