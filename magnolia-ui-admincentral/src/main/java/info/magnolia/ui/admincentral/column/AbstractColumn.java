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

import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Base class for tree columns.
 *
 * @param <D> type of the definition for this column.
 *
 * @version $Id$
 */
public abstract class AbstractColumn<D extends AbstractColumnDefinition> implements Column<AbstractColumnDefinition> {
    protected static final Label EMPTY_LABEL = new Label();
    protected final D definition;

    private Map<String, Component> components = new HashMap<String, Component>();

    public AbstractColumn(D def) {
        this.definition = def;
    }

    @Override
    public Component getComponent(Item item) throws RepositoryException {
        Component component = components.get(item.getPath());
        return component == null ? getDefaultComponent(item) : component;
    }

    protected abstract Component getDefaultComponent(Item item) throws RepositoryException;

    @Override
    public void setComponent(Item item, Component newValue) throws RepositoryException {
        components.put(item.getPath(), newValue);
    }

    @Override
    public D getDefinition() {
        return this.definition;
    }

    @Override
    public int getWidth() {
        return getDefinition().getWidth();
    }

    @Override
    public void setWidth(int newWidth) {
        getDefinition().setWidth(newWidth);
    }

    @Override
    public String getLabel() {
        return getDefinition().getLabel();
    }

    @Override
    public void setLabel(String newLabel) {
        getDefinition().setLabel(newLabel);
    }
}
