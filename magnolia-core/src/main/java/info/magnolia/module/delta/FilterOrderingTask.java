/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.filters.MagnoliaMainFilter;
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

    public FilterOrderingTask(String filterName, String[] requiredFiltersBefore) {
        super("Setup filter", "Sets the new " + filterName + " in the proper place.");
        this.filterToBeOrderedName = filterName;
        this.requiredFilters = new ArrayList(Arrays.asList(requiredFiltersBefore));
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Content filtersParent = ctx.getConfigHierarchyManager().getContent(MagnoliaMainFilter.SERVER_FILTERS);

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
