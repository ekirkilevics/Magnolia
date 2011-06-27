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
package info.magnolia.ui.admincentral.list.container;


import info.magnolia.ui.admincentral.container.ContainerItem;
import info.magnolia.ui.admincentral.container.ContainerItemId;
import info.magnolia.ui.admincentral.container.JcrContainer;
import info.magnolia.ui.admincentral.container.JcrContainerSource;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import javax.jcr.RepositoryException;
import javax.jcr.query.RowIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A flat implementation of {@link JcrContainer} where relationships are not taken into account.
 * @author fgrilli
 *
 */
public class FlatJcrContainer extends JcrContainer {

    private static final Logger log = LoggerFactory.getLogger(FlatJcrContainer.class);
    /**
     * Constructor for {@link FlatJcrContainer}.
     */
    public FlatJcrContainer(JcrContainerSource jcrContainerSource, WorkbenchDefinition workbenchDefinition) {
        super(jcrContainerSource, workbenchDefinition);

    }

    @Override
    public long update(RowIterator iterator) throws RepositoryException {
        log.debug("updating container...");
        getItemIndexes().clear();
        getCachedItems().clear();

        long rowCount = 0;
        while(iterator.hasNext()){

            final ContainerItemId id = createContainerId(iterator.nextRow().getNode("content"));
            /* Cache item */
            getItemIndexes().put(rowCount++, id);
            getCachedItems().put(id, new ContainerItem(id, this));

        }
        setSize(Long.valueOf(rowCount).intValue());
        super.fireItemSetChange();
        return rowCount;
    }
}
