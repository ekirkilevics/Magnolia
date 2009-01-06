/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.filters.MgnlMainFilter;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Orders a filter after a given set of other filters.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FilterOrderingTask extends AbstractRepositoryTask {
    private final String filterToBeOrderedName;
    private final List requiredFilters;

    /**
     * 
     * @param requiredFiltersBefore an array of filter names that must appear <strong>before</strong> the filter specified as filterName.
     */
    public FilterOrderingTask(String filterName, String[] requiredFiltersBefore) {
        this(filterName, "Sets the new " + filterName + " in the proper place.", requiredFiltersBefore);
    }

    public FilterOrderingTask(String filterName, String description, String[] requiredFiltersBefore) {
        super("Setup " + filterName + " filter", description);
        this.filterToBeOrderedName = filterName;
        this.requiredFilters = new ArrayList(Arrays.asList(requiredFiltersBefore));
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Content filtersParent = ctx.getConfigHierarchyManager().getContent(MgnlMainFilter.SERVER_FILTERS);

        // assert filter exists // TODO : this does not take nested filters into account
        if (!filtersParent.hasContent(filterToBeOrderedName)) {
            throw new TaskExecutionException("Filter with name " + filterToBeOrderedName + " can't be found.");
        }

        // TODO : this does not take nested filters into account
        final Collection filters = filtersParent.getChildren();
        final Iterator it = filters.iterator();
        while (it.hasNext()) {
            final Content filter = (Content) it.next();
            final String filterName = filter.getName();

            // have we seen all filters yet ?
            if (requiredFilters.size() == 0) {
                filtersParent.orderBefore(filterToBeOrderedName, filterName);
                return;
            }

            // remove the filter we're iterating over: it's been seen
            requiredFilters.remove(filterName);
        }
        ctx.warn("Could not sort filter "+filterToBeOrderedName+". It should be positioned after " + requiredFilters);
    }
}
