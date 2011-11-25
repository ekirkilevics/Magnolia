/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.delta;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.filters.FilterManager;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;

/**
 * Orders a filter <strong>before</strong> a given set of other filters that must be <strong>after</strong> it. The
 * filter is placed directly before the first of the required filters. Does not take nested filter into account.
 *
 * @version $Id$
 * @see FilterOrderingTask
 */
public class OrderFilterBeforeTask extends AbstractRepositoryTask {

    private final String filterToBeOrderedName;
    private final String[] requiredFiltersAfter;

    public OrderFilterBeforeTask(String filterName, String[] requiredFiltersAfter) {
        this(filterName, "Sets the new " + filterName + " in the proper place.", requiredFiltersAfter);
    }

    public OrderFilterBeforeTask(String filterName, String description, String[] requiredFiltersAfter) {
        super("Setup " + filterName + " filter", description);
        this.filterToBeOrderedName = filterName;
        this.requiredFiltersAfter = requiredFiltersAfter;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Node filtersNode = ctx.getConfigJCRSession().getNode(FilterManager.SERVER_FILTERS);

        if (!filtersNode.hasNode(filterToBeOrderedName)) {
            throw new TaskExecutionException("Filter with name " + filterToBeOrderedName + " can't be found.");
        }

        Node firstRequiredFilter = findFirstChildNode(filtersNode, requiredFiltersAfter);

        if (firstRequiredFilter == null) {
            ctx.warn("Could not sort filter " + filterToBeOrderedName + ". It should be positioned before " + StringUtils.join(requiredFiltersAfter, ", "));
            return;
        }

        NodeUtil.orderBefore(filtersNode.getNode(filterToBeOrderedName), firstRequiredFilter.getName());
    }

    private Node findFirstChildNode(Node parentNode, String[] childNodeNames) throws RepositoryException {

        NodeIterator nodes = parentNode.getNodes();
        while (nodes.hasNext()) {
            Node child = nodes.nextNode();
            if (ArrayUtils.contains(childNodeNames, child.getName())) {
                return child;
            }
        }
        return null;
    }
}
