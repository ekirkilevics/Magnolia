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

import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.framework.editor.ContentDriver;
import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.framework.editor.EditorError;
import info.magnolia.ui.framework.editor.HasEditors;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Abstract base class for columns that use AbstractEditable. Provides selecting of items and tree refresh on modification.
 *
 * @param <D>
 * @author tmattsson
 */
public abstract class AbstractEditableColumn<D extends AbstractColumnDefinition> extends AbstractColumn<D> {

    private Shell shell;
    private EventBus eventBus;
    private PlaceController placeController;

    public AbstractEditableColumn(D def, EventBus eventBus, PlaceController placeController, Shell shell) {
        super(def);
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.shell = shell;
    }

    /**
     * Presenter for AbstractEditable.
     */
    public class PresenterImpl implements Editable.Presenter {

        private ContentDriver driver;

        @Override
        public void edit(Item item, final Editor editor) throws RepositoryException {

            driver = new ContentDriver();
            driver.initialize(new HasEditors() {
                @Override
                public Collection<? extends Editor> getEditors() {
                    ArrayList<Editor> list = new ArrayList<Editor>();
                    list.add(editor);
                    return list;
                }
            });

            driver.edit(item.isNode() ? (Node) item : item.getParent());
        }

        @Override
        public boolean save(Item item) throws RepositoryException {
            driver.flush(item.isNode() ? (Node) item : item.getParent());

            if (driver.hasErrors()) {
                List<EditorError> errors = driver.getErrors();
                shell.showNotification(errors.get(0).getMessage());

                // TODO show validation errors inline in tree

                return false;
            }

            // eventBus.fireEvent(new
            // ContentChangedEvent(item.getSession().getWorkspace().getName(), item.getPath()));

            return true;
        }

        @Override
        public void onClick(Item item) throws RepositoryException {

            // TODO ItemSelectedPlace wants a path relative to the tree root, not the jcr workspace absolute root

            final ItemSelectedPlace currentPlace = (ItemSelectedPlace) placeController.getWhere();
            placeController.goTo(new ItemSelectedPlace(item.getSession().getWorkspace().getName(), item.getPath(), currentPlace.getViewType()));
        }
    }
}
