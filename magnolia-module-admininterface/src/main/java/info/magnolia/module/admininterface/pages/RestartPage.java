/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.ModuleLoader;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.module.Module;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 *
 * @deprecated not used nor useful anymore. The install/update mechanism introduced in 3.5 takes care of warning the user for needed restarts, before such a page could even be displayed.
 */
public class RestartPage extends TemplatedMVCHandler {

    /**
     * Required constructor.
     * @param name page name
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public RestartPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public List getRestartNeedingModules() {
        // is a system restart needed
        List restartNeedingModules = new ArrayList();

        // collect the modules needing a restart
        // TODO : review this !
        for (Iterator iter = ModuleLoader.getInstance().getModuleInstances().keySet().iterator(); iter.hasNext();) {
            String moduleName = (String) iter.next();
            Module module = ModuleLoader.getInstance().getModuleInstance(moduleName);
            if (module.isRestartNeeded()) {
                restartNeedingModules.add(module);
            }
        }

        return restartNeedingModules;
    }

    public Messages getMessages() {
        return MessagesManager.getMessages();
    }

}
