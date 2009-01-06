/**
 * This file Copyright (c) 2007-2009 Magnolia International
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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;


/**
 * Checks if installed modules contain filter definitions and if so warns on installation with an appropriate message.
 * 
 * @author vsteller
 * @version $Id$
 *
 */
public class WarnIgnoredModuleFilters extends AllModulesNodeOperation {

    
    public WarnIgnoredModuleFilters() {
        super("Filters", "Warns if filters are registered in module nodes since they're ignored in 3.5.");
    }
    
    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx)
        throws RepositoryException, TaskExecutionException {
        
        if (node.hasContent("filters")) {
            final Collection moduleFilters = node.getContent("filters").getChildren();
            final Iterator moduleFiltersIterator = moduleFilters.iterator();
            while (moduleFiltersIterator.hasNext()) {
                final Content currentFilter = (Content) moduleFiltersIterator.next();
                ctx.warn("Filters registered in module nodes are ignored since 3.5. Please re-check and move the filter '" + currentFilter.getName() + "' registered in module '" + node.getName() + "' to the filter chain under /server/filters manually.");
            }
        }
    }
}
