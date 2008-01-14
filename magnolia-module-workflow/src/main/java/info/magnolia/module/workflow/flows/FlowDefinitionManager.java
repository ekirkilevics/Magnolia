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

import java.util.List;

import openwfe.org.engine.workitem.LaunchItem;

/**
 * Manages flow definitions.Configures the item to use a specifig flow.
 * @author philipp
 * @version $Id$
 *
 */
public interface FlowDefinitionManager {

    /**
     * Configure the launchItem, so that it will use the workflow
     */
    void configure(LaunchItem launchItem, String workflowName) throws FlowDefinitionException;
    
    /**
     * The flow name gets extracted out of the workflow definition.
     * @param definition the workflow definition xml
     */
    void saveDefinition(String definition) throws FlowDefinitionException;
    
    /**
     * Read the workflow definition stored with this name
     */
    String readDefinition(String workflowName) throws FlowDefinitionException;

    /**
     * List all available workflow names
     */
    List getDefinitionNames() throws FlowDefinitionException;
}
