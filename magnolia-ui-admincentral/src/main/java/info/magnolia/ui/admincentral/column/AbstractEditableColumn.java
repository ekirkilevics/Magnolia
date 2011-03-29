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
package info.magnolia.ui.admincentral.column;

import java.util.ArrayList;
import java.util.Collection;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.ui.admincentral.workbench.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.framework.editor.ContentDriver;
import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.framework.editor.HasEditors;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.model.column.definition.ColumnDefinition;

/**
 * Abstract base class for columns that use AbstractEditable. Provides selecting of items and tree refresh on modification.
 *
 * @param <D>
 * @author tmattsson
 */
public abstract class AbstractEditableColumn<D extends ColumnDefinition> extends AbstractColumn<D> {

    private EventBus eventBus;

    private PlaceController placeController;

    public AbstractEditableColumn(D def, EventBus eventBus, PlaceController placeController) {
        super(def);
        this.eventBus = eventBus;
        this.placeController = placeController;
    }

    /**
     * Presenter for AbstractEditable.
     */
    protected class PresenterImpl implements AbstractEditable.Presenter {

        private ContentDriver driver;

        public void edit(Item item, final Editor editor) throws RepositoryException {

            driver = new ContentDriver();
            driver.initialize(new HasEditors() {
                public Collection<? extends Editor> getEditors() {
                    ArrayList<Editor> list = new ArrayList<Editor>();
                    list.add(editor);
                    return list;
                }
            });

            driver.edit(item instanceof Node ? (Node) item : item.getParent());
        }

        public boolean save(Item item) throws RepositoryException {
            driver.flush(item instanceof Node ? (Node) item : item.getParent());

            if (driver.hasErrors())
                // TODO show validation errors
                return false;

            eventBus.fireEvent(new ContentChangedEvent(item.getSession().getWorkspace().getName(), item.getPath()));

            return true;
        }

        public void onClick(Item item) throws RepositoryException {
            placeController.goTo(new ItemSelectedPlace(item.getSession().getWorkspace().getName(), item.getPath()));
        }
    }
}
