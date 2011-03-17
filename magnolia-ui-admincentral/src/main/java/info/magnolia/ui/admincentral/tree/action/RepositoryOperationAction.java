/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.tree.action;

import info.magnolia.ui.admincentral.editworkspace.event.ContentChangedEvent;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Session;


/**
 * A repository operation action which saves the changes and informs the event bus.
 * @param <D> The {@link ActionDefinition} used by the action.
 * @param <T> The item type the operation is working with, {@link javax.jcr.Node} or
 * {@link javax.jcr.Property}.
 */
public abstract class RepositoryOperationAction<D extends ActionDefinition, T extends Item> extends ActionBase<D> {

    private T item;

    private EventBus eventBus;

    public RepositoryOperationAction(D definition, T item, EventBus eventBus) {
        super(definition);
        this.item = item;
        this.eventBus = eventBus;
    }

    public void execute() throws ActionExecutionException {
        Session session;
        try {
            session = item.getSession();
            final String path = item.getPath();
            onExecute(item);
            session.save();
            eventBus.fireEvent(new ContentChangedEvent(session.getWorkspace().getName(), path));
        }
        catch (RepositoryException e) {
            throw new ActionExecutionException("Can't execute repository operation.", e);
        }
    }

    abstract void onExecute(T item) throws RepositoryException;

}