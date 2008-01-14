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
package info.magnolia.module.workflow.flows;

import info.magnolia.cms.util.DeprecationUtil;
import openwfe.org.engine.workitem.LaunchItem;

import java.util.List;

/**
 *
 * @author philipp
 * @version $Id$
 * @deprecated This was used in the 3.0 community edition to restrict to a single flow definition. Since 3.5,
 *             DefaultFlowDefinitionManager is part of CE.
 */
public class SimpleFlowDefinitionManager extends DefaultFlowDefinitionManager {

    public void configure(LaunchItem launchItem, String workflowName) throws FlowDefinitionException {
        DeprecationUtil.isDeprecated("Use DefaultFlowDefinitionManager");
        super.configure(launchItem, workflowName);
    }

    public String readDefinition(String workflowName) throws FlowDefinitionException {
        DeprecationUtil.isDeprecated("Use DefaultFlowDefinitionManager");
        return super.readDefinition(workflowName);
    }

    public void saveDefinition(String definition) throws FlowDefinitionException {
        DeprecationUtil.isDeprecated("Use DefaultFlowDefinitionManager");
        super.saveDefinition(definition);
    }

    public List getDefinitionNames() {
        DeprecationUtil.isDeprecated("Use DefaultFlowDefinitionManager");
        return super.getDefinitionNames();
    }
}
