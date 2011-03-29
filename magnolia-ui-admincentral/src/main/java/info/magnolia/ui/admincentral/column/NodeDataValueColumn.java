/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import java.io.Serializable;
import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.model.column.definition.NodeDataValueColumnDefinition;


/**
 * Describes a column that displays the value of a NodeData. Used in the config tree when a row in
 * the TreeTable is a NodeData.
 *
 * @author dlipp
 * @author tmattsson
 */
public class NodeDataValueColumn extends AbstractColumn<NodeDataValueColumnDefinition> implements Serializable {

    private EventBus eventBus;

    private PlaceController placeController;

    public NodeDataValueColumn(NodeDataValueColumnDefinition def, EventBus eventBus, PlaceController placeController) {
        super(def);
        this.eventBus = eventBus;
        this.placeController = placeController;
    }

    @Override
    public Component getComponent(Item item) throws RepositoryException {
        if (item instanceof Property) {
            return new EditableText(item, eventBus, "", placeController) {

                @Override
                protected String getValue(Item item) throws RepositoryException {
                    Property property = (Property) item;
                    return property.getString();
                }
            };
        }
        return new Label();
    }
}
